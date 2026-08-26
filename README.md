# Promise for Java

An ES6-style `Promise` for Java, built on top of `java.util.concurrent.CompletableFuture`.
A `Promise` represents a single value that will become available some time in the future —
either a **resolved** value, or a **rejection** reason (for example, a network error). You
attach callbacks to handle the value once it arrives, or to handle the failure, and you can
chain those callbacks into flat, readable asynchronous pipelines instead of deeply nested
ones.

Every value that flows through a promise is a datatree `Tree` (from `datatree-core`), the
universal in-memory document type. Whatever you resolve a promise with — a number, a
`String`, a `Collection`, another `Promise`, or a `CompletableFuture` — is automatically
normalized into a `Tree`, so your handlers always receive a `Tree`.

## Download

```xml
<dependency>
    <groupId>com.github.berkesa</groupId>
    <artifactId>datatree-promise</artifactId>
    <version>2.1.0</version>
</dependency>
```

This pulls in `datatree-core` (the `Tree` type) transitively — you don't need to add it
separately.

## Creating promises

There are three common ways to create a promise.

**1. With an asynchronous initializer.** The constructor receives a `Resolver` (called `r`
below) that exposes `resolve(value)` and `reject(error)`. Call one of them when your async
work finishes — from any thread, at any time. This is the idiomatic way to wrap a
callback-based API into a promise:

```java
Promise p = new Promise(r -> {
    // ...start some asynchronous work...
    if (success) {
        r.resolve(result);                          // fulfilled
    } else {
        r.reject(new IllegalStateException("failed")); // rejected
    }
});
```

**2. With an already-known value.** `Promise.resolve(...)` returns a promise that is already
fulfilled; `Promise.reject(...)` returns one that is already rejected:

```java
Promise ok  = Promise.resolve(1234);
Promise err = Promise.reject(new IOException("connection lost"));
```

**3. Empty, completed later.** Create a pending promise now and complete it from elsewhere
with `complete(...)`:

```java
Promise p = new Promise();

// ...later, possibly on another thread...
p.complete(1234);                       // fulfilled
// or:
p.complete(new Exception("failed"));    // rejected
```

## Consuming promises

Attach a handler with `then(...)`. The handler receives the resolved value as a `Tree`.
There are two flavours, and the compiler picks the right one based on whether your lambda
returns a value.

**Transforming the value** — return something, and it becomes the value of the next promise
in the chain:

```java
Promise p = Promise.resolve(5).then(in -> {
    return in.asInteger() + 1;
});

int out = p.waitFor().asInteger();   // out = 6
```

**Side-effect only** — return nothing, and the input value is passed through unchanged:

```java
Promise.resolve(5).then(in -> {
    System.out.println("Value is " + in.asInteger());
});
```

A handler may return a plain value, a `Tree`, **another `Promise`**, or a
`CompletableFuture`. When you return a promise or future, the chain waits for it and
flattens the result automatically — you never end up with a promise nested inside a promise.

## Chaining (waterfall)

Because every `then(...)` returns a new promise, you can chain steps into a flat, readable
pipeline. This is the main reason promises exist — it "un-nests" asynchronous code and makes
each step independently reusable:

```java
Promise p = Promise.resolve(0)
    .then(this::step)
    .then(this::step)
    .then(this::step);

int out = p.waitFor().asInteger();   // out = 3

// A step can return a primitive, a Tree, or another Promise:
public Object step(Tree in) throws Exception {
    return in.asInteger() + 1;
}
```

A step can return a rich `Tree` just as easily as a primitive:

```java
Promise p = Promise.resolve().then(in -> {
    Tree out = new Tree();
    out.put("description", "Prime numbers");
    out.putList("samples").add(2).add(3).add(5).add(7).add(11);
    return out;
});

String json = p.waitFor().toString();
// {
//   "description":"Prime numbers",
//   "samples":[2, 3, 5, 7, 11]
// }
```

## Error handling

Use `catchError(...)` to deal with rejections. (`catch` is a reserved word in Java, hence
the name.) Any exception thrown inside an initializer or a `then(...)` handler automatically
rejects the chain and jumps straight to the next `catchError(...)`:

```java
Promise p = new Promise().then(in -> {
    if (in.asInteger() == 0) {
        throw new IllegalArgumentException("zero is not allowed");
    }
    return 5;
}).catchError(err -> {
    System.out.println("Recovered from: " + err.getMessage());
    return 9;   // a fallback value the chain continues with
});

p.complete(0);
p.then(in -> System.out.println(in.asInteger()));   // prints 9
```

Like `then`, `catchError` has a value-returning form (provide a fallback value) and a
side-effect-only form (just observe the error).

## Combining promises

`Promise.all(...)` waits for **every** promise to complete and resolves to a `Tree` list of
all the results, in order. If any of them rejects, the combined promise rejects:

```java
Promise p = Promise.all(
    Promise.resolve("a"),
    Promise.resolve("b"),
    Promise.resolve("c"));

for (Tree result : p.waitFor()) {
    System.out.println(result.asString());   // a, b, c
}
```

`Promise.race(...)` settles as soon as the **first** of the given promises settles, adopting
its result or rejection:

```java
Promise fastest = Promise.race(slowCall(), fastCall());
```

Both combinators accept either a varargs array or a `Collection<Promise>`.

## Blocking for a result

`waitFor()` blocks the current thread until the promise settles and returns its `Tree`
result (re-throwing the original exception if it was rejected). Overloads accept a timeout:

```java
Tree result = p.waitFor();                      // wait indefinitely
Tree result = p.waitFor(2000);                  // wait up to 2000 ms
Tree result = p.waitFor(2, TimeUnit.SECONDS);   // wait up to 2 seconds
```

> `waitFor(...)` is a **blocking** call meant for tests, `main()` methods, and the edges of
> your application. In normal asynchronous flow prefer `then(...)` / `catchError(...)` so you
> never tie up a thread.

## Status and interop

Check a promise's state without blocking:

```java
p.isResolved();   // completed normally
p.isRejected();   // completed with an exception
p.isDone();       // completed in any way (resolved, rejected, or cancelled)
```

Need to integrate with code that speaks `CompletableFuture`? Unwrap the underlying future:

```java
CompletableFuture<Tree> future = p.toCompletableFuture();
```

## A complete example

```java
Promise.resolve().then(in -> {

    Tree out = new Tree();
    out.put("a", 1);
    out.put("b", 2);
    return out;

}).then(in -> {

    int a = in.get("a", -1);
    int b = in.get("b", -1);
    return a + b;

}).then(in -> {

    // You can return another Promise — the chain flattens it:
    return Promise.resolve("OK!");

}).then(in -> {

    if (!"OK!".equals(in.asString())) {
        throw new Exception("Invalid value!");
    }

}).catchError(err -> {

    System.out.println("Error: " + err);
    return "fallback";

});
```

> New to the promise model? This is the same mental model as JavaScript/ES6 promises — a
> good primer is <https://www.datchley.name/es6-promises/>.

## Requirements

Java 11 or newer. The only runtime dependency is `datatree-core`.

## License

`datatree-promise` is licensed under the Apache License, Version 2.0 — you can use it in your
commercial products for free.
