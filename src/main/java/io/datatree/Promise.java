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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import io.datatree.Tree;

/**
 * ES6-like Promise, based on the Java8's CompletableFuture API. A Promise is an
 * object that may produce a single value some time in the future: either a
 * resolved value, or a reason that it's not resolved (e.g., a network error
 * occurred). Promise users can attach callbacks to handle the fulfilled value
 * or the reason for rejection. Sample waterfall processing:<br>
 *
 * <pre>
 * Promise.resolve().then(in -> {
 *
 * 	Tree out = new Tree();
 * 	out.put("a", 1);
 * 	out.put("b", 2);
 * 	return out;
 *
 * }).then(in -> {
 *
 * 	int a = in.get("a", -1);
 * 	int b = in.get("b", -1);
 * 	return a + b;
 *
 * }).then(in -> {
 *
 * 	int sub = in.asInteger();
 *
 * 	// You can return an another Promise:
 * 	return Promise.resolve("OK!");
 *
 * }).then(in -> {
 *
 * 	if (!"OK".equals(in.asString())) {
 * 		throw new Exception("Invalid value!");
 * 	}
 *
 * 	// The "catch" is a protected name in Java,
 * 	// use "catchError" instead:
 *
 * }).catchError(err -> {
 *
 * 	System.out.println("Error: " + err);
 * 	return "foo";
 *
 * });
 * </pre>
 */
public class Promise {

	// --- INTERNAL COMPLETABLE FUTURES ---

	/**
	 * An internal CompletableFuture, what does the working logic of this
	 * Promise.
	 */
	protected final CompletableFuture<Tree> future;

	/**
	 * Root CompletableFuture of the "waterfall" logic.
	 */
	protected final CompletableFuture<Tree> root;

	// --- STATIC CONSTRUCTORS ---

	/**
	 * Returns a Promise object that is resolved with {@code null} value.
	 *
	 * @return new RESOLVED/COMPLETED Promise
	 */
	public static final Promise resolve() {
		return new Promise(CompletableFuture.completedFuture(null));
	}

	/**
	 * Returns a Promise object that is resolved with the given value. Allowed
	 * Object types of the "value" parameter are: Tree, String, int, double,
	 * byte, float, short, long, boolean, byte[], UUID, Date, InetAddress,
	 * BigInteger, BigDecimal, and Java Collections with these types.
	 *
	 * @param value
	 *            value of the new Promise
	 *
	 * @return new RESOLVED/COMPLETED Promise
	 */
	public static final Promise resolve(Object value) {
		return new Promise(toCompletableFuture(value));
	}

	/**
	 * Returns a Promise object that is rejected with the given reason.
	 *
	 * @param error
	 *            error state of the new Promise
	 *
	 * @return new REJECTED/COMPLETED EXCEPTIONALLY Promise
	 */
	public static final Promise reject(Throwable error) {
		CompletableFuture<Tree> future = new CompletableFuture<>();
		future.completeExceptionally(error);
		return new Promise(future);
	}

	/**
	 * Returns a Promise object that is rejected with an empty Exception.
	 *
	 * @param error
	 *            error state of the new Promise
	 *
	 * @return new REJECTED/COMPLETED EXCEPTIONALLY Promise
	 */
	public static final Promise reject() {
		return reject(new IllegalStateException("Promise rejected"));
	}

	// --- PUBLIC CONSTRUCTOR ---

	/**
	 * Creates an empty PENDING/INCOMPLETED Promise.
	 */
	public Promise() {
		root = future = new CompletableFuture<>();
	}

	/**
	 * Creates a Promise with an asynchronous initializer. Sample code:
	 * <pre>
	 * <b>return new Promise((r) -> {</b>
	 *   Tree value = new Tree();
	 *   value.put("a.b.c", 3);
	 *   r.resolve(value);
	 * <b>});</b>
	 * </pre>
	 */
	public Promise(Initializer initializer) {
		root = future = new CompletableFuture<>();
		try {
			initializer.init(new Resolver(future));
		} catch (Throwable cause) {
			future.completeExceptionally(cause);
		}
	}

	/**
	 * Creates a Promise.
	 *
	 * @param value
	 *            Promise, CompletableFuture, Tree, String, int, double, byte,
	 *            float, short, long, boolean, byte[], UUID, Date, InetAddress,
	 *            BigInteger, BigDecimal, and Java Collections with these types.
	 */
	public Promise(Object value) {
		root = future = toCompletableFuture(value);
	}

