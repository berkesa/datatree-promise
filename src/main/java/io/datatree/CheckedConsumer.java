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

/**
 * Checked "function" without return value. Sample code:
 * 
 * <pre>
 * return Promise.resolve().<b>then((value) -&gt; {</b>
 *   <i>// ...do something without any return value...</i>
 * <b>});</b>
 * </pre>
 */
@FunctionalInterface
public interface CheckedConsumer<T> {

	/**
	 * Performs this operation on the given argument.
	 *
	 * @param in
	 *            the input argument
	 * 
	 * @throws Throwable
	 *             any processing error
	 */
	void accept(T in) throws Throwable;

}