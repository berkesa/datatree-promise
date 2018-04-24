[![Build Status](https://travis-ci.org/berkesa/datatree-promise.svg?branch=master)](https://travis-ci.org/berkesa/datatree-promise)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7e4435de02d24edfa313bf8f56d5ea0e)](https://www.codacy.com/app/berkesa/datatree-promise?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=berkesa/datatree-promise&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/berkesa/datatree-promise/branch/master/graph/badge.svg)](https://codecov.io/gh/berkesa/datatree-promise)
[![GitHub license](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/berkesa/datatree-adapters/master/LICENSE)

# Promise for Java

ES6-like Promise, based on the Java8's CompletableFuture API. A Promise is an
object that may produce a single value some time in the future: either a
resolved value, or a reason that it's not resolved (e.g., a network error
occurred). Promise users can attach callbacks to handle the fulfilled value
or the reason for rejection.

Sample "waterfall" processing:

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