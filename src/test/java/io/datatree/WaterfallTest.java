/**
 * This software is licensed under the Apache 2 license, quoted below.<br>
 * <br>
 * Copyright 2019 Andras Berkes [andras.berkes@programmer.net]<br>
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

import java.util.concurrent.ForkJoinPool;

import org.junit.Test;

import junit.framework.TestCase;

public class WaterfallTest extends TestCase {

	@Test
	public void testWaterfall() throws Exception {

		final Tree i = new Tree();
		i.put("a", 1);

		Promise p = Promise.resolve(i).then(o -> {

			i.put("c", "foo");

		}).then(o -> {

			int num = i.get("a", 0);
			return asyncMethod1(num);

		}).then(o -> {

			int num = o.asInteger();
			return num + 3;

		}).then(this::asyncMethod2)
		  .then(this::blockingMethod)
		  .then(this::voidMethod);
		
		Tree o = p.waitFor(2000);
		assertEquals(3, o.get("b", 0));
		assertEquals(6, o.get("c", 0));
		assertEquals(123, o.get("d", 0));
	}

	private Promise asyncMethod1(int num) {
		return new Promise(res -> {
			ForkJoinPool.commonPool().execute(() -> {
				res.resolve(num + 2);
			});
		});
	}

	private Promise asyncMethod2(Tree o) {
		return new Promise(res -> {
			ForkJoinPool.commonPool().execute(() -> {
				Tree t = new Tree();
				t.put("b", 3);
				t.put("c", o.asInteger());
				res.resolve(t);
			});
		});
	}

	private Tree blockingMethod(Tree o) {
		o.put("d", 123);
		return o;
	}
	
	private void voidMethod(Tree o) {
		System.out.println(o);
	}
	
}
