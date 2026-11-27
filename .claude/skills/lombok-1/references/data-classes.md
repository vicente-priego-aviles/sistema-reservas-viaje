# Data classes: @Getter/@Setter, @Data, @Value, @With, @Accessors, @FieldDefaults

Read this when you write or review a class that holds data with Lombok: accessors, lazy getters, `@Data`, `@Value`, `@With`, accessor naming (`@Accessors`) or field modifiers (`@FieldDefaults`). Constructors and `@NonNull` are in `constructors-and-nulls.md`.

## Contents
- [@Getter and @Setter](#getter-and-setter)
- [@Getter(lazy=true)](#getterlazytrue)
- [@Data](#data)
- [@Value](#value)
- [@With](#with)
- [@Accessors (experimental)](#accessors-experimental)
- [@FieldDefaults (experimental)](#fielddefaults-experimental)
- [Choosing: @Data, @Value, @Getter+@Setter, or a record](#choosing-data-value-gettersetter-or-a-record)
- [Beyond the Lombok docs](#beyond-the-lombok-docs)

## @Getter and @Setter

Generates `getFoo()` (or `isFoo()` for a `boolean` field) returning the field, and `void setFoo(T foo)` assigning it. Both are `public` unless you pass an `AccessLevel`.

```java
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class Person {
    @Getter @Setter private int age = 10;
    @Setter(AccessLevel.PROTECTED) private String name;
}
```

- **Access levels**: `PUBLIC`, `PROTECTED`, `PACKAGE`, `PRIVATE`. `AccessLevel.NONE` generates nothing; it is only useful to exclude a field from `@Data` or from a class-level `@Getter`/`@Setter`.
- **On the class**: same as annotating every non-static field. A field-level annotation takes precedence over the class-level one.
- **Enums**: `@Getter` works on enums; `@Setter` does not (on purpose: setters on enums are "an extremely bad idea").
- **Annotations on the generated code**: `onMethod=@__({@AnnotationsHere})` for the method, `onParam=@__({@AnnotationsHere})` for the setter's parameter. Experimental: see `experimental-and-misc.md` (onX).
- **Javadoc** (since v1.12.0 per the page): field javadoc is copied; `@return` is moved to the getter and `@param` to the setter (removed from the field). For separate text, add a section line `-- GETTER --` or `-- SETTER --` (2 or more dashes on each side, nothing else on the line); inside a section, `@return`/`@param` are no longer moved, so put them in the section.

Small print:
- **Naming**: the first character is title-cased if lowercase, then `get`/`set`/`is` is prefixed. A `boolean` field named `isFoo` (`is` + title-case letter) gets getter `isFoo()`, no extra prefix. Only primitive `boolean` uses `is`: `java.lang.Boolean` gets `get`.
- **Existing methods**: no method is generated if a method with the same name (case insensitive) and the same parameter count already exists, even with different parameter types (`getFoo(String... x)` blocks `getFoo()`; varargs count as 0 to N parameters). Lombok emits a **warning** when it skips. Mark a method `@lombok.experimental.Tolerate` to hide it from Lombok.
- **Null checks**: well-known non-null annotations on the field (such as `javax.annotation.Nonnull`) add an explicit null check in the setter.
- **Copied annotations**: well-known nullability annotations (such as `org.eclipse.jdt.annotation.NonNull`) are copied to the getter method and the setter parameter. Add more with `lombok.copyableAnnotations`.

| Key | Default | Effect |
|---|---|---|
| `lombok.accessors.chain` | `false` | Setters return `this`. An explicit `@Accessors(chain=…)` wins. |
| `lombok.accessors.fluent` | `false` | No `get`/`is`/`set` prefix; method named like the field (minus prefixes). An explicit `@Accessors(fluent=…)` wins. |
| `lombok.accessors.prefix` | empty list | List (`+=` adds, `-=` removes inherited). Field prefixes to strip: with `m`, `mFoobar` → `getFoobar()`. An explicit `@Accessors(prefix=…)` wins. |
| `lombok.getter.noIsPrefix` | `false` | `boolean` getters use `get`; generated code that calls getters (e.g. `@ToString`) also uses `get`. |
| `lombok.accessors.capitalization` | `basic` | `basic`: `uShaped` → `getUShaped`; `beanspec`: `getuShaped`. |
| `lombok.copyableAnnotations` | empty list | Fully qualified annotation types copied from field to getter and setter parameter. |
| `lombok.getter.flagUsage` / `lombok.setter.flagUsage` | not set | `warning` or `error` on every use. |

Source: https://projectlombok.org/features/GetterSetter (Lombok 1.18.48)

## @Getter(lazy=true)

Generates a getter that computes the value once, on first call, and caches it (thread-safe: Lombok does the locking). Since v0.10.

```java
import lombok.Getter;

public class Report {
    @Getter(lazy = true) private final double[] cached = expensive();

    private double[] expensive() { /* costly computation */ return new double[1000000]; }
}
```

Requirements: the field must be `private` and `final`, must have an initializer, and must not be `transient`.

Small print:
- **Always use the getter, never the field**, also inside the class: the field's type is changed into an `AtomicReference`, and its internal encoding (pointing to itself means a computed `null`, `null` means not computed) may change in future versions.
- The expression runs at most once; a `null` result is cached too (no magic marker values). The expression need not be thread-safe.
- If the initializer is complex or contains generics, move it to a private (preferably static) method and call that.
- Other Lombok features such as `@ToString` always call the getter, even with `doNotUseGetters=true`.

| Key | Default | Effect |
|---|---|---|
| `lombok.getter.lazy.flagUsage` | not set | `warning` or `error` on every use. |

Source: https://projectlombok.org/features/GetterLazy (Lombok 1.18.48)

## @Data

Shortcut for `@Getter` on all fields, `@Setter` on all non-final fields, `@ToString`, `@EqualsAndHashCode` and `@RequiredArgsConstructor` (constructor for all final fields plus non-final `@NonNull` fields without initializer).

```java
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class Customer {
    private final String name;                 // getter, constructor parameter
    @Setter(AccessLevel.PACKAGE) private int age;
    private String[] tags;
}
```

What it does not do:
- **No parameters of the bundled annotations** (`callSuper`, `includeFieldNames`, `exclude`…). To change them, add that annotation explicitly next to `@Data` (e.g. `@EqualsAndHashCode(callSuper = true)`); `@Data` defers to it.
- **No constructor if any explicit constructor exists.**
- **No access level control**: all getters and setters are `public`. Override per field or class with explicit `@Getter`/`@Setter`, or suppress with `AccessLevel.NONE`.
- Static fields are skipped entirely; `transient` fields are left out of `equals`/`hashCode`; fields whose name starts with `$` are excluded (include them with an explicit annotation such as `@Getter` or `@ToString` and its `of` parameter).

Interplay with explicit methods: if the class already has a method with the same name and parameter count as one `@Data` would generate, it is not generated, **with no warning or error**, even if the parameter types differ (`equals(AnyType param)` blocks `equals`). The same applies to the constructor, `toString`, `equals` and all getters and setters. `@lombok.experimental.Tolerate` on a method or constructor hides it from Lombok.

`staticConstructor`: `@Data(staticConstructor="of")` makes the constructor private and adds a static factory, so generics are inferred: `Foo.of(5)` instead of `new Foo<Integer>(5)`. Since v1.18.10 the static constructor is also generated if a (private) constructor already exists.

Nullity: well-known nullity annotations cause null checks and are copied to the getter, and to the constructor and setter parameters (see @Getter/@Setter small print).

| Key | Default | Effect |
|---|---|---|
| `lombok.data.flagUsage` | not set | `warning` or `error` on every use. |
| `lombok.noArgsConstructor.extraPrivate` | `false` | If `true`, also generates a private no-args constructor that sets all fields to `null`/`0`/`false`. |

Source: https://projectlombok.org/features/Data (Lombok 1.18.48)

## @Value

Immutable variant of `@Data`. Equivalent to `final @ToString @EqualsAndHashCode @AllArgsConstructor @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE) @Getter`: the class is `final`, fields are `private final`, no setters, a getter per field, and an all-args constructor (except `final` fields initialized in their declaration). Introduced as experimental in v0.11.4; in the main `lombok` package since v0.12.0; no longer implies `@With` since v0.11.8.

```java
import lombok.Value;
import lombok.With;
import lombok.experimental.NonFinal;

@Value
public class Money {
    String currency;            // becomes private final
    long amount;
    @With @NonFinal int version; // not final
}
```

- **Import `lombok.Value`.** `lombok.experimental.Value` is an alias kept from v0.11.4–v0.11.9 and will eventually be removed.
- **Explicit methods**: writing any of the generated methods (e.g. your own `toString`) means that part is not generated, with no warning. **Any explicit constructor**, whatever its parameters, stops the all-args constructor; add `@AllArgsConstructor` to get it anyway.
- **Overriding defaults**: an explicit access level on a field, `@NonFinal` or `@PackagePrivate` (both in `lombok.experimental`) undo private/final for that field; `@NonFinal` on the class removes the class's `final`. `@FieldDefaults` **cannot** undo them. Any bundled part can be tuned by adding its annotation explicitly.
- **`staticConstructor`**: `@Value(staticConstructor="of")` makes the all-args constructor private and adds a public static `of` wrapping it (useful for generics).
- **`@Value` + `@Builder`**: the package-private all-args constructor that `@Builder` wants wins over the public one `@Value` wants. See `builders.md`.
- `@lombok.experimental.Tolerate` hides a method or constructor from Lombok.
- Nullity annotations: same as `@Data` (null checks, copied to getter and constructor parameter).

| Key | Default | Effect |
|---|---|---|
| `lombok.value.flagUsage` | not set | `warning` or `error` on every use. |
| `lombok.noArgsConstructor.extraPrivate` | `false` | If `true`, also generates a private no-args constructor that sets all fields to `null`/`0`/`false`. |

Source: https://projectlombok.org/features/Value (Lombok 1.18.48)

## @With

Generates `withFieldName(newValue)`: returns a clone with that one field changed (an immutable "setter"). The generated method returns `this` when the new value is the same reference (`this.x == x`), otherwise calls the all-args constructor. Introduced as `@Wither` (experimental) in v0.11.4; renamed to `@With` and moved to the core package (`lombok.With`) in v1.18.10.

```java
import lombok.AllArgsConstructor;
import lombok.With;

@AllArgsConstructor
public class Point {
    @With private final int x;
    @With private final int y;
}
// p.withX(5) -> new Point(5, p.y)
```

- **Requirement**: a constructor with **all non-static fields in their lexical order** must exist (from `@AllArgsConstructor`, `@Value`, or hand-written); otherwise compile-time error.
- **Access level**: `@With(level = AccessLevel.PROTECTED)`; default `public`.
- **On the class**: a `with` method for every field, including non-final ones; static fields and fields whose name starts with `$` are skipped. Never generated for static fields.
- **Abstract classes**: generates an abstract method with the right signature.
- **Naming**: first character title-cased if lowercase, prefixed with `with`. `lombok.accessors.prefix` and `lombok.accessors.capitalization` (`basic` → `withUShaped`, `beanspec` → `withuShaped`) apply.
- **Existing methods**: not generated if a method with the same name (case insensitive) and parameter count exists (varargs count as 0 to N); a warning is emitted.
- `onMethod=@__({...})` adds annotations to the method (experimental, onX).
- **Javadoc**: copied; `@param` moved to the with method, `@return` stripped. A `-- WITH --` section gives it its own text.
- **Nullity**: well-known nullity annotations add a null check and are copied to the parameter. With `lombok.addNullAnnotations` set, the method or return type gets a non-null annotation.

| Key | Default | Effect |
|---|---|---|
| `lombok.accessors.prefix` | empty list | Field prefixes stripped to build the name. |
| `lombok.accessors.capitalization` | `basic` | `basic` or `beanspec`. |
| `lombok.with.flagUsage` | not set | `warning` or `error` on every use. |

Source: https://projectlombok.org/features/With (Lombok 1.18.48)

## @Accessors (experimental)

Configures how Lombok generates **and looks for** getters, setters and with-ers. Package `lombok.experimental`. Experimental since v0.11.0; status *neutral* (changes expected, intended to be backwards compatible). Say it is experimental when you suggest it.

```java
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class Settings {
    @Getter @Setter private int age = 10;   // int age(); Settings age(int age)
}
```

| Option | Default | Effect |
|---|---|---|
| `fluent` | `false` | Getter `pepper()`, setter `pepper(T newValue)`; unless set, `chain` becomes `true`. |
| `chain` | `false` (`true` if `fluent=true`) | Setters return `this` instead of `void`. |
| `makeFinal` | `false` | Generated getters, setters and with-ers are `final`. Since v1.18.24. |
| `prefix` | — | Fields must start with one of these prefixes; the first match is stripped to make the base name. An empty string always matches. After a letter prefix the next character must not be lowercase: `pepper` does not match `p`, `pEpper` does (base name `epper`). |

Resolution: field annotation first, then the enclosing type (and each outer type), then `lombok.config` for options not set explicitly ("cascades" since v1.18.24). Note: the same page's experimental notes still say it "does not cascade from field `@Accessors` annotation to the class-level" one; set the option on the field itself when it matters, and check with delombok.

Small print:
- If a prefix list is set and a field does not start with any prefix, Lombok **skips the field entirely** and warns.
- The nearest `@Accessors` is also used by Lombok features that look for getters, such as `@EqualsAndHashCode`.
- The docs *strongly* discourage field prefixes (`fPepper`).

| Key | Default | Effect |
|---|---|---|
| `lombok.accessors.chain` | `false` | Acts as `@Accessors(chain = true)` where not set explicitly. |
| `lombok.accessors.fluent` | `false` | Acts as `@Accessors(fluent = true)` where not set explicitly. |
| `lombok.accessors.makeFinal` | `false` | Acts as `@Accessors(makeFinal = true)` where not set explicitly. Since v1.18.24. |
| `lombok.accessors.prefix` | empty list | List (`+=`/`-=`); acts as `@Accessors(prefix = {…})` where not set explicitly. |
| `lombok.accessors.capitalization` | `basic` | `basic` or `beanspec`. Since v1.18.24. |
| `lombok.accessors.flagUsage` | not set | `warning` or `error` on every use. |

Source: https://projectlombok.org/features/experimental/Accessors (Lombok 1.18.48)

## @FieldDefaults (experimental)

Adds an access modifier and/or `final` to each instance field of the annotated class or enum. Package `lombok.experimental`. Experimental since v0.11.4; status *neutral* ("may not move out of experimental status without changes").

```java
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.experimental.PackagePrivate;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Config {
    String host;                 // private final
    @NonFinal int retries;       // private, not final
    @PackagePrivate int port;    // package-private final
}
```

- `level` only changes fields **without** an access modifier (those that look package-private). Keep one package-private with `@PackagePrivate`.
- `makeFinal=true` makes instance fields `final`; keep one non-final with `@NonFinal`.
- Fields whose name starts with `$` are not touched.

Project-wide keys: `lombok.fieldDefaults.defaultPrivate` / `defaultFinal` change **every field in every class or enum being compiled**, even files with no Lombok in them. The Lombok authors call this possibly "too much magic"; if the feature ever leaves experimental, the annotation will most likely be required. Prefer the annotation per class.

| Key | Default | Effect |
|---|---|---|
| `lombok.fieldDefaults.defaultPrivate` | `false` | Since 1.16.8. Every field becomes `private` unless it has an explicit access modifier, `@PackagePrivate`, or an explicit `@FieldDefaults` overrides. |
| `lombok.fieldDefaults.defaultFinal` | `false` | Since 1.16.8. Every field becomes `final` unless `@NonFinal`, or an explicit `@FieldDefaults` overrides. |
| `lombok.fieldDefaults.flagUsage` | not set | `warning` or `error` on every use. |

Source: https://projectlombok.org/features/experimental/FieldDefaults (Lombok 1.18.48)

## Choosing: @Data, @Value, @Getter+@Setter, or a record

- **Mutable bean/POJO with all the boilerplate** → `@Data`. Remember it cannot take parameters: add `@EqualsAndHashCode(...)`/`@ToString(...)` explicitly when needed, and a hand-written constructor removes its constructor.
- **Immutable value** → `@Value` (class `final`, fields `private final`, all-args constructor); add `@With` for copy-with-change; with `@Builder`, mind that the builder's package-private constructor wins.
- **Only some accessors, or control over equality** → `@Getter` (and `@Setter` where needed) plus the specific annotations you want, instead of `@Data`.
- Lombok on records: `@NonNull` on record components adds checks to the compact constructor (see `constructors-and-nulls.md`).

Source: https://projectlombok.org/features/Data and https://projectlombok.org/features/Value (Lombok 1.18.48)

## Beyond the Lombok docs

- (Not from the Lombok docs) On Java 16+, a `record` gives immutable data with constructor, accessors (`name()`, not `getName()`), `equals`, `hashCode` and `toString` without Lombok; prefer it for simple immutable carriers, and keep `@Value` when you need JavaBean-style getter names, a non-record class, `@With`, or other Lombok features on the type.
- (Not from the Lombok docs) `@Data` on JPA entities is usually a bad idea: generated `equals`/`hashCode`/`toString` over all fields interact badly with lazy associations and changing identifiers; see `spring-jpa-jackson.md`.
- (Not from the Lombok docs) Fluent accessors (`@Accessors(fluent = true)`) break tools that expect JavaBean getters/setters (many serializers, bean binders, expression languages).
