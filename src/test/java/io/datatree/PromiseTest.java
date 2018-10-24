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

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import junit.framework.TestCase;

public class PromiseTest extends TestCase {

	@Test
	public void testNumbers() throws Exception {
		
		// Number
		assertEqualsNumber(new Promise(123L), 123L);
		assertEqualsNumber(Promise.resolve(123L), 123L);
		assertEqualsNumber(new Promise(r -> {
			r.resolve(123L);
		}), 123L);
		Promise p = new Promise();
		p.complete(123L);
		assertEqualsNumber(p, 123L);

	}
	
	@Test
	public void testBooleans() throws Exception {
		
		// boolean
		assertEqualsBoolean(new Promise(true), true);
		assertEqualsBoolean(Promise.resolve(true), true);
		assertEqualsBoolean(new Promise(r -> {
			r.resolve(true);
		}), true);
		Promise p = new Promise();
		p.complete(true);
		assertEqualsBoolean(p, true);
		
		assertEqualsBoolean(new Promise(false), false);
		assertEqualsBoolean(Promise.resolve(false), false);
		assertEqualsBoolean(new Promise(r -> {
			r.resolve(false);
		}), false);
		p = new Promise();
		p.complete(false);
		assertEqualsBoolean(p, false);

	}
	
	@Test
	public void testBytes() throws Exception {
		
		// byte[]
		String txt = "Hello World";
		byte[] bytes = txt.getBytes();
		assertEqualsBytes(new Promise(bytes), bytes);
		assertEqualsBytes(Promise.resolve(bytes), bytes);
		assertEqualsBytes(new Promise(r -> {
			r.resolve(bytes);
		}), bytes);
		Promise p = new Promise();
		p.complete(bytes);
		assertEqualsBytes(p, bytes);

	}
	
	@Test
	public void testStrings() throws Exception {
		
		// String
		String txt = "Hello World";
		assertEqualsString(new Promise(txt), txt);
		assertEqualsString(Promise.resolve(txt), txt);
		assertEqualsString(new Promise(r -> {
			r.resolve(txt);
		}), txt);
		Promise p = new Promise();
		p.complete(txt);
		assertEqualsString(p, txt);
		
	}
	
	@Test
	public void testDates() throws Exception {
		
		// Date
		Date date = new Date();
		assertEqualsDate(new Promise(date), date);
		assertEqualsDate(Promise.resolve(date), date);
		assertEqualsDate(new Promise(r -> {
			r.resolve(date);
		}), date);
		Promise p = new Promise();
		p.complete(date);
		assertEqualsDate(p, date);
		
	}
	
	@Test
	public void testUUIDs() throws Exception {
		
		// UUID
		UUID uuid = UUID.randomUUID();
		assertEqualsUUID(new Promise(uuid), uuid);
		assertEqualsUUID(Promise.resolve(uuid), uuid);
		assertEqualsUUID(new Promise(r -> {
			r.resolve(uuid);
		}), uuid);
		Promise p = new Promise();
		p.complete(uuid);
		assertEqualsUUID(p, uuid);
		
	}
	
	@Test
	public void testInetAddresses() throws Exception {
		
		// InetAddress
		InetAddress address = InetAddress.getLocalHost();
		assertEqualsInetAddress(new Promise(address), address);
		assertEqualsInetAddress(Promise.resolve(address), address);
		assertEqualsInetAddress(new Promise(r -> {
			r.resolve(address);
		}), address);
		Promise p = new Promise();
		p.complete(address);
		assertEqualsInetAddress(p, address);
		
	}
	
	@Test
	public void testTrees() throws Exception {
		
		// Tree
		Tree tree = new Tree().put("a", "b");
		assertEqualsTree(new Promise(tree), tree);
		assertEqualsTree(Promise.resolve(tree), tree);
		assertEqualsTree(new Promise(r -> {
			r.resolve(tree);
		}), tree);
		Promise p = new Promise();
		p.complete(tree);
		assertEqualsTree(p, tree);
		
		// Object
		Object o = new Object();
		p = new Promise();
		p.complete(o);
		assertEquals(o, p.waitFor(5000).asObject());
	}
	
	// Number
	protected void assertEqualsNumber(Promise promise, Number value) throws Exception {
		assertEquals(value, (Number) promise.waitFor(5000).asObject());
	}
	
