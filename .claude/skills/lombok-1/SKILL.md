---
name: lombok-1
description: Lombok 1.18.x (current 1.18.48) small print: what each annotation generates, since-versions, experimental status, lombok.config keys, Maven/Gradle setup (JDK 23+), builders, @Jacksonized, equals/hashCode, logging, Spring/JPA. Use it whenever Java code, a pom or a lombok.config uses Lombok.
license: "MIT (skill text, see LICENSE); Lombok documentation excerpts are MIT, © The Project Lombok Authors — see NOTICE.md"
paths: ["**/*.java", "**/pom.xml", "**/build.gradle", "**/build.gradle.kts", "**/lombok.config"]
metadata:
  lombok-version: "1.18.48"
  line: "1"
---

# Lombok 1.18

Lombok generates code at compile time from annotations. You already know the basics well; where answers go wrong is the **small print**: since which version something exists, which package it lives in today, whether it is experimental, what exactly it requires, and which values a config key accepts. Every fact in `references/` was checked against the official documentation of **Lombok 1.18.48**, and each section ends with its official URL.

## How to work
1. Before stating a version, package, requirement, default or config value, **read the reference for that topic** (table below). Read only the one you need; each is self-contained.
2. If the reference does not cover a detail, say so, and point to the official URL given in its sections rather than guessing. If you state something from memory, say that it is from memory.
3. Keep Lombok facts and your own design advice apart: the Lombok docs say nothing about JPA beyond `canEqual`, nor about DDD or Spring style. That advice is welcome, but label it as yours.
4. To know what Lombok really generates, use delombok instead of assuming (`references/build-setup.md`).

## Need… → read
| You need to… | Read |
|---|---|
| Add Lombok to Maven/Gradle/javac, fix "symbol not found" on JDK 23+, modules, IDE setup, delombok | `references/build-setup.md` |
| Write or review a `lombok.config`, find a config key, forbid features | `references/lombok-config.md` |
| `@Builder`, `@Builder.Default`, `@Singular`, `toBuilder`, `@SuperBuilder`, `@Jacksonized` | `references/builders.md` |
| `@Getter`/`@Setter`, `@Data`, `@Value`, `@With`, lazy getters, `@Accessors`, `@FieldDefaults` | `references/data-classes.md` |
| Constructors (`@NoArgsConstructor`, `@RequiredArgsConstructor`, `@AllArgsConstructor`), `@NonNull`, null annotations | `references/constructors-and-nulls.md` |
| `@EqualsAndHashCode` (`callSuper`, `canEqual`, includes), `@ToString` | `references/equality-and-tostring.md` |
| `@StandardException`, `@SneakyThrows`, `@Cleanup`, `@Synchronized`, `@Locked` | `references/exceptions-resources-concurrency.md` |
| `@Slf4j`, `@Log4j2`, `@CustomLog` and the other loggers | `references/logging.md` |
| Any experimental feature and its status, `@UtilityClass`, `@Delegate`, `onX`, `val`/`var` | `references/experimental-and-misc.md` |
| Lombok with Spring beans, JPA entities, Jackson, MapStruct | `references/spring-jpa-jackson.md` |

## Facts that are often wrong (verified for 1.18.48)
- **Current version: 1.18.48.** When writing a dependency, use it or the version the project's parent/BOM already manages; do not invent one.
- **Maven**: Lombok with `<scope>provided</scope>` **and** in `annotationProcessorPaths` of `maven-compiler-plugin` — **mandatory since JDK 23** (and since JDK 9 when compiling with `module-info.java`).
- **`@SuperBuilder` is in the main package since v1.18.48**: `import lombok.SuperBuilder;`. The `lombok.experimental` one remains for a few versions before being deprecated; its config keys were renamed.
- **Experimental** (warn when you suggest them): `@Accessors`, `@ExtensionMethod`, `@FieldDefaults`, `@Delegate`, `onX`, `@UtilityClass`, `@Helper`, `@FieldNameConstants`, `@Tolerate`, `@Jacksonized`, `@StandardException`. `lombok.experimental.flagUsage` controls all of them at once.
- **`@StandardException`** (experimental; its page says since v1.18.21, the changelog lists it in v1.18.22): the parent class needs a **no-args** constructor and a `(String message)` constructor.
- **`@Jacksonized`** (experimental; its page says since v1.18.14, the changelog lists it in v1.18.16) only acts together with `@Builder`/`@SuperBuilder`. Since 1.18.44 `lombok.jacksonized.jacksonVersion` selects Jackson 2, 3 or both; **without it Lombok generates Jackson 2 annotations and warns** — set it for Spring Boot 4 / Jackson 3.
- **`lombok.equalsAndHashCode.callSuper`** accepts `call`, `skip`, `warn` (default `warn`, which only warns). **`lombok.addNullAnnotations`**: `jspecify` and `checkerframework` are the recommended flavors.

## Rules
1. **`@Builder` on a class** acts like a package-private all-args constructor plus `@Builder` on it, and only works when there are no other constructors (hand-written or generated). If `build()` must run your validation, put `@Builder` **on that constructor**. Hand-written constructors ignore `@Builder.Default`.
2. **Inheritance → `@SuperBuilder` on every class of the hierarchy**, never mixed with `@Builder`; `toBuilder = true` on all or none. Frameworks still need their own no-args constructor.
3. **Subclasses → always set `callSuper`** on `@EqualsAndHashCode` (or the config key). If you write `equals` by hand, also write `canEqual` and `hashCode`.
4. **Never parse `toString()` output**; its format may change between versions.
5. **Experimental features are opt-in**: say they are experimental, and prefer the stable alternative when one exists.
