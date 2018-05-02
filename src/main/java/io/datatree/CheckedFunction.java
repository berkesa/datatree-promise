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
 * Checked function with a return value. Sample code:
 * 
 * <pre>
 * return Promise.resolve().<b>then(value -&gt; {</b>
 *   <i>// ...do something...</i>
 *   return value;
 * <b>}).then(value -&gt; {</b>
 *   <i>// ...do something...</i>
 *   return value;
 * <b>})</b>.catchError(error -&gt; {
 *   <i>// ...error handling...</i>
 *   return new Tree().put("key.subkey", "value");
 * });
 * </pre>
 */
@FunctionalInterface
public interface CheckedFunction<T> {

	/**
	 * Applies this function to the given argument.
	 *
	 * @param in
	 *            the function argument
	 * 
	 * @return the function result
	 * 
	 * @throws Throwable
	 *             any processing error
	 */
	Object apply(T in) throws Throwable;

}