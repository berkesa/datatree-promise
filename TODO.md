# TODO — Modernize `datatree-promise` to 2.0.0

> **You are the per-project Claude Code instance for `datatree-promise`.** Self-contained file.
> Goal: Gradle/Java 8 → **Maven + JDK 21**, deps upgraded, tests green on **JUnit 5**, legacy files
> removed, version **2.0.0**. Tiny project (5 source files in `io.datatree`): an ES6-like `Promise`
> over `CompletableFuture`.

## Coordinates & facts
- Maven: `com.github.berkesa:datatree-promise`, `jar`, license **Apache-2.0**.
- `name`: *Promise for Java* · `inceptionYear`: 2018
- `url`: https://berkesa.github.io/datatree-promise/ · `scm`: https://github.com/berkesa/datatree-promise.git
- developer: `berkesa` / Andras Berkes / andras.berkes@programmer.net
- **Version → `2.0.0`**.

## Inter-project dependency (PIN to 2.0.0)
- `com.github.berkesa:datatree-core:2.0.0` (was 1.1.2). Build `datatree` first so
  `datatree-core:2.0.0-SNAPSHOT` is in your local `~/.m2`.

## Target versions
| Dependency | Current | Target | Scope |
|---|---|---|---|
| `com.github.berkesa:datatree-core` | 1.1.2 | **2.0.0** | compile |
| `junit:junit` 4.12 | → `junit-jupiter` | **5.x** | test |
| Eclipse `ecj` 4.4.2 | — | **remove** | — |
| Java | 1.8 | **21** | — |

## Steps
1. **`pom.xml`** with metadata above, `<maven.compiler.release>21</maven.compiler.release>`, deps:
   ```xml
   <dependency><groupId>com.github.berkesa</groupId><artifactId>datatree-core</artifactId><version>2.0.0-SNAPSHOT</version></dependency>
   <dependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter</artifactId><version>5.11.4</version><scope>test</scope></dependency>
   ```
   Build: compiler 3.14.0, surefire 3.5.3, and (release profile) sources/javadoc/gpg +
   `central-publishing-maven-plugin:0.9.0`.
2. **Remove ECJ** (`compileJava` fork block + `ecj` config) → javac.
3. **Javadoc:** the old build used `javadoc { failOnError = true }`. On JDK 21 javadoc is stricter —
   either fix all javadoc warnings or set `<doclint>none</doclint>` on `maven-javadoc-plugin`. The
   javadoc jar must still build for publishing.
4. **Tests → JUnit 5.** Migrate `io.datatree.PromiseTest`, `WaterfallTest`, etc. to Jupiter.
   `mvn test` green offline. (No API behavior changes — keep `catchError`, the `then` overloads,
   `all`/`race`, `waitFor`, and the `fillInStackTrace` error-trace helper exactly as-is.)
5. **Cleanup — delete:** `build.gradle`, `settings.gradle`, `gradlew`, `gradlew.bat`, `gradle/`,
   `.gradle/`, `.travis.yml`, `.codacy.yaml`, `.classpath`, `.project`, `.settings/`.
6. **VSCode + .gitignore** (java pack/redhat/maven; exclude `target/`). No `launch.json` (library).
7. **Build & install:** `mvn clean install`, then `mvn clean verify`.
8. **Update `CLAUDE.md`** commands to Maven.

## Definition of done
- `mvn clean verify` green on JDK 21; legacy files gone; JUnit 5; version `2.0.0(-SNAPSHOT)`.
- Depends on `datatree-core:2.0.0(-SNAPSHOT)` only; installed to local `~/.m2`.
- VSCode + .gitignore present; publishing configured (Central Portal).