	// boolean
	protected void assertEqualsBoolean(Promise promise, Boolean value) throws Exception {
		assertEquals(value, (Boolean) promise.waitFor(5000, TimeUnit.MILLISECONDS).asObject());
	}
	
	// byte[]
	protected void assertEqualsBytes(Promise promise, byte[] value) throws Exception {
		byte[] array = (byte[]) promise.waitFor().asObject();
		assertTrue(Arrays.equals(array, value));
	}
	
	// String
	protected void assertEqualsString(Promise promise, String value) throws Exception {
		assertEquals(value, (String) promise.waitFor().asObject());
	}
	
	// Date
	protected void assertEqualsDate(Promise promise, Date value) throws Exception {
		assertEquals(value, (Date) promise.waitFor().asObject());
	}
	
	// UUID
	protected void assertEqualsUUID(Promise promise, UUID value) throws Exception {
		assertEquals(value, (UUID) promise.waitFor().asObject());
	}
	
	// InetAddress
	protected void assertEqualsInetAddress(Promise promise, InetAddress value) throws Exception {
		assertEquals(value, (InetAddress) promise.waitFor().asObject());
	}
	
	// Tree
	protected void assertEqualsTree(Promise promise, Tree value) throws Exception {
		assertEquals(value, (Tree) promise.waitFor());
	}

