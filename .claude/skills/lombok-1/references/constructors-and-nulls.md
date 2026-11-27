# Constructors and nulls: @NoArgsConstructor, @RequiredArgsConstructor, @AllArgsConstructor, @NonNull, null annotations

Read this when you generate constructors with Lombok, add `@NonNull` null checks, choose the exception a null check throws, or make Lombok emit `@NonNull`/`@Nullable` annotations on generated code. Getters, `@Data` and `@Value` are in `data-classes.md`.

## Contents
- [Constructor annotations](#constructor-annotations)
- [@NonNull](#nonnull)
- [Null annotations added by Lombok](#null-annotations-added-by-lombok)
- [Where Lombok does NOT check null](#where-lombok-does-not-check-null)
- [Beyond the Lombok docs](#beyond-the-lombok-docs)

## Constructor annotations

Each generates a constructor that assigns one parameter per selected field. Static fields are always skipped.

| Annotation | Parameters | Null checks |
|---|---|---|
| `@NoArgsConstructor` | none | none, even for `@NonNull` fields |
| `@RequiredArgsConstructor` | every uninitialized `final` field + every `@NonNull` field not initialized in its declaration, in field order | for the `@NonNull` fields (throws `NullPointerException`) |
| `@AllArgsConstructor` | every field, in field order | for the `@NonNull` fields |

```java
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")          // private ctor(description) + static of(description)
@AllArgsConstructor(access = AccessLevel.PROTECTED)  // protected ctor(x, y, description)
public class ConstructorExample<T> {
    private int x, y;
    @NonNull private T description;

    @NoArgsConstructor
    public static class NoArgsExample {
        @NonNull private String field;               // not checked by the no-args ctor
    }
}
```

### @NoArgsConstructor
- If a `final` field makes it impossible, compilation fails, unless you use `@NoArgsConstructor(force = true)` (since v1.16.6): all final fields are then initialized with `0` / `false` / `null`.
- **No check is generated for constrained fields such as `@NonNull`**: those constraints are not fulfilled until the fields are initialized later.
- Typical need: frameworks like Hibernate or the Service Provider Interface. Mainly useful together with `@Data` or another constructor annotation.
- Since v1.18.2 it takes `@Builder.Default` values into account.

### @RequiredArgsConstructor
- "Required" = uninitialized `final` fields plus `@NonNull` fields without initializer. Initialized `final` fields and plain non-final fields are not parameters.
- A `@NonNull` field **explicitly initialized to `null`** counts as fulfilled and is **not** a required argument.
- It is the constructor `@Data` generates.

### @AllArgsConstructor
- One parameter per (non-static) field. It is the constructor `@Value` generates, and the one `@With` needs (all non-static fields in lexical order).

### Common options
- **`access`**: `AccessLevel` of the constructor, e.g. `@AllArgsConstructor(access = AccessLevel.PROTECTED)`.
- **`staticName`**: the constructor becomes private and a static factory wrapping it is generated; it infers generics: `MapEntry.of("foo", 5)` instead of `new MapEntry<String, Integer>("foo", 5)`. Example: `@RequiredArgsConstructor(staticName = "of")`.
- **`onConstructor=@__({@AnnotationsHere})`**: annotations on the generated constructor. Experimental (onX), see `experimental-and-misc.md`.
- **Enums**: the annotations work on enums; the constructor is always private (no need for `AccessLevel.PRIVATE`).

### Explicit constructors
Unlike most Lombok annotations, **a hand-written constructor does not stop these three annotations**: Lombok still generates its constructor next to yours. If both end up with the same signature, you get a compiler error. (By contrast, `@Data` and `@Value` generate no constructor when any explicit constructor exists; see `data-classes.md`.)

### @ConstructorProperties
- Not added by default. Set `lombok.anyConstructor.addConstructorProperties = true` to add `@java.beans.ConstructorProperties` to generated constructors. (Default flipped in v1.16.20, when `lombok.anyConstructor.suppressConstructorProperties` was deprecated.)
- Never added to a no-args constructor (so `@NoArgsConstructor` has no `suppressConstructorProperties`), nor to static factory methods (it only goes on real constructors).

### Nullity annotations on fields
Well-known nullity annotations on a field cause null checks in generated constructors and are copied to the constructor parameter. Add more annotations to copy with `lombok.copyableAnnotations`.

| Key | Default | Effect |
|---|---|---|
| `lombok.anyConstructor.addConstructorProperties` | `false` | Adds `@java.beans.ConstructorProperties` to generated constructors. |
| `lombok.allArgsConstructor.flagUsage`, `lombok.requiredArgsConstructor.flagUsage`, `lombok.noArgsConstructor.flagUsage` | not set | `warning` or `error` on every use of that annotation. |
| `lombok.anyConstructor.flagUsage` | not set | `warning` or `error` on any of the three. |
| `lombok.copyableAnnotations` | empty list | Fully qualified annotation types copied from the field to constructor parameter, setter parameter and getter. Popular nullable/nonnull annotations are already copied out of the box. |
| `lombok.noArgsConstructor.extraPrivate` | `false` | If `true`, `@Data` and `@Value` classes also get a private no-args constructor setting all fields to `null`/`0`/`false`. |

The `flagUsage` keys do **not** trigger when the constructor is generated by `@Data`, `@Value` or another Lombok annotation.

Source: https://projectlombok.org/features/constructor (Lombok 1.18.48)

## @NonNull

`@lombok.NonNull` on a **method or constructor parameter** or a **record component** inserts a null check. Since v0.11.10.

```java
import lombok.NonNull;

public class NonNullExample extends Something {
    private String name;

    public NonNullExample(@NonNull Person person) {
        super("Hello");
        // inserted: if (person == null) throw new NullPointerException("person is marked non-null but is null");
        this.name = person.getName();
    }
}
```

Where the check goes:
- **Methods**: at the very top.
- **Constructors**: right after any explicit `this()` or `super()` call.
- **Record components**: in the compact constructor, generated if you have none. If you wrote the record constructor in long form (parameters matching the components), nothing happens: annotate that constructor's parameters instead.
- **Fields**: `@NonNull` (Lombok's or other well-known ones) on a field only matters when Lombok generates the whole method/constructor (setter, `@RequiredArgsConstructor`, `@AllArgsConstructor`, `@Data`, `@With`…).

Small print:
- **Only `lombok.NonNull` triggers the parameter check.** Other libraries' non-null annotations act only on fields, through generated methods.
- **Existing check detection**: if your method already starts with a matching check, Lombok adds none. Counts: an `if (PARAMNAME == null)` whose then-part is a `throw` (braces or not); `assert PARAMNAME != null`; a call to any method named `requireNonNull` or `checkNotNull`, as a standalone statement or as the expression of an assignment or variable declaration. The condition must look exactly like that. The first statement that is not such a check ends the scan.
- **Primitive parameter**: warning, no check.
- **Abstract method parameter**: allowed, no warning since 1.16.8, no code (documentary). `@NonNull` on a method is also allowed, warns nothing and generates nothing.

| Key | Default | Effect |
|---|---|---|
| `lombok.nonNull.exceptionType` | `NullPointerException` | What every Lombok-generated null check does (see below). |
| `lombok.nonNull.flagUsage` | not set | `warning` or `error` on every use of `@NonNull`. |

Values of `lombok.nonNull.exceptionType` (message is always `<name> is marked non-null but is null`):

| Value | Generated check |
|---|---|
| `NullPointerException` (default) | `if (x == null) throw new java.lang.NullPointerException(msg);` |
| `IllegalArgumentException` | same `if`, throwing `IllegalArgumentException` |
| `Assertion` | `assert` statement with the same message (since v1.18.8) |
| `JDK` | `java.util.Objects.requireNonNull(x, msg);` (since v1.18.10) |
| `Guava` | `com.google.common.base.Preconditions.checkNotNull(x, msg);` (since v1.18.10) |

Source: https://projectlombok.org/features/NonNull (Lombok 1.18.48)

## Null annotations added by Lombok

Lombok can annotate generated code with nullity annotations where it makes sense: e.g. the return of generated `toString` and `withX` (never null), and the parameter of generated `equals` (may be null). This matters if your IDE runs strict null checks with "parameters are non-null by default". **Off by default**; since v1.18.12.

```
lombok.addNullAnnotations = jspecify
```

Flavors:
- **Recommended**: `jspecify`, `checkerframework`.
- Others: `jakarta`, `eclipse`, `jetbrains`, `netbeans`, `androidx`, `findbugs`, `spring`, `jml`.
- `javax` (= JSR305): not recommended. `android.support`: deprecated within Android.
- Custom: `CUSTOM:fully.qualified.NonNullAnnotation:fully.qualified.NullableAnnotation`; for type-use-only annotations (like Eclipse's and Checker Framework's): `CUSTOM:TYPE_USE:nonnullanno:nullableanno`.

Per the changelog, it applies to the return value of `toString`, `withX`, chainable `setX`, static constructors, `build`, `builder`, etc., and to the parameter of `equals`, `canEqual`, and the plural form of `@Singular` builder methods (v1.18.12). `jspecify` support was added in v1.18.38; since v1.18.32 the JSpecify package is `org.jspecify.annotations` (was `org.jspecify.nullness`).

Source: https://projectlombok.org/features/configuration (Lombok 1.18.48)

## Where Lombok does NOT check null

- `@NoArgsConstructor` (with or without `force = true`): no check for `@NonNull` fields; a freshly created object can hold `null` there. `force = true` even sets final fields to `null`.
- `lombok.noArgsConstructor.extraPrivate = true`: the extra private constructor sets all fields to `null`/`0`/`false`.
- A `@NonNull` field explicitly initialized to `null` is not a `@RequiredArgsConstructor` parameter, so nothing ever checks it.
- Hand-written methods/constructors: only `@lombok.NonNull` on the **parameter** adds a check; a non-null annotation on the field does not affect your own code.
- Records with a long-form constructor: annotating the component does nothing; annotate the constructor parameter.
- Primitive parameters, abstract methods, and `@NonNull` on a method: no check.
- `lombok.nonNull.exceptionType = Assertion`: the check is an `assert` statement instead of an `if`/`throw`.

Source: https://projectlombok.org/features/constructor and https://projectlombok.org/features/NonNull (Lombok 1.18.48)

## Beyond the Lombok docs

- (Not from the Lombok docs) For Spring beans, prefer `@RequiredArgsConstructor` with `private final` dependencies (constructor injection); a single constructor needs no `@Autowired`.
- (Not from the Lombok docs) For JPA entities, use `@NoArgsConstructor(access = AccessLevel.PROTECTED)` to satisfy the JPA spec without exposing a public no-args constructor; see `spring-jpa-jackson.md`.
- (Not from the Lombok docs) With `exceptionType = Assertion`, checks only run when the JVM has assertions enabled (`-ea`); they are disabled by default.
- (Not from the Lombok docs) `@NonNull` is not Bean Validation: it is a code-level check, unrelated to `jakarta.validation.constraints.NotNull`.