	// --- PROTECTED CONSTRUCTORS ---

	protected Promise(CompletableFuture<Tree> future, CompletableFuture<Tree> root) {
		this.future = future;
		this.root = root;
	}

	public Promise(Object value, CompletableFuture<Tree> root) {
		future = toCompletableFuture(value);
		this.root = root;
	}
	
	// --- WATERFALL FUNCTION ---

	/**
	 * Promises can be used to unnest asynchronous functions and allows one to
	 * chain multiple functions together - increasing readability and making
	 * individual functions, within the chain, more reusable. Sample code:
	 * <pre>
	 * return Promise.resolve().<b>then(value -> {</b>
	 *   <i>// ...do something...</i>
	 *   return value;
	 * <b>}).then(value -> {</b>
	 *   <i>// ...do something...</i>
	 *   return value;
	 * <b>})</b>.catchError(error -> {
	 *   <i>// ...error handling...</i>
	 *   return new Tree().put("key.subkey", "value");
	 * });
	 * </pre>
	 *
	 * @param action
	 *            next action in the invocation chain (allowed return types:
	 *            Promise, CompletableFuture, Tree, String, int, double, byte,
	 *            float, short, long, boolean, byte[], UUID, Date, InetAddress,
	 *            BigInteger, BigDecimal, and Java Collections with these types)
	 *
	 * @return output Promise
	 */
	public Promise then(CheckedFunction<Tree> action) {
		return new Promise(future.handle((data, error) -> {
			if (error != null) {
				return error;
			}
			try {
				return action.apply(data);
			} catch (Throwable cause) {
				return cause;
			}
		}), root);
	}

	/**
	 * Promises can be used to unnest asynchronous functions and allows one to
	 * chain multiple functions together - increasing readability and making
	 * individual functions, within the chain, more reusable. Sample code:
	 * <pre>
	 * return Promise.resolve().<b>then((value) -> {</b>
	 *   <i>// ...do something without any return value...</i>
	 * <b>});</b>
	 * <pre>
	 * 
	 * @param action
	 *            next action in the invocation chain
	 *
	 * @return output Promise
	 */
	public Promise then(CheckedConsumer<Tree> action) {
		return new Promise(future.handle((data, error) -> {
			if (error != null) {
				return error;
			}
			try {
				action.accept(data);
			} catch (Throwable cause) {
				return cause;
			}
			return data;
		}), root);
	}

	// --- ERROR HANDLER METHODS ---

	/**
	 * The catchError() method returns a Promise and deals with rejected cases
	 * only. Sample:
	 * <pre>
	 * Promise.resolve().then(() -> {
	 *   return 123;
	 * <b>}).catchError(error -> {</b>
	 *   // ...do something, with a return value...
	 *   return 456;
	 * });
	 * </pre>
	 *
	 * @param action
	 *            error handler of the previous "next" handlers
	 *
	 * @return output Promise (allowed return types: Tree, String, int, double,
	 *         byte, float, short, long, boolean, byte[], UUID, Date,
	 *         InetAddress, BigInteger, BigDecimal, and Java Collections with
	 *         these types)
	 */
	public Promise catchError(CheckedFunction<Throwable> action) {
		return new Promise(future.handle((data, error) -> {
			if (error != null) {
				try {
					return action.apply(error);
				} catch (Throwable cause) {
					return cause;
				}
			}
			return data;
		}), root);
	}

	/**
	 * The catchError() method returns a Promise and deals with rejected cases
	 * only. Sample:
	 * <pre>
	 * Promise.resolve().then(() -> {
	 *   return 123;
	 * <b>}).catchError(error -> {</b>
	 *   // ...do something, without a return value...
	 * });
	 * </pre>
	 *
	 * @param action
	 *            error handler of the previous "next" handlers
	 *
	 * @return output Promise
	 */
	public Promise catchError(CheckedConsumer<Throwable> action) {
		return new Promise(future.handle((data, error) -> {
			if (error != null) {
				try {
					action.accept(error);
				} catch (Throwable cause) {
					return cause;
				}
			}
			return data;
		}), root);
	}

	// --- COMPLETE UNRESOLVED / INCOMPLETED PROMISE ---

	/**
	 * If not already completed, sets the value to {@code null}. Sample code:
	 * <pre>
	 * Promise p = new Promise().then(value -> {
	 *   System.out.println("Completed!");
	 *   return value;
	 * });
	 * 
	 * // Invoke chain:
	 * p.complete();
	 * </pre>
	 *
	 * @return {@code true} if this invocation caused this Promise to transition
	 *         to a completed state, else {@code false}
	 */
	public boolean complete() {
		return future.complete(new Tree((Tree) null, null, null));
	}

