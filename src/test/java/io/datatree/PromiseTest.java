/**
 * This software is licensed under the Apache 2 license, quoted below.<br>
 * <br>
 * Copyright 2018 Andras Berkes [andras.berkes@programmer.net]<br>
 * <br>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at<br>
 * <br>
 * http://www.apache.org/licenses/LICENSE-2.0<br>
 * <br>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datatree;

import org.junit.Test;

import junit.framework.TestCase;

public class PromiseTest extends TestCase {

	@Test
	public void testPromise() throws Exception {

		// Complete with null
		Tree out = Promise.resolve().waitFor();
		assertNull(out);

		// Complete with an empty structure
		out = new Promise(new Tree()).waitFor();
		assertNotNull(out);
		assertTrue(out.isEmpty());
		
		// Complete with a structure
		Promise promise = new Promise();
		promise.complete(new Tree().put("a", 3));
		out = promise.waitFor();
		assertEquals(3, out.get("a", 1));
		
		// Handle exception (completed)
		promise = Promise.reject().then(in -> {
			return null;
		}).catchError(err -> {
			return "xyz";
		}).then(in -> {
			return in.asString() + "1";
		});
		out = promise.waitFor();
		assertEquals("xyz1", out.asString());

		// Handle exception (uncompleted)
		promise = new Promise().then(in -> {
			return "x";
		}).catchError(err -> {
			return "y";
		});
		promise.complete(new Exception());
		out = promise.waitFor();
		assertEquals("y", out.asString());
		
		// Change value (completed)
		promise = new Promise("y").then(in -> {
			return in.asString() + "x";
		});
		out = promise.waitFor();
		assertEquals("yx", out.asString());

		// Change value (uncompleted)
		promise = new Promise().then(in -> {
			return in.asString() + "x";
		});
		promise.complete("z");
		out = promise.waitFor();
		assertEquals("zx", out.asString());

		// Change value (in constructor)
		promise = new Promise(rsp -> {
			rsp.resolve("y");
		}).then(in -> {
			return in.asString() + "x";
		});
		out = promise.waitFor();
		assertEquals("yx", out.asString());
	}

	@Test
	public void testWaterfall() throws Exception {
		Promise promise = new Promise().then(in -> {
			
			// Scalar processing - return int
			return in.asInteger() + 1;
			
		}).then(in -> {
			
			// Scalar processing - return String
			return in.asString() + "X";
			
		}).then(in -> {
			
			// Generating the first error
			if (in.asString().equals("6X")) {
				throw new IllegalArgumentException("Sample error");
			}
			return "OK";
			
		}).catchError(err -> {
			
			// Handle error
			if (err.toString().contains("Sample error")) {
				return 7;
			}
			return -1;
			
		}).then(in -> {
			
			// Processing error handler's result
			return in.asInteger() * 2;
			
		}).then(in -> {
			
			// Second error
			if (in.asInteger() == 14) {
				throw new IllegalStateException("Another error");
			}
			return -1;
			
		}).catchError(err -> {
			
			// Second error handler
			if (err.toString().contains("Another error")) {
				return 5;
			}
			return 0;

		}).then(in -> {
			
			// Generating JSON structure
			Tree out = new Tree();
			out.put("num", in.asInteger());
			out.put("str", "abc");
			out.put("bool", "true");
			return out;
			
		}).then(in -> {
			
			// Modify and forward structure
			in.put("str", "xyz");
			return in;
			
		}).then(in -> {
			
			// Do nothing, just check the input
			assertEquals("xyz", in.get("str", ""));
			
		});
		
		// Start waterfall
		promise.complete(5);
		
		// Wait for result (in blocking style)
		Tree result = promise.waitFor();

		// Check result
		assertEquals(5, result.get("num", -1));
		assertTrue(result.get("bool", false));
	}
	
	@Test
	public void testResolve() throws Exception {
		Tree out;
		
		out = Promise.resolve(123).waitFor();
		assertEquals(123, (int) out.asInteger());

		out = Promise.resolve(true).waitFor();
		assertTrue(out.asBoolean());

		out = Promise.resolve(false).waitFor();
		assertFalse(out.asBoolean());

		out = Promise.resolve("abc").waitFor();
		assertEquals("abc", out.asString());

		Tree in = new Tree();
		in.put("a.b", "c");
		out = Promise.resolve(in).waitFor();
		assertEquals("c", out.get("a.b", "x"));
	
		Promise p = Promise.resolve(456);
		out = Promise.resolve(p).waitFor();
		assertEquals(456, (int) out.asInteger());
	
		p = new Promise();
		p.complete(new Tree());
		out = Promise.resolve(p).waitFor();
		assertTrue(out.isStructure());
	}
	
	@Test
	public void testInvalidInit() throws Exception {
		Promise p = new Promise(r -> {
			throw new Exception("error");
		}).catchError(err -> {
			return 123;
		});
		Tree out = p.waitFor();
		assertEquals(123, (int) out.asInteger());		
	}

	@Test
	public void testExceptionInFunction() throws Exception {
		Promise p1 = Promise.resolve(0).then(in -> {
			if (in.asInteger() == 0) {
				throw new IllegalArgumentException("test");
			}
			return 1;
		}).catchError(err -> {
			return 2;
		});
		Tree out1 = p1.waitFor();
		assertEquals(2, (int) out1.asInteger());
		
		Promise p2 = Promise.resolve(1).then(in -> {
			if (in.asInteger() == 0) {
				throw new IllegalArgumentException("test");
			}
			return 1;
		}).catchError(err -> {
			return 2;
		});
		Tree out2 = p2.waitFor();
		assertEquals(1, (int) out2.asInteger());
	}

	@Test
	public void testExceptionInConsumer() throws Exception {
		Promise p1 = Promise.resolve(0).then(in -> {
			if (in.asInteger() == 0) {
				throw new IllegalArgumentException("test");
			}
		}).catchError(err -> {
			return 2;
		});
		Tree out1 = p1.waitFor();
		assertTrue(p1.isDone());
		assertTrue(p1.isResolved());
		assertFalse(p1.isRejected());
		assertEquals(2, (int) out1.asInteger());
		
		Promise p2 = Promise.resolve(1).then(in -> {
			if (in.asInteger() == 0) {
				throw new IllegalArgumentException("test");
			}
		}).catchError(err -> {
			return 2;
		});
		Tree out2 = p2.waitFor();
		assertEquals(1, (int) out2.asInteger());
		assertTrue(p2.isDone());
		assertTrue(p2.isResolved());
		assertFalse(p2.isRejected());		
	}


	@Test
	public void testExceptionInErrorHandlerFunction() throws Exception {
		Tree out1 = Promise.resolve(0).then(in -> {
			if (in.asInteger() == 0) {
				throw new IllegalArgumentException("test1");
			}
		}).catchError(err -> {
			if (err.toString().contains("test1")) {
				throw new IllegalArgumentException("test2");
			}
			return 2;
		}).catchError(err -> {
			return 3;
		}).waitFor();
		assertEquals(3, (int) out1.asInteger());		
	}

	@Test
	public void testExceptionInErrorHandlerConsumer() throws Exception {
		Tree out1 = Promise.resolve(0).then(in -> {
			if (in.asInteger() == 0) {
				throw new IllegalArgumentException("test1");
			}
		}).catchError(err -> {
			if (err.toString().contains("test1")) {
				throw new IllegalArgumentException("test2");
			}
		}).catchError(err -> {
			return 3;
		}).waitFor();
		assertEquals(3, (int) out1.asInteger());		
	}

	@Test
	public void testRejected() throws Exception {
		Promise p = new Promise().then(in -> {
			if (in.asInteger() == 0) {
				throw new IllegalArgumentException("test1");
			}
			return "foo";
		});
		try {
			p.complete(0);
		} catch (Exception ignored) {
			ignored.printStackTrace();
		}
		assertTrue(p.isRejected());
	}
	
	@Test
	public void testSimpleComplete() throws Exception {
		Promise p = Promise.resolve();
		Tree out = p.waitFor();
		assertNull(out);
		assertTrue(p.isDone());
		assertTrue(p.isResolved());
		assertFalse(p.isRejected());
	}
	
}