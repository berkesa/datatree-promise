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

import io.datatree.Tree;
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
		Promise root = new Promise();
		promise = root.then(in -> {
			return "x";
		}).catchError(err -> {
			return "y";
		});
		root.complete(new Exception());
		out = promise.waitFor();
		assertEquals("y", out.asString());
		
		// Change value (completed)
		promise = new Promise("y").then(in -> {
			return in.asString() + "x";
		});
		out = promise.waitFor();
		assertEquals("yx", out.asString());

		// Change value (uncompleted)
		root = new Promise();
		promise = root.then(in -> {
			return in.asString() + "x";
		});
		root.complete("z");
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

}