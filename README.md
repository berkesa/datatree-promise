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

## Download

If you use Maven, add the following dependency to your pom.xml:

```xml
<dependency>
    <groupId>com.github.berkesa</groupId>
    <artifactId>datatree-promise</artifactId>
    <version>1.0.5</version>
</dependency>
```

[...or download the JARs directly from the Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.berkesa%22)

## Usage

### Creating Promises

The simplest way to create a Promise is by using the `new Promise` constructor which accepts an interface (in the example below with "r") that is given two methods. The first method (named `resolve`) is a function to call with the future value when it's ready; and the second method (named `reject`) is a function to call to complete it with an exception.

```java
Promise p = new Promise(r -> {
   if (/* condition */) {
      r.resolve(/* value */);  // fulfilled successfully
   } else {
      r.reject(/* reason */);  // error, rejected, completed exceptionally
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
   p.complete(new Exception("foo"));  // error, rejected, completed exceptionally
}
```

### Consuming Promises

To consume the Promise - meaning we want to process the Promises value once fulfilled - we attach a handler to the Promise using it's `.then()` method. This method takes a function that will be passed the resolved value of the Promise once it is fulfilled.

```java
Promise p = Promise.resolve(5).then(in -> {
   return in.asInteger() + 1;
});

int out = p.waitFor().asInteger(); // out = 6
```

The following method returns a result asynchronously, via a Promise:

```java
public Promise asyncMethod() {
   return new Promise(r -> {
      ...
      r.resolve(/* value */);
      ...
      r.reject(/* reason */);
      ...
   });
}
```

You can call `asyncMethod()` as follows:

```java
asyncMethod().then(in -> {
   ···
}).catchError(err -> {
   ···
});
```

Chaining methods:

```java
Promise p = Promise.resolve(0)
               .then(this::chainedMethod)
               .then(this::chainedMethod)
               .then(this::chainedMethod);

int out = p.waitFor().asInteger(); // out = 3

public Object chainedMethod(Tree in) throws Exception {
   // You can return with a primitive type, Promise, or Tree
   return in.asInteger() + 1;
}
```

Returning with a complex structure:

```java
Promise p = Promise.resolve().then(in -> {
   Tree out = new Tree();
   out.put("description", "Prime numbers");
   Tree list = out.putList("samples");
   list.add(2).add(3).add(5).add(7).add(11).add(13).add(17);
   return out;
});

String json = p.waitFor().toString();
// Value of 'json':
// {
//  "description":"Prime numbers",
//  "samples":[2, 3, 5, 7, 11, 13, 17]
// }
```

### Handling errors

You can use `.catchError()` for handling errors. Throwing an exception in a Promise will automatically reject that Promise as well. This is the same for `.then()` handlers and their results and return values as well - a thrown error is wrapped in a Promise and treated as a rejection.

```java
Promise p = new Promise().then(in -> {
   if (in.asInteger() == 0) {
      throw new Exception();
   }
   return 5;
}).catchError(err -> {
   return 9;
});

p.complete(0);
p.then(in -> {
   System.out.println(in.asInteger()); // 9
});
```

### Composing Promises

Sometimes we're working with multiple Promises and we need to be able to start our processing when all of them are fulfilled. This is where `Promise.all()` comes in. `Promise.all()` takes an array of Promises and once all of them are fulfilled it fulfills its returned Promise with an array of their fulfilled values.

```java
Promise p = Promise.all(
               Promise.resolve("a"),
               Promise.resolve("b"),
               Promise.resolve("c"));

for (Tree result: p.waitFor()) {
   System.out.println(result.asString());
}
// Output:
// a
// b
// c
```

### A complex sample

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

### More information about ES6 /JavaScript/ Promises

https://www.datchley.name/es6-promises/

## Requirements

The DataTree API requires Java 8.

## License

DataTree is licensed under the Apache License V2, you can use it in your commercial products for free.