	@Test
	public void testPromise() throws Exception {

		// Complete with null
		Tree out = Promise.resolve().waitFor();
		assertTrue(out.isNull());

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
		promise.complete(new IllegalArgumentException());
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
	public void testConstructor() throws Exception {
		CompletableFuture<Tree> future = new CompletableFuture<Tree>();
		Promise p = new Promise(future);
		p.complete();
		assertTrue(future != p.toCompletableFuture());
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
			throw new IllegalArgumentException("error");
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

		// Complete in constructor
		Promise p = Promise.resolve();
		Tree out = p.waitFor();
		assertTrue(out.isNull());
		assertTrue(p.isDone());
		assertTrue(p.isResolved());
		assertFalse(p.isRejected());

		// Complete later
		p = new Promise();
		p.complete();
		out = p.waitFor();
		assertTrue(out.isNull());
		assertTrue(p.isDone());
		assertTrue(p.isResolved());
		assertFalse(p.isRejected());
	}

	@Test
	public void testReturnException() throws Exception {
		Promise p1 = Promise.resolve(0).then(in -> {
			if (in.asInteger() == 0) {
				return new IllegalArgumentException("test");
			}
			return 1;
		}).catchError(err -> {
			return 2;
		});
		Tree out1 = p1.waitFor();
		assertTrue(p1.isDone());
		assertTrue(p1.isResolved());
		assertFalse(p1.isRejected());
		assertEquals(2, (int) out1.asInteger());
	}

	@Test
	public void testAllCollections() throws Exception {
		LinkedList<Promise> list = new LinkedList<>();
		AtomicInteger count = new AtomicInteger();
		for (int i = 0; i < 10; i++) {
			list.add(new Promise(r -> {
				new Thread() {
					public void run() {
						try {
							Thread.sleep(10);
							r.resolve(count.incrementAndGet());
						} catch (Exception e) {
							r.reject(e);
						}
					}
				}.start();
			}));
		}

		// Invoke all
		Promise p = Promise.all(list);
		Tree out = p.waitFor();
		assertTrue(out.isEnumeration());
		assertEquals(10, out.size());
		assertTrue(p.isResolved());

		List<Integer> outList = out.asList(Integer.class);
		Collections.sort(outList);
		for (int i = 0; i < 10; i++) {
			assertEquals(i, outList.get(i).intValue() - 1);
		}

		// Null/empty input
		assertTrue(Promise.all((Collection<Promise>) null).waitFor().isNull());
		assertTrue(Promise.all(Collections.emptyList()).waitFor().isNull());
	}

	@Test
	public void testAllArray() throws Exception {
		Promise[] array = new Promise[10];
		AtomicInteger count = new AtomicInteger();
		for (int i = 0; i < 10; i++) {
			array[i] = new Promise(r -> {
				new Thread() {
					public void run() {
						try {
							Thread.sleep(10);
							r.resolve(count.incrementAndGet());
						} catch (Exception e) {
							r.reject(e);
						}
					}
				}.start();
			});
		}

		// Invoke all
		Promise p = Promise.all(array);
		Tree out = p.waitFor();
		assertTrue(out.isEnumeration());
		assertEquals(10, out.size());

		List<Integer> outList = out.asList(Integer.class);
		Collections.sort(outList);
		for (int i = 0; i < 10; i++) {
			assertEquals(i, outList.get(i).intValue() - 1);
		}

		// Null/empty input
		assertTrue(Promise.all(new Promise[0]).waitFor().isNull());
	}

	@Test
	public void testRaceCollections() throws Exception {
		LinkedList<Promise> list = new LinkedList<>();
		AtomicInteger count = new AtomicInteger();
		for (int i = 0; i < 10; i++) {
			list.add(new Promise(r -> {
				new Thread() {
					public void run() {
						try {
							Thread.sleep(10);
							r.resolve(count.incrementAndGet());
						} catch (Exception e) {
							r.reject(e);
						}
					}
				}.start();
			}));
		}

		// Race
		Promise p = Promise.race(list);
		Tree out = p.waitFor();
		assertTrue(out.isPrimitive());
		assertEquals(1, out.size());
		assertTrue(out.asInteger() < 11);
		assertTrue(p.isResolved());

		// Null/empty input
		assertTrue(Promise.race((Collection<Promise>) null).waitFor().isNull());
	}

	@Test
	public void testRaceArray() throws Exception {
		Promise[] array = new Promise[10];
		AtomicInteger count = new AtomicInteger();
		for (int i = 0; i < 10; i++) {
			array[i] = new Promise(r -> {
				new Thread() {
					public void run() {
						try {
							Thread.sleep(10);
							r.resolve(count.incrementAndGet());
						} catch (Exception e) {
							r.reject(e);
						}
					}
				}.start();
			});
		}

		// Race
		Promise p = Promise.race(array);
		Tree out = p.waitFor();
		assertTrue(out.isPrimitive());
		assertEquals(1, out.size());
		assertTrue(out.asInteger() < 11);
		assertTrue(p.isResolved());

		// Null/empty input
		assertTrue(Promise.race(new Promise[0]).waitFor().isNull());
	}

	@Test
	public void testThrowInFunction() throws Exception {
		try {
			Promise.resolve().then(in -> {
				if (in.isNull()) {
					throw new IllegalAccessError("test1");
				}
				return 1;
			}).waitFor();
		} catch (Throwable e) {
			if (e.toString().contains("test1")) {
				return;
			}
		}
		fail();
	}

	@Test
	public void testThrowInConsumer() throws Exception {
		try {
			Promise.resolve().then(in -> {
				if (in.isNull()) {
					throw new IllegalAccessError("test1");
				}
			}).waitFor();
		} catch (Throwable e) {
			if (e.toString().contains("test1")) {
				return;
			}
		}
		fail();
	}

	@Test
	public void testToTree() throws Exception {
		Tree t1 = Promise.toTree(null);
		assertTrue(t1.isNull());

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("a", 3);
		Tree t2 = Promise.toTree(map);
		assertEquals(3, t2.get("a", 1));

		LinkedList<Integer> list = new LinkedList<>();
		list.add(12);
		Tree t3 = Promise.toTree(list);
		assertEquals(12, t3.get(0).asInteger().intValue());
	}

	@Test
	public void testEmptyResolver() throws Exception {
		Tree out = new Promise(r -> {
			r.resolve();
		}).waitFor();
		assertTrue(out.isNull());
	}

	@Test
	public void testThrowInResolver() throws Exception {
		try {
			new Promise(r -> {
				r.reject(new IllegalAccessError("test1"));
			}).waitFor();
		} catch (Throwable e) {
			if (e.toString().contains("test1")) {
				return;
			}
		}
		fail();
	}

	@Test
	public void testResolveWithError() throws Exception {
		try {
			new Promise(r -> {
				r.resolve(new IllegalAccessError("test1"));
			}).waitFor();
		} catch (Throwable e) {
			if (e.toString().contains("test1")) {
				return;
			}
		}
		fail();
	}

}