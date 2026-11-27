# lombok.config: the configuration system (Lombok 1.18.48)

Read this to write or review a `lombok.config`, to find which config key controls a feature and its default, or to forbid features project-wide.

## Contents
1. [How config files work](#how-config-files-work)
2. [Inspecting the configuration](#inspecting-the-configuration)
3. [import](#import)
4. [Global keys](#global-keys)
5. [Dangerous keys: they touch every source file](#dangerous-keys-they-touch-every-source-file)
6. [Recommended root lombok.config](#recommended-root-lombokconfig)
7. [Feature to config keys](#feature-to-config-keys)
8. [Keys that appear only in the changelog](#keys-that-appear-only-in-the-changelog)
9. [Beyond the Lombok docs](#beyond-the-lombok-docs)

## How config files work

The configuration system exists since Lombok 1.14. A `lombok.config` file in any directory applies to all source files in that directory and in all child directories.

- **Hierarchy**: settings closer to the source file win. A key in `/projects/lombok.config` applies to every project below, except where `/projects/MyProject/lombok.config` sets it differently.
- **Root marker**: `config.stopBubbling = true` stops Lombok from looking in parent directories. The docs suggest it for the root of the workspace; put it in the root `lombok.config` of every repository so that files outside the repository (a developer's home, a CI workspace) cannot change the build.
- **Reset to default**: `clear <key>`, for example `clear lombok.val.flagUsage` undoes a parent's setting.
- **Lists** (such as `lombok.accessors.prefix`, `lombok.copyableAnnotations`): `+=` adds an entry, `-=` removes one entry (also an inherited one), `=` replaces the whole list with a single value (shorthand for `clear` + `+=`).
- **Comments**: lines starting with `#`.
- **Precedence over annotations**: an explicit annotation parameter (for example `@ToString(includeFieldNames = …)`, `@Accessors(chain = …)`) wins over the config key.

```properties
# From parent files
lombok.accessors.prefix += m_
lombok.accessors.prefix += f_
# Reset to a single value; same as: clear lombok.accessors.prefix + lombok.accessors.prefix += x_
lombok.accessors.prefix = x_
```

Source: https://projectlombok.org/features/configuration (Lombok 1.18.48)

## Inspecting the configuration

```shell
# Every key supported by this lombok.jar, with documentation; the output is itself a valid lombok.config
java -jar lombok.jar config -g --verbose

# The effective configuration for a directory or source file
java -jar lombok.jar config src/main/java/com/example/Order.java
```

Use the first command to confirm that a key exists in the Lombok version actually used; the key lists in this file are those of 1.18.48. Per the changelog, `config -v <files>` no longer shows files that do not mention the requested keys; add `--non-mentioned` (`-n`) to show them.

Source: https://projectlombok.org/features/configuration (Lombok 1.18.48)

## import

Since Lombok 1.18.12. Imports must come **before any configuration key** in the file; a file may import several others. The imported content behaves as if it were written at the `import` line.

```properties
import ../configuration/model.config      # relative to the importing file (use relative paths in shared projects)
import /etc/lombok/model.config           # absolute (Linux); Windows: import d:/lombok/model.config
import ../deps/lombok-config.jar          # 'lombok.config' at the root of the archive
import ../deps/lombok-config.zip!base/model.config
import <JAVA_PROJECTS>/shared.config      # <NAME> replaced by System.getenv(NAME), if present
import ~/my.config                        # ~ replaced by System.getProperty("user.home", "~")
```

Small print:
- Imported files may have any name and any extension (or none).
- Duplicate entries are effectively ignored; last wins. Each file is processed once per lookup, so importing the same file twice or creating loops is harmless.
- A config file inside an archive can import only files in the same archive.
- Absolute paths, environment variables and `~` suit individuals, not shared projects.

Source: https://projectlombok.org/features/configuration (Lombok 1.18.48)

## Global keys

| Key | Default | Effect |
|---|---|---|
| `config.stopBubbling` | not set | `true`: do not read `lombok.config` files in parent directories. |
| `lombok.addNullAnnotations` | none added | Add nullity annotations where it makes sense: return of generated `toString` and `withX` (never null), the parameter of generated `equals` (may be null), and more. Value: a flavor (below). Since 1.18.12. |
| `lombok.checkReturnValueAnnotation` | none | Annotate generated methods whose result must not be discarded (`withX` methods, `@Builder`'s `build()`). Flavors: `lombok` (`@lombok.CheckReturnValue`) and `androidx` (`@androidx.annotation.CheckResult`). Static analysis tools (IntelliJ, Error Prone, SpotBugs) then warn when callers ignore the result. A future release may make `lombok` the default. Since 1.18.48. |
| `lombok.addLombokGeneratedAnnotation` | not set | `true`: add `@lombok.Generated` to all generated nodes where possible. JaCoCo has built-in support; also useful for other style checkers and code coverage tools. |
| `lombok.addSuppressWarnings` | `true` (adds `@SuppressWarnings("all")`) | `false`: stop adding it, useful with static analyzers such as Checker Framework. |
| `lombok.extern.findbugs.addSuppressFBWarnings` | not set | `true`: add `@SuppressFBWarnings`; FindBugs must be on the compile classpath. |
| `lombok.copyableAnnotations` | empty list | List (`+=`) of fully qualified annotation types copied from the field to the generated getter, setter parameter, constructor parameter. Popular nullable/nonnull annotations are already copied out of the box. |
| `lombok.<feature>.flagUsage` | not set | `warning` or `error` on every use of that feature. |
| `lombok.experimental.flagUsage` | not set | `warning` or `error` on every use of any experimental feature. |

**`lombok.addNullAnnotations` flavors**: `jspecify` (recommended), `checkerframework` (recommended), `jakarta`, `eclipse`, `jetbrains`, `netbeans`, `androidx`, `findbugs`, `spring`, `jml`, `javax` (= JSR305; not recommended), `android.support` (deprecated within Android). Custom: `CUSTOM:fully.qualified.NonNullAnnotation:fully.qualified.NullableAnnotation`; for type-use-only annotations (like Eclipse's and Checker Framework's): `CUSTOM:TYPE_USE:nonnullanno:nullableanno`. The `jspecify` flavor was added in 1.18.38 (changelog). With `checkerframework`, Lombok also adds `@SideEffectFree` to the constructors it generates (since 1.18.48, changelog).

**flagUsage** examples of feature names: `experimental`, `builder`, `sneakyThrows`, `extensionMethod`; the full list is in [Feature to config keys](#feature-to-config-keys).

Source: https://projectlombok.org/features/configuration (Lombok 1.18.48)

## Dangerous keys: they touch every source file

```properties
lombok.fieldDefaults.defaultPrivate = true
lombok.fieldDefaults.defaultFinal = true
```

Both default to `false` and exist since 1.16.8. When on, **every field in every class or enum** in the compiled sources becomes `private` and/or `final`, even in files with no Lombok annotation at all. Exceptions: for `defaultPrivate`, a field with an explicit access modifier or `@PackagePrivate`; for `defaultFinal`, a field with `@NonFinal`; for both, a class with an explicit `@FieldDefaults`. Do not add them to an existing project casually: they silently change the meaning of code that never mentions Lombok (package-private fields become private, mutable fields become final). `@FieldDefaults` is experimental.

Sources: https://projectlombok.org/features/configuration, https://projectlombok.org/features/experimental/FieldDefaults (Lombok 1.18.48)

## Recommended root lombok.config

A starting point for the root of an application repository. Every key and value comes from the docs; **the choice of keys and values is a recommendation**, not a Lombok default. Lines marked "optional" are commented out: enable them only if the condition holds.

```properties
# This is the root: ignore lombok.config files in parent directories (developer home, CI workspace).
config.stopBubbling = true

# Mark all generated code with @lombok.Generated: JaCoCo has built-in support for it, and other coverage and style tools can use it.
lombok.addLombokGeneratedAnnotation = true

# Warn on build() / withX() results that are thrown away (catches "built but never used" bugs). Lombok >= 1.18.48 only.
lombok.checkReturnValueAnnotation = lombok

# Experimental features are less tested and may change or disappear: make each use a visible decision.
lombok.experimental.flagUsage = warning

# @SneakyThrows hides checked exceptions from the compiler: allow it, but make it stand out in the build log.
lombok.sneakyThrows.flagUsage = warning

# onX has 'workaround status': it may disappear without a deprecation period or stop working on future javac. Forbid it.
lombok.onX.flagUsage = error

# Optional: only if JSpecify is on the compile classpath. Adds @NonNull/@Nullable to generated code (toString, withX, equals parameter...).
# lombok.addNullAnnotations = jspecify

# Optional: subclasses with @EqualsAndHashCode/@Data must decide about callSuper; the default (warn) only warns and behaves like skip.
# Set this only if superclass state should take part in equality everywhere (review JPA entities and framework base classes first).
# lombok.equalsAndHashCode.callSuper = call

# Optional: identifiers not in English (e.g. Spanish domain names): turn off automatic singularization in @Singular;
# lombok then requires an explicit singular name instead of guessing a wrong one.
# lombok.singular.auto = false

# Optional: only if @Jacksonized is used (Lombok >= 1.18.44). Choose the Jackson major version to stop the warning;
# without it Lombok generates Jackson 2 annotations and warns. The docs do not spell out the value syntax: check it with java -jar lombok.jar config -g --verbose
# lombok.jacksonized.jacksonVersion += <version>
```

Do **not** put `lombok.fieldDefaults.defaultPrivate` / `defaultFinal` in a shared root file unless the whole team has decided so (see the previous section).

Source: https://projectlombok.org/features/configuration (Lombok 1.18.48)

## Feature to config keys

Every key from the "Supported configuration keys" section of each feature page. Booleans are `true`/`false`; every `flagUsage` key accepts `warning` or `error` and defaults to not set.

| Feature | Key | Values | Default |
|---|---|---|---|
| `@Getter` / `@Setter` | `lombok.getter.flagUsage`, `lombok.setter.flagUsage` | flag | not set |
| | `lombok.getter.noIsPrefix` | boolean (`true`: `boolean` getters use `get`, also in code that calls getters such as `@ToString`) | `false` |
| | `lombok.copyableAnnotations` | list of fully qualified types | empty |
| `@Getter`, `@Setter`, `@With`, `@Accessors` | `lombok.accessors.chain` | boolean (setters return `this`) | `false` |
| | `lombok.accessors.fluent` | boolean (no `get`/`is`/`set` prefix) | `false` |
| | `lombok.accessors.prefix` | list, `+=` / `-=` (field prefixes to strip) | empty |
| | `lombok.accessors.capitalization` | `basic` (`getUShaped`) / `beanspec` (`getuShaped`) | `basic` |
| `@Accessors` (experimental) | `lombok.accessors.makeFinal` | boolean | `false` |
| | `lombok.accessors.flagUsage` | flag | not set |
| `@Getter(lazy=true)` | `lombok.getter.lazy.flagUsage` | flag | not set |
| `@With` | `lombok.with.flagUsage` | flag | not set |
| Constructors | `lombok.anyConstructor.addConstructorProperties` | boolean (adds `@java.beans.ConstructorProperties`; with JPMS needs module `java.desktop`) | `false` |
| | `lombok.allArgsConstructor.flagUsage`, `lombok.requiredArgsConstructor.flagUsage`, `lombok.noArgsConstructor.flagUsage`, `lombok.anyConstructor.flagUsage` (all three) | flag | not set |
| | `lombok.noArgsConstructor.extraPrivate` | boolean (private no-args constructor for `@Data`/`@Value`, fields set to null/0/false) | `false` |
| | `lombok.copyableAnnotations` | list of fully qualified types | empty |
| `@Data` | `lombok.data.flagUsage` | flag | not set |
| `@Value` | `lombok.value.flagUsage` | flag | not set |
| `@EqualsAndHashCode` | `lombok.equalsAndHashCode.callSuper` | `call` / `skip` / `warn` (`warn` = `skip` plus a warning) | `warn` |
| | `lombok.equalsAndHashCode.doNotUseGetters` | boolean | `false` |
| | `lombok.equalsAndHashCode.flagUsage` | flag | not set |
| `@ToString` | `lombok.toString.callSuper` | `call` / `skip` / `warn` | `skip` |
| | `lombok.toString.includeFieldNames` | boolean | `true` |
| | `lombok.toString.doNotUseGetters` | boolean | `false` |
| | `lombok.toString.onlyExplicitlyIncluded` | boolean (`true`: only `@ToString.Include` members) | `false` |
| | `lombok.toString.flagUsage` | flag | not set |
| `@NonNull` | `lombok.nonNull.exceptionType` | `NullPointerException` / `IllegalArgumentException` / `JDK` (`Objects.requireNonNull`) / `Guava` (`Preconditions.checkNotNull`) / `Assertion` (`assert`) | `NullPointerException` |
| | `lombok.nonNull.flagUsage` | flag | not set |
| `@Builder` | `lombok.builder.className` | identifier, `*` = return type name | `*Builder` |
| | `lombok.builder.flagUsage` | flag | not set |
| `@Builder`, `@SuperBuilder` (`@Singular`) | `lombok.singular.useGuava` | boolean (Guava `ImmutableXxx`; Guava must be on the classpath) | `false` |
| | `lombok.singular.auto` | boolean (`false`: singular name must be explicit) | `true` |
| `@SuperBuilder` | `lombok.builder.className` | as above; the whole `@SuperBuilder` hierarchy needs the same value | `*Builder` |
| | `lombok.superBuilder.flagUsage` | flag | not set |
| `@Jacksonized` (experimental) | `lombok.jacksonized.jacksonVersion` | list, `+=` a version; both versions can be set when migrating 2 to 3 (since 1.18.44) | 2, plus a warning |
| | `lombok.jacksonized.flagUsage` | flag | not set |
| `@Cleanup` | `lombok.cleanup.flagUsage` | flag | not set |
| `@SneakyThrows` | `lombok.sneakyThrows.flagUsage` | flag | not set |
| `@Synchronized` | `lombok.synchronized.flagUsage` | flag | not set |
| `@Locked` | `lombok.locked.flagUsage` | flag | not set |
| `val` / `var` | `lombok.val.flagUsage`, `lombok.var.flagUsage` | flag | not set |
| Logging | `lombok.log.fieldName` | identifier | `log` |
| | `lombok.log.fieldIsStatic` | boolean (`false`: instance field) | `true` |
| | `lombok.log.custom.declaration` | `LoggerType LoggerFactoryType.method(params)(params)`, kinds `TYPE`, `NAME`, `TOPIC`, `NULL` (required for `@CustomLog`) | none |
| | `lombok.log.flagUsage` (all loggers), `lombok.log.custom.flagUsage`, `lombok.log.apacheCommons.flagUsage`, `lombok.log.flogger.flagUsage`, `lombok.log.jbosslog.flagUsage`, `lombok.log.javaUtilLogging.flagUsage`, `lombok.log.log4j.flagUsage`, `lombok.log.log4j2.flagUsage`, `lombok.log.slf4j.flagUsage`, `lombok.log.xslf4j.flagUsage` | flag | not set |
| `@FieldDefaults` (experimental) | `lombok.fieldDefaults.defaultPrivate`, `lombok.fieldDefaults.defaultFinal` | boolean, **affect every source file** (since 1.16.8) | `false` |
| | `lombok.fieldDefaults.flagUsage` | flag | not set |
| `@FieldNameConstants` (experimental) | `lombok.fieldNameConstants.innerTypeName` | a string | `Fields` |
| | `lombok.fieldNameConstants.uppercase` | boolean | `false` |
| | `lombok.fieldNameConstants.flagUsage` | flag | not set |
| `@StandardException` (experimental) | `lombok.standardException.addConstructorProperties` | boolean | `false` |
| | `lombok.standardException.flagUsage` | flag | not set |
| `@Delegate`, `@ExtensionMethod`, `@Helper`, `@UtilityClass`, `onX` (experimental) | `lombok.delegate.flagUsage`, `lombok.extensionMethod.flagUsage`, `lombok.helper.flagUsage`, `lombok.utilityClass.flagUsage`, `lombok.onX.flagUsage` | flag | not set |
| All experimental features | `lombok.experimental.flagUsage` | flag | not set |

`@Builder.Singular` and `@Tolerate` have no keys of their own (`@Singular` uses the `lombok.singular.*` keys above).

Sources: the feature pages under https://projectlombok.org/features/ and https://projectlombok.org/features/experimental/all (Lombok 1.18.48)

## Keys that appear only in the changelog

Not on any feature page; verify with `java -jar lombok.jar config -g --verbose` before relying on them.
- `lombok.copyJacksonAnnotationsToAccessors = true`: from 1.18.16 to 1.18.38 Lombok copied certain Jackson annotations (such as `@JsonProperty`) from fields to getters/setters; since 1.18.40 it no longer does, and this key restores the old behavior.
- `lombok.addJavaxGeneratedAnnotation`: defaults to `false` (changed because JDK 9 broke `@javax.annotation.Generated`). Its older name `lombok.addGeneratedAnnotation` is deprecated.
- `lombok.anyConstructor.suppressConstructorProperties`: deprecated, replaced by `lombok.anyConstructor.addConstructorProperties`.
- `checkerframework = true`: an experiment with Checker Framework to detect improper builder use (such as calling `build()` before setting a mandatory property).
- When an experimental feature is promoted (`@Wither` to `@With`, `@SuperBuilder` in 1.18.48), its config keys are renamed: update old `lombok.config` files.

Source: https://github.com/projectlombok/lombok/blob/b48657c83ce44d027984f539bd36048293ce5e8e/doc/changelog.markdown (Lombok 1.18.48)

## Beyond the Lombok docs

- (Not from the Lombok docs) `lombok.config` is read at compile time from the source directories, so commit it to version control; it is not packaged into the jar and has no effect at runtime.
- (Not from the Lombok docs) Nullity annotation flavors refer to annotations from third-party libraries; that library must be on the compile classpath, or the generated code does not compile.
- (Not from the Lombok docs) Changing `lombok.config` may not trigger recompilation in incremental builds or IDEs; do a clean build after editing it.
- (Not from the Lombok docs) Do not set `lombok.accessors.fluent` or `lombok.accessors.chain` project-wide unless every framework in use (bean mappers, JPA providers, template engines) accepts non-JavaBean accessors.
