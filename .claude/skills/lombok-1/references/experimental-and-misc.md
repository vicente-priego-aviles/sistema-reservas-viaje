# Experimental features, `val` and `var`

Covers every feature in `lombok.experimental` (status, risk, how to forbid them) plus `val` and `var`. Read it before using or reviewing any experimental annotation, when deciding whether to allow them in a project, or when you meet `val`/`var` in Lombok code.

## Contents
- [Status of every experimental feature](#status-of-every-experimental-feature)
- [Forbid experimental features](#forbid-experimental-features)
- [Graduated from experimental](#graduated-from-experimental)
- [@UtilityClass](#utilityclass)
- [@Delegate](#delegate)
- [@ExtensionMethod](#extensionmethod)
- [onX (onMethod_, onConstructor_, onParam_)](#onx-onmethod_-onconstructor_-onparam_)
- [@Helper](#helper)
- [@FieldNameConstants](#fieldnameconstants)
- [@Tolerate](#tolerate)
- [val and var](#val-and-var)

## Status of every experimental feature

Experimental features ship in the normal Lombok jar but are tested less, get bugs fixed more slowly, may change their API drastically, and may disappear entirely. Status is quoted from each feature's page.

| Feature | Status (as its page states) | Since | Verdict | Covered in |
|---|---|---|---|---|
| `@Accessors` | neutral | v0.11.0 | Fluent/chained accessors; widely usable, small changes expected. | `data-classes.md` |
| `@ExtensionMethod` | hold | v0.11.2 | Will not leave experimental, but unlikely to change or be removed; high impact on code style and IDE support. | this file |
| `@FieldDefaults` | neutral | v0.11.4 | Default field modifiers; may not leave experimental without changes. | `data-classes.md` |
| `@Delegate` | negative | v0.10 (moved to experimental in v1.14) | Avoid in new code; support may be dropped. | this file |
| `onX` | uncertain | v0.11.8 | "Workaround status": ugly, may break with future javac, can disappear without deprecation. Last resort only. | this file |
| `@UtilityClass` | negative | v1.16.2 | Cannot leave experimental because non-star static imports of its members do not work. | this file |
| `@Helper` | unknown | v1.16.6 | Rarely needed; lambdas are an alternative. | this file |
| `@FieldNameConstants` | neutral | v1.16.22 (redesigned v1.18.4) | Field-name constants for frameworks; still gathering feedback. | this file |
| `@Tolerate` | none stated | v1.14.2 | Makes Lombok ignore an existing method/constructor. | this file |
| `@Jacksonized` | none stated | v1.18.14 (changelog lists it under v1.18.16) | Lets Jackson deserialize through `@Builder`/`@SuperBuilder`; Jackson 3 support since v1.18.44. | `builders.md` |
| `@StandardException` | none stated (page heading reads "TODO") | v1.18.21 (changelog lists it under v1.18.22) | Generates the 4 standard exception constructors. | `exceptions-resources-concurrency.md` |

Source: https://projectlombok.org/features/experimental/all (Lombok 1.18.48)

## Forbid experimental features

To make any use of any experimental feature a warning or a compile error, put this in `lombok.config` (usually the root one, with `config.stopBubbling = true`):

```properties
lombok.experimental.flagUsage = error
```

To forbid only some, use the per-feature keys. All take `warning` | `error` and default to not set.

| Key | Flags |
|---|---|
| `lombok.experimental.flagUsage` | any experimental feature |
| `lombok.accessors.flagUsage` | `@Accessors` |
| `lombok.extensionMethod.flagUsage` | `@ExtensionMethod` |
| `lombok.fieldDefaults.flagUsage` | `@FieldDefaults` |
| `lombok.delegate.flagUsage` | `@Delegate` |
| `lombok.onX.flagUsage` | `onX` (warnings actually emitted only since v1.18.34) |
| `lombok.utilityClass.flagUsage` | `@UtilityClass` |
| `lombok.helper.flagUsage` | `@Helper` |
| `lombok.fieldNameConstants.flagUsage` | `@FieldNameConstants` |
| `lombok.jacksonized.flagUsage` | `@Jacksonized` |
| `lombok.standardException.flagUsage` | `@StandardException` |
| `lombok.val.flagUsage` | `val` (not experimental) |
| `lombok.var.flagUsage` | `var` (not experimental) |

The `@Tolerate` page lists no configuration key. To undo a parent file's setting in a subdirectory, write `clear lombok.val.flagUsage` (works for any key).

Source: https://projectlombok.org/features/configuration (Lombok 1.18.48)

## Graduated from experimental

These moved to the main `lombok` package; use the main-package import, not `lombok.experimental.*`.

| Feature | Promoted in | Notes |
|---|---|---|
| `@Value` | v0.12.0 | `lombok.experimental.Value` removed from the jar in v1.16.22 (Lombok still processes it). |
| `@Builder` | v1.16.0 | Gained `@Singular`; lost `fluent` and `chain`. `lombok.experimental.Builder` removed from the jar in v1.16.22. |
| `@Wither` → `@With` | v1.18.10 | Renamed; config keys renamed. `lombok.experimental.Wither` removed from the jar in v1.18.48 (Lombok still processes it). |
| `var` | v1.16.22 per changelog (the `var` page says 1.16.20) | Experimental `var` kept as a deprecated alias. |
| `@SuperBuilder` | v1.18.48 | No changes to the annotation; old experimental one stays for a few versions, then deprecated, then removed. Its `lombok.config` keys were renamed. |

Also: the old `lombok.Delegate` (pre-v1.14) was removed from the jar in v1.18.48; use `lombok.experimental.Delegate`.

Source: https://projectlombok.org/features/experimental/all (Lombok 1.18.48)

## @UtilityClass

Turns a class into a utility class: generates a private constructor that throws `java.lang.UnsupportedOperationException`, makes the class `final`, marks *all* members `static` (fields and inner classes too), and marks an inner class `static`.

```java
import lombok.experimental.UtilityClass;

@UtilityClass
public class Strings2 {
    private final int LIMIT = 5;          // becomes private static final

    public int clamp(int in) {            // becomes public static
        return Math.min(in, LIMIT);
    }
}
```

Small print:
- Explicit constructors are flagged as errors; you cannot write your own constructor or any non-static member. If the class must be instantiated, even internally, do not use `@UtilityClass`.
- **Non-star static imports of its members do not work** (javac limitation). Use `import static com.example.Strings2.*;` or do not static-import. This is the reason for the *negative* status.
- Inner classes in `@UtilityClass` were broken on JDK9+ until v1.18.24.

Source: https://projectlombok.org/features/experimental/UtilityClass (Lombok 1.18.48)

## @Delegate

On a field or a no-argument method: generates forwarding methods for all `public` methods of its type (and supertypes), except those declared in `java.lang.Object`.

```java
import java.util.ArrayList;
import java.util.Collection;
import lombok.experimental.Delegate;

public class Names {
    private interface Included {            // precise control: list signatures
        boolean add(String item);
        boolean remove(Object item);
    }

    @Delegate(types = Included.class)
    private final Collection<String> items = new ArrayList<String>();
}
```

Options: `types` (delegate the public methods of these types instead of the field's type; they need not be supertypes of the field) and `excludes` (skip all public methods of these types and their supertypes).

Small print:
- All computed methods are copied **even if you wrote them yourself**, causing duplicate-method errors. Exclude them with `excludes=` (typically a private inner interface with the signatures you implement by hand).
- `types`/`excludes` cannot take generics (Java limitation); use private inner interfaces or classes that extend the generic type.
- Not allowed on static fields or methods.
- Recursive delegation (delegated/excluded types that themselves contain `@Delegate`) is an error.
- Combined with `@Getter` or `@Data`, it delegates to the result of the generated getter.

Source: https://projectlombok.org/features/experimental/Delegate (Lombok 1.18.48)

## @ExtensionMethod

Makes `public static` methods with at least one non-primitive parameter callable as if they were instance methods of their first parameter's type, inside the annotated class.

```java
import lombok.experimental.ExtensionMethod;

@ExtensionMethod({java.util.Arrays.class, Extensions.class})
public class Example {
    public String test() {
        int[] a = {5, 3, 8, 2};
        a.sort();                                  // -> Arrays.sort(a)
        String s = null;
        return s.or("hello".toTitleCase());        // -> Extensions.or(s, Extensions.toTitleCase("hello"))
    }
}

class Extensions {
    public static <T> T or(T obj, T ifNull) { return obj != null ? obj : ifNull; }
    public static String toTitleCase(String in) {
        return in.isEmpty() ? in : Character.toTitleCase(in.charAt(0)) + in.substring(1).toLowerCase();
    }
}
```

Small print:
- Calls are rewritten, not inlined: the extension class must be on the classpath at compile **and** run time.
- A call on `null` is not an instant `NullPointerException`; `null` is passed as the first argument.
- Generics are fully applied: an extension taking `List<? extends String>` applies to `List<String>`, not to `List<Object>`.
- Lombok ships no extension methods; you write your own.
- Known pain: IDE auto-complete (NetBeans), more bugs than the authors like. On JDK25, javac could show spurious errors with `@ExtensionMethod` (fixed in v1.18.44).

Source: https://projectlombok.org/features/experimental/ExtensionMethod (Lombok 1.18.48)

## onX (onMethod_, onConstructor_, onParam_)

Puts your own annotations on generated code. "Workaround status": it can disappear without a reasonable deprecation period and may stop working with future javac. Use it only when nothing else works.

| Parameter | On | Puts annotations on |
|---|---|---|
| `onMethod_` | `@Getter`, `@Setter`, `@With` (page says `@Wither`) | the generated method |
| `onParam_` | `@Setter`, `@With`; `@EqualsAndHashCode` | the single parameter; for `@EqualsAndHashCode`, the parameter of `equals` and any `canEqual` |
| `onConstructor_` | `@AllArgsConstructor`, `@NoArgsConstructor`, `@RequiredArgsConstructor` | the generated constructor |

```java
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor(onConstructor_ = @Inject)
public class OnXExample {
    @Getter(onMethod_ = {@Id, @Column(name = "unique-id")})
    @Setter(onParam_ = @Max(10000))
    private long unid;
}
```

Small print:
- javac8 and later: add an underscore after the name (`onMethod_=`). The javac7 form is `onMethod=@__({@A, @B})`; the `__` type must **not** exist (add more underscores if it does); any wrapper other than a series of underscores is an error.
- Not legal on type-wide variants, per the onX page (a class-level `@Getter` does not support `onMethod`). The v1.16.22 changelog says `@Getter`/`@Setter` on a type do allow `onMethod`/`onParam`; test before relying on either.
- `lombok.onX.flagUsage = WARNING` only actually produced warnings from v1.18.34.

Source: https://projectlombok.org/features/experimental/onX (Lombok 1.18.48)

## @Helper

On a method-local class: lets you call its methods directly, as if Java allowed methods inside methods.

```java
import lombok.experimental.Helper;

public class HelperExample {
    int someMethod(int arg1) {
        int localVar = 5;
        @Helper class Helpers {
            int helperMethod(int arg) { return arg + localVar; }
        }
        return helperMethod(10);   // -> $Helpers.helperMethod(10)
    }
}
```

Small print:
- The helper class needs a no-args constructor (compile error otherwise).
- The hidden instance is named `$Helpers` (`$` + class name); do not rely on it.
- `this` inside the helper is not your outer object; use `OuterClass.this`.
- *Any* unqualified call below the declaration whose name matches a helper method is treated as a call to the helper; incompatible arguments give a compile error.
- Before v1.18.30, a `@Helper` class directly in a method (not nested deeper) did not work.

Source: https://projectlombok.org/features/experimental/Helper (Lombok 1.18.48)

## @FieldNameConstants

Generates an inner type (default `public static final class Fields`) with one `public static final String` constant per field, value equal to the field name. With `asEnum = true`, an enum with one value per field instead. Useful for marshalling/serialization frameworks.

```java
import lombok.experimental.FieldNameConstants;

@FieldNameConstants
public class Customer {
    private final String name;
    private final int age;
    @FieldNameConstants.Exclude private final int internalId;
}
// generates: public static final class Fields { name = "name"; age = "age"; }
```

Options: `asEnum`, `innerTypeName = "FieldNames"`, `level = AccessLevel.PACKAGE` (of the inner type; constants are always `public`), `onlyExplicitlyIncluded = true` with `@FieldNameConstants.Include`, `@FieldNameConstants.Exclude`.

Small print:
- Only on classes (or enums). By default all non-transient, non-static fields; fields starting with `$` and static fields are always skipped.
- Constant names match the field name exactly (capitalization included) unless `lombok.fieldNameConstants.uppercase = true` (since v1.18.8).
- Since v1.18.6 you can write the inner `Fields` class/enum yourself; Lombok adds only the missing constants.
- v1.16.22 to v1.18.2 generated `FIELD_...` constants directly in the class; v1.18.4 redesigned it (breaking change). Old code using `FIELD_X` names breaks on upgrade.
- Lombok annotation parameters that take strings need literals; you cannot pass these constants to `@ToString(of=...)` etc. Use `@ToString.Include`/`.Exclude` instead.
- MapStruct: when you reference these constants in `@Mapping` and use `@Mapping` more than once on a node, wrap them manually in `@Mappings`: `@Mappings({@Mapping(target = Entity.Fields.entityProperty, source = "dtoProperty")})`.

| Key | Default | Effect |
|---|---|---|
| `lombok.fieldNameConstants.flagUsage` | not set | `warning`/`error` on any use |
| `lombok.fieldNameConstants.innerTypeName` | `Fields` | name of the generated inner type |
| `lombok.fieldNameConstants.uppercase` | `false` | if `true`, try to `UPPER_CASE` the constant names |

Source: https://projectlombok.org/features/experimental/FieldNameConstants (Lombok 1.18.48)

## @Tolerate

On any method or constructor: Lombok acts as if it did not exist, so it generates a member it would otherwise skip because of a name clash.

```java
import lombok.Setter;
import lombok.experimental.Tolerate;

public class Event {
    @Setter private Date date;          // setDate(Date) is generated anyway

    @Tolerate
    public void setDate(String date) {  // without @Tolerate, Lombok would skip setDate(Date)
        this.date = Date.valueOf(date);
    }
}
```

Small print:
- Use it when your method has a different parameter type that does not really clash.
- `@EqualsAndHashCode` generates nothing (and warns) if any `equals` or `hashCode` exists; mark such a method `@lombok.experimental.Tolerate` to hide it from Lombok.
- No configuration key is documented.

Source: https://projectlombok.org/features/experimental/Tolerate (Lombok 1.18.48)

## val and var

Both are main-package features (`lombok.val`, `lombok.var`), not experimental. Use them as the type of a local variable; the type is inferred from the mandatory initializer.

```java
import lombok.val;
import lombok.var;

val list = new ArrayList<String>();     // final ArrayList<String> list
var count = 0;                          // int count (not final)
for (val e : map.entrySet()) { ... }    // final Map.Entry<K, V> e
```

`val` (since v0.10):
- Makes the variable `final`. Local variables and foreach loops only; not fields, not basic `for` loops. Initializer required.
- You must import `lombok.val` (or write `lombok.val`).
- Since v1.18.22: if the compiler and `--release`/`--source` are 10 or higher, `val` is replaced by `final var`. Consequence: compound declarations like `val x = 10, y = 12;` now fail.
- Compound types infer the most common superclass, not shared interfaces (`b ? new HashSet() : new ArrayList()` gives `AbstractCollection`). Ambiguous cases (initializer `null`) infer `java.lang.Object`.
- Does not work in NetBeans. On JDK25, javac could show spurious errors (fixed in v1.18.44).

`var` (experimental in v1.16.12; promoted in v1.16.22 per changelog, 1.16.20 per its page):
- Works exactly like `val` but not `final`.
- The type comes only from the initializer; later assignments do not widen it: `var x = "Hello"; x = Color.RED;` fails.
- On Java's own `var`: the `var` page says JEP 286 "establishes expectations" and Lombok's `var` follows them, which is why it was promoted. Delombok outputs Java 10's own `var` as `var` (since v1.18.8).

Source: https://projectlombok.org/features/val and https://projectlombok.org/features/var (Lombok 1.18.48)

## Beyond the Lombok docs

- (Not from the Lombok docs) On Java 10+, prefer the language's own `var` (and `final var`) over `lombok.var`/`lombok.val`: it needs no import and no annotation processing.
- (Not from the Lombok docs) In shared codebases, set `lombok.experimental.flagUsage = error` in the root `lombok.config` and `clear` it only in the subdirectories that deliberately use an experimental feature.
