# Builders: @Builder, @Singular, @SuperBuilder, @Jacksonized

Covers everything about Lombok builders in 1.18.48. Read it when you add, review or debug `@Builder`, `@Builder.Default`, `@Builder.ObtainVia`, `toBuilder`, `@Singular`, `@SuperBuilder` or `@Jacksonized`, or when you must choose between them.

## Contents
1. [@Builder](#builder)
2. [@Builder.Default and @Builder.ObtainVia](#builderdefault-and-builderobtainvia)
3. [@Singular](#singular)
4. [@SuperBuilder](#superbuilder)
5. [@Jacksonized](#jacksonized)
6. [Choosing: @Builder on class, on constructor, @SuperBuilder, or a record](#choosing-builder-on-class-on-constructor-superbuilder-or-a-record)
7. [Pitfalls](#pitfalls)
8. [Beyond the Lombok docs](#beyond-the-lombok-docs)

## @Builder

Status: main package `lombok` since v1.16.0 (experimental since v0.12.0). Not experimental.

`@Builder` goes on a **class**, a **constructor** or a **method** (the *target*). For a method target Lombok generates:
- an inner static class `FooBuilder` with the same type arguments as the method (the *builder*);
- in the builder: one private non-final field per target parameter, a package-private no-args constructor, one chainable "setter" per parameter (same name and type, returns the builder), `build()` (calls the target with all fields, returns the target's return type) and a `toString()`;
- in the enclosing class: a static `builder()` method that returns a new builder.

What each placement implies:
- **On a method**: `build()` invokes that method. The builder is named after the return type (`VoidBuilder` for `void`). Instance methods are allowed since v1.16.8.
- **On a constructor**: same as a static method whose return type is the class and whose type parameters are the class's. `build()` runs your constructor body, so your validation runs.
- **On a class**: as if you added `@AllArgsConstructor(access = AccessLevel.PACKAGE)` and put `@Builder` on it. **Only works if there is no explicit constructor and no Lombok-generated one** (e.g. `@NoArgsConstructor`). If you have an explicit constructor, put `@Builder` on that constructor instead.
- With `@Value` + `@Builder` on a class, the package-private all-args constructor of `@Builder` wins and suppresses the public one `@Value` would make.

Builder name: `<SimplifiedTitleCasedReturnType>Builder`; `com.yoyodyne.FancyList<T>` gives `FancyListBuilder<T>`. Generic type parameters carry over to the builder (`SingularExampleBuilder<T>`, `public static <T extends Number> SingularExampleBuilder<T> builder()`).

Existing code wins: each generated element is silently skipped if a member with that **name** already exists (parameter counts ignored). If you write the builder class yourself, Lombok injects the missing fields and methods into it. Do not put other method- or constructor-generating Lombok annotations on the builder class (e.g. `@EqualsAndHashCode` is not allowed there).

### Example: value object whose `build()` goes through validation

```java
import java.math.BigDecimal;
import java.util.Objects;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public final class Money {
    private final BigDecimal amount;
    private final String currency;

    @Builder(toBuilder = true)
    private Money(BigDecimal amount, String currency) {
        this.amount = Objects.requireNonNull(amount, "amount");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        this.currency = currency == null ? "EUR" : currency; // defaults live here: @Builder.Default needs @Builder on the class
    }
}

// Money m = Money.builder().amount(new BigDecimal("10.00")).build();
// Money n = m.toBuilder().currency("USD").build();   // validated again
```

### Options

| Option | Default | Effect |
|---|---|---|
| `builderClassName` | return type + `Builder` (or `lombok.builder.className`) | Name of the builder class |
| `buildMethodName` | `"build"` | Name of the build method |
| `builderMethodName` | `"builder"` | Name of the static factory; `""` suppresses it (since v1.18.8) |
| `toBuilder` | `false` | Adds instance method `toBuilder()`: a new builder pre-filled with this instance's values (since v1.16.6) |
| `access` | `public` | Access level of the builder class, `builder()` and the other generated elements (since v1.18.8) |
| `setterPrefix` | none | **Discouraged.** Prefix for builder setters, e.g. `setName(...)` (since v1.18.12) |

All options changed: `@Builder(builderClassName = "HelloWorldBuilder", buildMethodName = "execute", builderMethodName = "helloWorld", toBuilder = true, access = AccessLevel.PRIVATE, setterPrefix = "set")`.

`toBuilder` only applies when the builder produces your own type (always, unless `@Builder` is on a method returning another type). With `toBuilder = true` on a method, every type parameter of the method must also appear in its return type. To keep only `toBuilder()` (copy-constructor use), write `@Builder(toBuilder = true, builderMethodName = "")`; warnings about missing `@Builder.Default` then disappear. `foo.toBuilder().build()` is a **shallow** clone.

Nullity: well-known nullity annotations cause null checks and are copied to the builder setter's parameter. Javadoc on fields is copied to builder setters (since v1.18.6). A deprecated field gives a `@Deprecated` builder setter.

### Config keys

| Key | Default | Effect |
|---|---|---|
| `lombok.builder.className` | `*Builder` | Builder class name unless `builderClassName` is given; `*` is replaced by the return type (since v1.18.10) |
| `lombok.builder.flagUsage` | not set | `warning` or `error` on any `@Builder` |
| `lombok.checkReturnValueAnnotation` | none | `lombok` or `androidx`: annotate `build()` so callers must use the result (since v1.18.48) |

`checkReturnValueAnnotation` and `build()`: added for `@Builder` on classes and constructors (including hand-written constructors; Lombok assumes constructors have no side effects and you cannot override that). For `@Builder` on a method it is **not** added unless that method itself carries an annotation implying "check return value" (including side-effect-free / pure).

Source: https://projectlombok.org/features/Builder (Lombok 1.18.48)

## @Builder.Default and @Builder.ObtainVia

Status: `@Builder.Default` since v1.16.16. Only for **`@Builder` on a class** (fields), not on methods or constructors.

Without it, an unset field gets `0` / `null` / `false` even if the field has an initializer.

```java
@Builder
public class BuilderExample {
    @Builder.Default private long created = System.currentTimeMillis();
    private String name;
}
```

What happens:
- The initializer is **moved into a static method** (`$default$created()`) so it does not run when the value is set in the builder. Therefore the initializer **cannot refer to `this`, `super` or any non-static member**.
- The builder stores it in `created$value` plus a boolean `created$set`. Implementation detail: never touch these fields; inside a custom builder method call the generated setter instead.
- Lombok-generated constructors (e.g. `@NoArgsConstructor`, also `lombok.noArgsConstructor.extraPrivate=true`) use the default (since v1.18.2). **Explicit (hand-written) constructors do not**: set the value yourself or call a generated constructor, e.g. `this();`.
- Works with type parameters (fixed in v1.16.20).

`@Builder.ObtainVia` (for `toBuilder`): put it on fields (`@Builder` on a type) or parameters (on a constructor/method) to say how `toBuilder()` gets the value from the instance, e.g. `@Builder.ObtainVia(method = "calculateFoo")`. It has `@Retention(SOURCE)`.

Source: https://projectlombok.org/features/Builder (Lombok 1.18.48)

## @Singular

Status: since v1.16.0 (with the promotion of `@Builder`); `clear` method since v1.16.8. Works with `@Builder` and `@SuperBuilder`.

Put `@Singular` on a collection field (`@Builder` on class) or parameter (on constructor/method). Instead of a setter, Lombok generates:
- an add-one method (singular name), e.g. `occupation(String)`;
- an add-all method (plural name), e.g. `occupations(Collection<? extends String>)`;
- `clearOccupations()`.
No method that replaces the whole collection is generated. Maps get `elf(key, value)` and `elves(Map<? extends K, ? extends V>)`.

Guarantees: the collection built by `build()` is immutable; calling adders or `clear` after `build()` does not change already built objects; a later `build()` produces a new collection with everything added since the builder was created; the result is compacted.

```java
@Builder
public class SingularExample<T extends Number> {
    private @Singular Set<String> occupations;          // occupation(..), occupations(..), clearOccupations()
    private @Singular("axis") ImmutableList<String> axes; // axis(..), axes(..), clearAxes()
    private @Singular SortedMap<Integer, T> elves;        // elf(k, v), elves(map), clearElves()
    private @Singular Collection<?> minutiae;             // minutia(..), minutiae(..), clearMinutiae()
}
```

Supported types (only these):
- `java.util`: `Iterable`, `Collection`, `List` (compacted unmodifiable `ArrayList`); `Set`, `SortedSet`, `NavigableSet` (unmodifiable `HashSet`/`TreeSet`); `Map`, `SortedMap`, `NavigableMap` (unmodifiable `HashMap`/`TreeMap`). `NavigableMap/Set` need compiling with JDK 1.8+.
- Guava `com.google.common.collect`: `ImmutableCollection`, `ImmutableList`, `ImmutableSet`, `ImmutableSortedSet`, `ImmutableMap`, `ImmutableBiMap`, `ImmutableSortedMap`, `ImmutableTable` (backed by their builders). Guava is used automatically for these types.

Naming:
- Lombok assumes the name is a **common English plural** and singularizes it (`statuses` → `status`).
- Give the singular explicitly: `@Singular("axis") List<Line> axes;`.
- If Lombok cannot singularize or the name is ambiguous, it is a **compile error**: name it explicitly.
- Non-English identifiers: set `lombok.singular.auto = false` and always give the singular explicitly.
- With `setterPrefix = "with"`: `withName` (add one), `withNames` (add many), `clearNames`.

Null collections: the plural method null-checks like `@NonNull` (by default an NPE with a message; since v1.18.12). To ignore a null collection instead (add nothing, return), the Builder page text says `@Singular(ignoreNullCollections = true)`, while its own Jackson example uses `@Singular(nullBehavior = NullCollectionBehavior.IGNORE)`; check which one your IDE resolves. With `lombok.addNullAnnotations` set, the plural method's parameter gets a non-null annotation, or a nullable one when null collections are ignored.

Small print:
- You cannot hand-write any part of a `@Singular` node; if you need control, drop `@Singular` and write it all.
- Sorted collections (`SortedSet`, `NavigableSet`, `SortedMap`, `NavigableMap`, `ImmutableSortedSet`, `ImmutableSortedMap`) need elements with natural order (`Comparable`); there is **no way to pass a `Comparator`**.
- For `java.util` targets the builder stores elements in an `ArrayList` even for sets and maps; not externally visible.
- Type-use annotations on the element type are handled: `@Singular List<@NonNull String> names;`.

| Key | Default | Effect |
|---|---|---|
| `lombok.singular.useGuava` | `false` | `true`: implement `java.util` interfaces with Guava `ImmutableXxx` (Guava must be on the classpath) |
| `lombok.singular.auto` | `true` | `false`: never auto-singularize; missing explicit singular is an error |

Source: https://projectlombok.org/features/Builder and https://projectlombok.org/features/BuilderSingular (Lombok 1.18.48)

## @SuperBuilder

Status: experimental since v1.18.2; `toBuilder` and limited customization since v1.18.4; more customization since v1.18.14; **promoted to the main `lombok` package in v1.18.48**: `import lombok.SuperBuilder;`. The old `lombok.experimental.SuperBuilder` remains for a few versions, then will be deprecated and later removed. The changelog says its config keys were renamed; the 1.18.48 page lists the keys below.

`@SuperBuilder` makes a builder that also sets fields of superclasses. Requirements:
- **Types only** (not constructors or methods).
- **Every superclass** must also have `@SuperBuilder`.
- **Not compatible with `@Builder`.**
- `toBuilder = true` requires `toBuilder = true` on **all superclasses**.
- `lombok.builder.className` must be **the same for the whole hierarchy**.

Generated per annotated class: a **protected constructor taking the builder** (sets the fields from it), and **two builder classes**: abstract `FoobarBuilder` and concrete `FoobarBuilderImpl`, with heavy generics to avoid casts. Supports `@Singular` and `@Builder.ObtainVia` on fields.

Options: `buildMethodName` (`"build"`), `builderMethodName` (`"builder"`), `toBuilder` (`false`), `setterPrefix` (discouraged; since v1.18.16). Example: `@SuperBuilder(buildMethodName = "execute", builderMethodName = "helloWorld", toBuilder = true, setterPrefix = "set")`. The page lists neither `builderClassName` nor `access`.

Customization: you can customize most of the generated code except internal methods (e.g. `self()`). The builder class headers must match exactly what Lombok would generate; use the uncustomized delombok output as reference.

### Example: hierarchy with a JPA-friendly no-args constructor

```java
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SuperBuilder;

@MappedSuperclass
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseEntity {
    @Id @GeneratedValue
    private Long id;
}

@Entity
@Getter
@SuperBuilder(toBuilder = true)          // toBuilder on every level, or on none
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer extends BaseEntity {
    private String name;
}

// Customer c = Customer.builder().name("Ada").build();
```

| Key | Default | Effect |
|---|---|---|
| `lombok.builder.className` | `*Builder` | Builder class name; identical across the whole `@SuperBuilder` hierarchy |
| `lombok.superBuilder.flagUsage` | not set | `warning` or `error` on any `@SuperBuilder` |
| `lombok.singular.useGuava` | `false` | As in `@Singular` |
| `lombok.singular.auto` | `true` | As in `@Singular` |
| `lombok.checkReturnValueAnnotation` | none | Annotates `build()`; constructors are always assumed side-effect free |

Nullity annotations are copied to builder setters as with `@Builder`.

Source: https://projectlombok.org/features/SuperBuilder (Lombok 1.18.48)

## @Jacksonized

Status: **experimental**, since v1.18.14 (the changelog lists it under v1.18.16 because 1.18.14 was broken). Add-on for `@Builder`, `@SuperBuilder` and (since v1.18.40) `@Accessors`. It does **not** change the builder's behavior; a `@Jacksonized @SuperBuilder` stays compatible with plain `@SuperBuilder`s.

With `@Builder`/`@SuperBuilder` it makes Jackson deserialize through the builder. It only acts where there is also a `@Builder` or `@SuperBuilder`; otherwise you get a warning. It does:
- add `@JsonDeserialize(builder = Foobar.FoobarBuilder.class)` (or `FoobarBuilderImpl` for `@SuperBuilder`) to the class; **error if such an annotation already exists**;
- copy Jackson configuration annotations (like `@JsonIgnoreProperties`) from the class to the builder class, so Jackson sees them;
- add `@JsonPOJOBuilder(withPrefix = "")` to the builder; if you set `setterPrefix`, that prefix is used; if you set `buildMethodName`, it is passed to Jackson too;
- for `@SuperBuilder`, make the builder implementation class package-private.

With `@Accessors(fluent = true)` on a **type** it inserts `@JsonProperty` so Jackson uses the fluent getters/setters. `@Jacksonized @Accessors` on fields is not supported. Fields marked `transient` are not serialized (fixed in v1.18.44).

### Example: immutable DTO for Jackson 3

```properties
# lombok.config
lombok.jacksonized.jacksonVersion += 3
```

```java
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)   // copied to the builder by @Jacksonized
public class OrderDto {
    String id;
    @Singular List<String> lines;
}
```

| Key | Default | Effect |
|---|---|---|
| `lombok.jacksonized.flagUsage` | not set | `warning` or `error` on any `@Jacksonized` |
| `lombok.jacksonized.jacksonVersion` | `2` + warning | List key (`+=`), since v1.18.44: which Jackson major's annotations to generate; add both while migrating from 2 to 3 |

**Without `jacksonVersion`, Lombok generates Jackson 2 annotations and warns.** Set it explicitly in every project that uses `@Jacksonized`.

Source: https://projectlombok.org/features/experimental/Jacksonized (Lombok 1.18.48)

## Choosing: @Builder on class, on constructor, @SuperBuilder, or a record

| Situation | Use |
|---|---|
| Simple data carrier, no other constructors, wants `@Builder.Default` | `@Builder` on the class |
| Invariants must hold on every instance; you have or need a hand-written constructor | `@Builder` on that constructor (put defaults in the constructor body) |
| Builder for a factory method or for calling some method | `@Builder` on the method |
| Class hierarchy where the subclass builder must set superclass fields | `@SuperBuilder` on **every** class of the hierarchy |
| Jackson must deserialize an immutable type | add `@Jacksonized` (experimental) and set `lombok.jacksonized.jacksonVersion` |
| Java record | see Beyond the Lombok docs |

Source: https://projectlombok.org/features/Builder (Lombok 1.18.48)

## Pitfalls

1. **`@Builder.Default` + hand-written constructor**: the default is not applied; set it yourself or call a Lombok-generated constructor (`this();`).
2. **`@Builder.Default` initializers cannot use `this`, `super` or non-static members**: they are moved into a static method.
3. **Unset field without `@Builder.Default`** is `0`/`null`/`false`, even if it has an initializer (Lombok warns about missing `@Builder.Default` unless `builderMethodName = ""`).
4. **`@Builder` on a class with any constructor** (hand-written or from `@NoArgsConstructor`, etc.) does not work: move `@Builder` to the constructor.
5. **`@Value` + `@Builder`**: the all-args constructor becomes package-private; there is no public constructor.
6. **`access = AccessLevel.PROTECTED`**: everything generated inside the builder class becomes `public`.
7. **Static import of `builder()`**: a non-star static import fails under javac; use `import static Type.*;` or don't import it.
8. **`toBuilder().build()` is a shallow clone**: mutable contents are shared.
9. **`checkReturnValueAnnotation`** is not applied to `build()` of `@Builder` on a method unless that method is marked check-return-value / side-effect-free / pure.
10. **Same-named members suppress generation** silently (names only, parameter counts ignored); an unrelated method called `builder` disables the generated one.
11. **`@Singular`**: no hand-written parts; non-English names fail auto-singularization; sorted collections cannot take a `Comparator`.
12. **`@SuperBuilder`**: missing on one superclass, mixed with `@Builder`, `toBuilder` not on all levels, or different `lombok.builder.className` in the hierarchy all break it; still imported from `lombok.experimental` in old code.
13. **`@Jacksonized`**: a hand-written `@JsonDeserialize` on the class is an error; forgetting `jacksonVersion` generates Jackson 2 annotations.
14. **`@Builder.Default` builder fields** (`x$value`, `x$set`) are internal: never access them.

Source: https://projectlombok.org/features/Builder (Lombok 1.18.48)

## Beyond the Lombok docs

- (Not from the Lombok docs) JPA requires a no-args constructor (public or protected); `@SuperBuilder` and `@Builder` on a class do not provide one, so add `@NoArgsConstructor(access = AccessLevel.PROTECTED)`. On a class with `@Builder` (not `@SuperBuilder`) this also needs an all-args constructor (`@AllArgsConstructor`), because `@Builder` on a class no longer generates one once another constructor exists.
- (Not from the Lombok docs) `@Jacksonized` is in package `lombok.extern.jackson`.
- (Not from the Lombok docs) Jackson 3 keeps the annotations in `com.fasterxml.jackson.annotation` but moves databind (including `@JsonDeserialize`, `@JsonPOJOBuilder`) to `tools.jackson.databind`; Spring Boot 4 uses Jackson 3, so pick `jacksonVersion` accordingly.
- (Not from the Lombok docs) For a Java record, the canonical constructor already enforces invariants; `@Builder` can be placed on the record to get a builder, and validation goes into the compact constructor.
