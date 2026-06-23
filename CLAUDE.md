# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

`datatree-promise` is a tiny single-package Java 11 library (`io.datatree`) that
implements an ES6-like `Promise` on top of `java.util.concurrent.CompletableFuture`.
Published to Maven Central as `com.github.berkesa:datatree-promise`. The only
runtime dependency is `datatree-core` (the `Tree` data structure), which is the
universal value type flowing through every promise.

## Build & test

The project is built with **Maven** (`pom.xml`); the bytecode target is **Java 11**
(`<maven.compiler.release>11</maven.compiler.release>`); minimum consumer runtime: **JDK 11**.
Build JDK: 17+ (JDK 25 in use).

```
mvn clean verify           # compile + test (the CI / definition-of-done gate)
mvn clean install          # build + install to local ~/.m2 (so dependents resolve it)
mvn test                   # run JUnit 5 tests only
mvn clean package          # compile + test + jar
```

Run a single test class or method:

```
mvn test -Dtest=WaterfallTest
mvn test -Dtest=PromiseTest#testNumbers
```

Sources/javadoc/GPG-signed artifacts and Central Portal publishing live under the
`release` profile (`mvn -Prelease ...`). The javadoc plugin runs with
`<doclint>none</doclint>` so warnings don't break the javadoc jar on JDK 21.

Compilation uses `javac` via `maven-compiler-plugin`.

## Architecture

Five source files, all in `src/main/java/io/datatree/`:

- **`Promise.java`** — the whole public API. Wraps two `CompletableFuture<Tree>`
  fields: `future` (this stage's result) and `root` (the head of the chain).
  `then`/`catchError` build a new `Promise` whose `future` is derived via
  `future.handle(...)`, while threading the **same `root`** through. This split is
  the key design point: `complete(...)` resolves `root` so an externally-created
  pending chain (`new Promise().then(...).then(...)`) can be fired from its head,
  whereas status checks (`isResolved`, `waitFor`) read the tail `future`.
- **`Resolver.java`** — the `r` handed to the `Initializer` lambda
  (`new Promise(r -> { ... r.resolve(value); })`). Bridges async callback code
  into the promise by completing the underlying future.
- **`Initializer.java`** — `@FunctionalInterface` for the constructor lambda.
- **`CheckedFunction` / `CheckedConsumer`** — like `Function`/`Consumer` but their
  apply/accept **throw `Throwable`**. This is why `then`/`catchError` accept
  arbitrary throwing lambdas; any thrown error is caught and turned into an
  exceptionally-completed stage.

### Conventions that matter

- **Everything is a `Tree`.** All resolved values are normalized to `Tree` via
  `toTree(...)` / `toCompletableFuture(...)`. When you `resolve`/`return` an
  `int`, `String`, `Collection`, etc., it is wrapped as `new Tree(null, null, value)`.
  Returning a `Promise`, `CompletableFuture`, or `CompletionStage` flattens
  (composes) rather than nesting. The list of "allowed types" repeated throughout
  the javadoc is: Tree, String, the boxed primitives, byte[], UUID, Date,
  InetAddress, BigInteger, BigDecimal, and Java Collections of those.
- **Use `catchError`, not `catch`** — `catch` is a Java keyword.
- **`then` has two overloads**: `CheckedFunction` (transforms the value) and
  `CheckedConsumer` (side-effect only, passes the input through unchanged). The
  compiler picks based on whether your lambda returns a value.
- **Error stack traces**: every catch site calls the private `fillInStackTrace`
  helper before propagating, so rejection reasons carry a usable trace. Preserve
  this when adding new error paths.
- **`all` / `race`** are the static combinators (`CompletableFuture.allOf` /
  `anyOf`). `all` resolves to a `Tree` list (`putList("array")`) of every result.
- **`waitFor(...)`** unwraps `ExecutionException`, rethrowing the original cause —
  it's blocking and meant only for tests/edges, not normal flow.

When adding a new resolved-value type, follow the existing pattern: add matching
fast overloads in **all three** of `Promise` (static `resolve`, constructor,
`complete`) and `Resolver` (`resolve`) to keep the API symmetric.

## Releasing

Version lives in `pom.xml` (`<version>`); the current line is `2.0.0`. Commits are
tagged by version (e.g. `V1.0.10`).
Publishing goes through the `release` profile: `maven-source-plugin` +
`maven-javadoc-plugin` + `maven-gpg-plugin` (GPG signing) +
`central-publishing-maven-plugin` (Central Portal, `publishingServerId=central`).
The POM metadata (coordinates, licenses, scm, developers, the `datatree-core`
dependency) is maintained directly in `pom.xml` — update it if anything changes.
