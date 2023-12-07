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

import static io.datatree.Promise.toCompletableFuture;

import java.net.InetAddress;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous initializer. Sample code:
 * 
 * <pre>
 * <b>return new Promise(resolver -&gt; {</b>
 *   Tree value = new Tree();
 *   value.put("a.b.c", 3);
 *   resolver.resolve(value);
 * <b>});</b>
 * </pre>
 */
public final class Resolver {

	// --- VARIABLES ---
	
	private final CompletableFuture<Tree> future;

	// --- CONSTRUCTOR ---
	
	protected Resolver(CompletableFuture<Tree> future) {
		this.future = future;
	}

	// --- STATUS ---

	/**
	 * Returns {@code true} if this Promise completed exceptionally, in any way.
	 *
	 * @return {@code true} if this Promise completed exceptionally
	 */
	public boolean isRejected() {
		return future.isCompletedExceptionally();
	}

	/**
	 * Returns {@code true} if this Promise completed normally, in any way.
	 *
	 * @return {@code true} if this Promise completed normally
	 */
	public boolean isResolved() {
		return future.isDone() && !future.isCompletedExceptionally() && !future.isCancelled();
	}

	/**
	 * Returns {@code true} if this Promise completed in any fashion: normally,
	 * exceptionally, or via cancellation.
	 *
	 * @return {@code true} if completed
	 */
	public boolean isDone() {
		return future.isDone();
	}
	
	// --- RESOLVE / REJECT ---
	
	public final void resolve() {
		future.complete(new Tree((Tree) null, null, null));
	}

	/**
	 * Resolves the value of the current Promise with the given value.
	 * Allowed Object types of the "value" parameter are: Tree, String, int,
	 * double, byte, float, short, long, boolean, byte[], UUID, Date,
	 * InetAddress, BigInteger, BigDecimal, and Java Collections with these
	 * types.
	 *
	 * @param value
	 *            value of the current Promise
	 */
	public final void resolve(Object value) {
		toCompletableFuture(value).handle((data, error) -> {
			if (error == null) {
				future.complete(data);
			} else {
				future.completeExceptionally(error);
			}
			return data;
		});
	}

	public final void reject(Throwable error) {
		future.completeExceptionally(error);
	}

	// --- FAST RESOLVERS FOR BASIC TYPES ---

	/**
	 * Resolves the value of the current Promise with the given numeric
	 * value.
	 * 
	 * @param value
	 *            the value of the Promise
	 */
	public final void resolve(Number value) {
		future.complete(new Tree((Tree) null, null, value));
	}

	/**
	 * Resolves the value of the current Promise with the given boolean
	 * value.
	 * 
	 * @param value
	 *            the value of the Promise
	 */
	public final void resolve(boolean value) {
		future.complete(new Tree((Tree) null, null, value));
	}

	/**
	 * Resolves the value of the current Promise with the given byte array.
	 * 
	 * @param value
	 *            the value of the Promise
	 */
	public final void resolve(byte[] value) {
		future.complete(new Tree((Tree) null, null, value));
	}

	/**
	 * Resolves the value of the current Promise with the given text.
	 * 
	 * @param value
	 *            the value of the Promise
	 */
	public final void resolve(String value) {
		future.complete(new Tree((Tree) null, null, value));
	}

	/**
	 * Resolves the value of the current Promise with the given date.
	 * 
	 * @param value
	 *            the value of the Promise
	 */
	public final void resolve(Date value) {
		future.complete(new Tree((Tree) null, null, value));
	}

	/**
	 * Resolves the value of the current Promise with the given UUID.
	 * 
	 * @param value
	 *            the value of the Promise
	 */
	public final void resolve(UUID value) {
		future.complete(new Tree((Tree) null, null, value));
	}

	/**
	 * Resolves the value of the current Promise with the given InetAddress.
	 * 
	 * @param value
	 *            the value of the Promise
	 */
	public final void resolve(InetAddress value) {
		future.complete(new Tree((Tree) null, null, value));
	}

	/**
	 * Resolves the value of the current Promise with the given Tree.
	 * 
	 * @param value
	 *            the value of the Promise
	 */
	public final void resolve(Tree value) {
		future.complete(value == null ? new Tree((Tree) null, null, null) : value);
	}

}