	/**
	 * If not already completed, sets the value to the given value. Sample code:
	 * <pre>
	 * Promise p = new Promise().then(value -> {
	 *   System.out.println("Received: " + value);
	 *   return value;
	 * });
	 * 
	 * // Invoke chain:
	 * Tree t = new Tree().put("a", "b");
	 * p.complete(t);
	 * </pre>
	 * 
	 * @param value
	 *            the result value (allowed types: Tree, String, int, double,
	 *            byte, float, short, long, boolean, byte[], UUID, Date,
	 *            InetAddress, BigInteger, BigDecimal, and Java Collections with
	 *            these types)
	 *
	 * @return {@code true} if this invocation caused this Promise to transition
	 *         to a completed state, else {@code false}
	 */
	public boolean complete(Object value) {
		return root.complete(toTree(value));
	}

	/**
	 * If not already completed, sets the exception state to the given
	 * exception. Sample code:<br>
	 * <pre>
	 * Promise p = new Promise().catchError((error) -> {
	 *   System.out.println("Received: " + error);
	 *   return null;
	 * });
	 * 
	 * // Invoke chain:
	 * p.complete(new Exception("Foo!"));
	 * </pre>
	 * 
	 * @param error
	 *            the exception
	 *
	 * @return {@code true} if this invocation caused this Promise to transition
	 *         to a completed state, else {@code false}
	 */
	public boolean complete(Throwable error) {
		return root.completeExceptionally(error);
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

	// --- GET THE INTERNAL COMPLETABLE FUTURE ---

	/**
	 * Returns the internal CompletableFuture.
	 *
	 * @return internal CompletableFuture
	 */
	public CompletableFuture<Tree> toCompletableFuture() {
		return future;
	}

	// --- BLOCK CURRENT THREAD ---

	/**
	 * Waits if necessary for this future to complete, and then returns its
	 * result. It's a blocking operation, do not use this method unless it is
	 * absolutely necessary for something. Rather use the "then" and
	 * "catchError" methods.
	 * 
	 * @return result Tree structure
	 * 
	 * @throws InterruptedException
	 *             if the current thread was interrupted while waiting
	 * @throws ExecutionException
	 *             if this future completed exceptionally
	 */
	public Tree waitFor() throws InterruptedException, ExecutionException {
		return future.get();
	}

	// --- PARALLEL ALL / ALLOF FUNCTION ---

	/**
	 * Returns a new Promise that is completed when all of the given Promise
	 * complete. If any of the given Promise complete exceptionally, then the
	 * returned Promise also does so, with a Promise holding this exception as
	 * its cause.
	 *
	 * @param promises
	 *            array of Promises
	 *
	 * @return a new Promise that is completed when all of the given Promises
	 *         complete
	 */
	public static final Promise all(Collection<Promise> promises) {
		if (promises == null || promises.isEmpty()) {
			return Promise.resolve();
		}
		Promise[] array = new Promise[promises.size()];
		promises.toArray(array);
		return all(array);
	}

	/**
	 * Returns a new Promise that is completed when all of the given Promise
	 * complete. If any of the given Promise complete exceptionally, then the
	 * returned Promise also does so, with a Promise holding this exception as
	 * its cause.
	 *
	 * @param promises
	 *            Collection of Promises
	 *
	 * @return a new Promise that is completed when all of the given Promises
	 *         complete
	 */
	public static final Promise all(Promise... promises) {
		if (promises == null || promises.length == 0) {
			return Promise.resolve();
		}

		@SuppressWarnings("unchecked")
		CompletableFuture<Tree>[] futures = new CompletableFuture[promises.length];
		for (int i = 0; i < promises.length; i++) {
			futures[i] = promises[i].future;
		}
		CompletableFuture<Void> all = CompletableFuture.allOf(futures);
		return new Promise((r) -> {
			all.whenComplete((Void, error) -> {
				try {
					if (error != null) {
						r.reject(error);
						return;
					}
					Tree array = new Tree().putList("array");
					for (int i = 0; i < futures.length; i++) {
						array.addObject(futures[i].get());
					}
					r.resolve(array);
				} catch (Throwable cause) {
					r.reject(cause);
				}
			});
		});
	}

	// --- PARALLEL RACE / ANYOF FUNCTION ---

	/**
	 * Returns a new Promise that is completed when any of the given Promises
	 * complete, with the same result. Otherwise, if it completed exceptionally,
	 * the returned Promise also does so, with a CompletionException holding
	 * this exception as its cause.
	 *
	 * @param promises
	 *            Collection of Promises
	 *
	 * @return a new Promise that is completed with the result or exception of
	 *         any of the given Promises when one completes
	 */
	public static final Promise race(Collection<Promise> promises) {
		if (promises == null || promises.isEmpty()) {
			return Promise.resolve();
		}
		Promise[] array = new Promise[promises.size()];
		promises.toArray(array);
		return race(array);
	}

	/**
	 * Returns a new Promise that is completed when any of the given Promises
	 * complete, with the same result. Otherwise, if it completed exceptionally,
	 * the returned Promise also does so, with a CompletionException holding
	 * this exception as its cause.
	 *
	 * @param promises
	 *            array of Promises
	 *
	 * @return a new Promise that is completed with the result or exception of
	 *         any of the given Promises when one completes
	 */
	public static final Promise race(Promise... promises) {
		if (promises == null || promises.length == 0) {
			return Promise.resolve();
		}

		@SuppressWarnings("unchecked")
		CompletableFuture<Tree>[] futures = new CompletableFuture[promises.length];
		for (int i = 0; i < promises.length; i++) {
			futures[i] = promises[i].future;
		}
		CompletableFuture<Object> any = CompletableFuture.anyOf(futures);
		return new Promise((r) -> {
			any.whenComplete((object, error) -> {
				try {
					if (error != null) {
						r.reject(error);
						return;
					}
					r.resolve((Tree) object);
				} catch (Throwable cause) {
					r.reject(cause);
				}
			});
		});
	}

	// --- CONVERTERS ---

	/**
	 * Converts an Object to a (completed or uncompleted) CompletableFuture.
	 * 
	 * @param object input Object
	 * @return object converted to CompletableFuture
	 */
	protected static final CompletableFuture<Tree> toCompletableFuture(Object object) {
		if (object == null) {
			return CompletableFuture.completedFuture(null);
		}
		if (object instanceof CompletableFuture) {
			return ((CompletableFuture<?>) object).thenCompose(Promise::toCompletableFuture);
		}
		if (object instanceof Promise) {
			return ((Promise) object).future;
		}
		if (object instanceof Throwable) {
			CompletableFuture<Tree> future = new CompletableFuture<>();
			future.completeExceptionally((Throwable) object);
			return future;
		}
		if (object instanceof CompletionStage) {
			CompletableFuture<Tree> future = new CompletableFuture<>();
			((CompletionStage<?>) object).handle((value, error) -> {
				if (error == null) {
					future.complete(toTree(value));
				} else {
					future.completeExceptionally(error);
				}
				return null;
			});
			return future;
		}
		return CompletableFuture.completedFuture(toTree(object));
	}

	/**
	 * Converts an Object to a Tree.
	 * 
	 * @param object input Object
	 * @return object converted to Tree
	 */
	@SuppressWarnings("unchecked")
	protected static final Tree toTree(Object object) {
		if (object == null) {
			return new Tree((Tree) null, null, null);
		}
		if (object instanceof Tree) {
			return (Tree) object;
		}
		if (object instanceof Map) {
			return new Tree((Map<String, Object>) object);
		}
		if (object instanceof Collection) {
			return new Tree((Collection<Object>) object);
		}
		return new Tree().setObject(object);
	}

	// --- SUBCLASSES AND INTERFACES ---

	@FunctionalInterface
	public static interface Initializer {

		void init(Resolver resolver) throws Throwable;

	}

	public static final class Resolver {

		private final CompletableFuture<Tree> future;

		private Resolver(CompletableFuture<Tree> future) {
			this.future = future;
		}

		public final void resolve() {
			future.complete(new Tree((Tree) null, null, null));
		}

		/**
		 * Resolve the value of the current Promise with the given value.
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

	}

	@FunctionalInterface
	public static interface CheckedConsumer<IN> {

		/**
		 * Performs this operation on the given argument.
		 *
		 * @param t
		 *            the input argument
		 */
		void accept(IN in) throws Throwable;

	}

	@FunctionalInterface
	public static interface CheckedFunction<IN> {

		/**
		 * Applies this function to the given argument.
		 *
		 * @param t
		 *            the function argument
		 * @return the function result
		 */
		Object apply(IN in) throws Throwable;

	}

}