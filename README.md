[![Build Status](https://travis-ci.org/berkesa/datatree-promise.svg?branch=master)](https://travis-ci.org/berkesa/datatree-promise)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7e4435de02d24edfa313bf8f56d5ea0e)](https://www.codacy.com/app/berkesa/datatree-promise?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=berkesa/datatree-promise&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/berkesa/datatree-promise/branch/master/graph/badge.svg)](https://codecov.io/gh/berkesa/datatree-promise)
[![GitHub license](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/berkesa/datatree-adapters/master/LICENSE)
[![Javadocs](https://www.javadoc.io/badge/com.github.berkesa/datatree-promise.svg)](https://www.javadoc.io/doc/com.github.berkesa/datatree-promise)

# Promise for Java

ES6-like Promise, based on the Java8's CompletableFuture API. A Promise is an
object that may produce a single value some time in the future: either a
resolved value, or a reason that it's not resolved (e.g., a network error
occurred). Promise users can attach callbacks to handle the fulfilled value
or the reason for rejection.

## Usage

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

	int sub = in.asInteger();

	// You can return an another Promise:
	return Promise.resolve("OK!");

}).then(in -> {

	if (!"OK".equals(in.asString())) {
		throw new Exception("Invalid value!");
	}

	// The "catch" is a protected name in Java,
	// use "catchError" instead:

}).catchError(err -> {

	System.out.println("Error: " + err);
	return "foo";

});
```

## Download

If you use Maven, add the following dependency to your pom.xml:

```xml
<dependency>
    <groupId>com.github.berkesa</groupId>
    <artifactId>datatree-promise</artifactId>
    <version>1.0.1</version>
</dependency>
```

[...or download the JARs directly from the Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.berkesa%22)

## More samples

### Creating Promises

The simplest way to create a Promise is by using the `new Promise` constructor which accepts an interface (in the example below with "r") that is given two methods. The first method (named `resolve`) is a function to call with the future value when it's ready; and the second method (named `reject`) is a function to call to complete it with an exception.

```java
Promise p = new Promise(r -> {
   if (/* condition */) {
      r.resolve(/* value */);  // fulfilled successfully
   } else {
      r.reject(/* reason */);  // error, rejected
   }
});
```

You can create an immediately resolved Promise by using the `Promise.resolve()` method.

```java
Promise p = Promise.resolve(1234);
```

You can also create an unresolved Promise by using the parameterless `new Promise` constructor. The Promise can be completed at some time in the future using the `complete` method.

```java
Promise p = new Promise();

if (/* condition */) {
   p.complete(1234);  // fulfilled successfully
} else {
   p.complete(new Exception("foo"));  // error, rejected
}
```

## Requirements

The DataTree API requires Java 8.

## License

DataTree is licensed under the Apache License V2, you can use it in your commercial products for free.
