# Equality and toString: `@EqualsAndHashCode` and `@ToString`

Read this when you add, review or debug `@EqualsAndHashCode` or `@ToString` (directly or through `@Data`/`@Value`): which members are used, inheritance with `callSuper`, `canEqual`, hash caching, and the config keys.

## Contents
- [@EqualsAndHashCode](#equalsandhashcode)
- [@ToString](#tostring)
- [Beyond the Lombok docs](#beyond-the-lombok-docs)

## @EqualsAndHashCode

Generates `equals(Object other)` and `hashCode()` from the fields of the class, plus a `canEqual` method (see below). Import: `lombok.EqualsAndHashCode`.

### Which members are used
- Default: all **non-static, non-transient** fields.
- Exclude a member: annotate it with `@EqualsAndHashCode.Exclude`.
- Include a member (also a method: its return value is used): `@EqualsAndHashCode.Include`.
- Only the members you mark: `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` plus `@EqualsAndHashCode.Include` on each member.
- Fields whose name starts with `$` are excluded automatically; only `@EqualsAndHashCode.Include` brings them in.
- A method marked for inclusion that has the same name as a field **replaces** that field (method included, field excluded). To replace a field with a mapped value, use `@EqualsAndHashCode.Include(replaces = "fieldName")` on a method.
- Excluding fields that do not exist, or that would be excluded anyway (static or transient), produces warnings on those names.
- The old parameters `of` and `exclude` (the mechanism before lombok 1.16.22) still work, but "will be deprecated in the future". Prefer `@EqualsAndHashCode.Include` / `@EqualsAndHashCode.Exclude` in new code.
- If a getter exists for an included field, the getter is called instead of reading the field. To read fields directly: `@EqualsAndHashCode(doNotUseGetters = true)` or the config key `lombok.equalsAndHashCode.doNotUseGetters`. The annotation parameter, if explicitly set, takes precedence over the key.

Example with explicit includes:

```java
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Product {
    @EqualsAndHashCode.Include private String sku;
    @EqualsAndHashCode.Include private BigDecimal price;
    private String description;          // not used: not marked

    // Replaces the field 'price' so that 1E2 equals 100
    @EqualsAndHashCode.Include(replaces = "price")
    BigDecimal price() { return price.stripTrailingZeros(); }
}
```

### Inheritance: `callSuper`
- In a class that extends another, the generated methods do **not** take the superclass fields into account. The docs call auto-generating `equals`/`hashCode` for such classes "normally" "a bad idea" unless you chain to the superclass.
- `callSuper = true`: `hashCode` includes `super.hashCode()` in the hash; `equals` returns false if `super.equals` thinks the objects are not equal.
- This is safe when the superclass `equals` is also lombok-generated. "Not all `equals` implementations handle this situation properly"; lombok-generated ones do.
- If the class has an explicit superclass and you do not set `callSuper`, lombok emits a **warning**. Set it (to `true` or `false`) to acknowledge you considered it.
- `callSuper = true` on a class that extends only `java.lang.Object` is a **compile-time error**.
- To set the behaviour project-wide, use `lombok.equalsAndHashCode.callSuper` (see config keys).

Example of a subclass:

```java
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Shape {
    private String color;
}

@EqualsAndHashCode(callSuper = true)
public class Square extends Shape {
    private final int width, height;

    public Square(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
```

The generated `Square.equals` checks `o instanceof Square`, then `other.canEqual(this)`, then `super.equals(o)`, then `width` and `height`; `hashCode` starts from `super.hashCode()`.

### `canEqual`
- Since lombok 0.10, unless the class is `final` **and** extends `java.lang.Object`, lombok also generates `protected boolean canEqual(Object other)`.
- Why: "JPA proxies can still be equal to their base class, but subclasses that add new state don't break the equals contract". The docs refer to the paper [How to Write an Equality Method in Java](https://www.artima.com/lejava/articles/equality.html).
- If you write your own `equals`, always override `canEqual` too when you change `equals` and `hashCode`.

### Caching the hash: `cacheStrategy`
- Since lombok 1.18.16, setting `cacheStrategy` to a value other than `CacheStrategy.NEVER` caches the result of `hashCode()` (the changelog shows `@EqualsAndHashCode(cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)`).
- **Do not** use it if objects of the class can be modified in any way that would change the result of `hashCode()`. Use it only on immutable classes.

### Annotations on the `equals` parameter
- Since lombok 1.14.0, `onParam=@__({@AnnotationsHere})` puts annotations on the `other` parameter of `equals` (and of `canEqual`, if generated). This is **experimental** (onX feature).
- If `lombok.addNullAnnotations` is configured, the parameter of `equals` and of `canEqual` gets a nullable annotation (needed with `@NonNullByDefault`-style annotations and strict nullity checking).

### Small print / pitfalls
- **Existing methods**: if there is *any* method named `hashCode` or `equals`, regardless of return type, **neither** is generated and a warning is emitted (they must stay in sync). Mark a method with `@lombok.experimental.Tolerate` to hide it from lombok.
- **Delegation to field `equals`**: lombok calls `.equals()` for every field except primitives and arrays. Surprising types stay surprising: `java.math.BigDecimal` considers scale, so `1E2` is not equal to `100`. Fix it with a `replaces` method (example above).
- **Arrays** are compared/hashed deeply (`Arrays.deepEquals`, `Arrays.deepHashCode`): an array that contains itself causes `StackOverflowError` (same as `ArrayList`).
- **floats/doubles**: two `NaN` values are considered equal (as in `java.lang.Double.equals`), so that an object equals an exact copy of itself. Doubles are compared with `Double.compare`.
- **Hash algorithm stability**: you may presume it does not change between lombok versions, but this "is not set in stone"; it may change for a significant performance gain.

### Config keys
| Key | Default | Effect |
|---|---|---|
| `lombok.equalsAndHashCode.doNotUseGetters` = `true` \| `false` | `false` | `true`: access fields directly instead of getters. An explicit annotation parameter wins. |
| `lombok.equalsAndHashCode.callSuper` = `call` \| `skip` \| `warn` | `warn` | `call`: call the superclass `equals`/`hashCode` if the class extends something. `skip`: no such calls. `warn`: like `skip`, plus a warning. |
| `lombok.equalsAndHashCode.flagUsage` = `warning` \| `error` | not set | Flags any usage of `@EqualsAndHashCode`. |

Source: https://projectlombok.org/features/EqualsAndHashCode (Lombok 1.18.48)

## @ToString

Generates `toString()`. The format is fixed: class name, then parentheses with the members separated by commas, e.g. `MyClass(foo=123, bar=234)`. Import: `lombok.ToString`. Works on enums too.

### Which members are printed
- Default: all **non-static** fields.
- Skip a field: `@ToString.Exclude`.
- Only the members you mark: `@ToString(onlyExplicitlyIncluded = true)` plus `@ToString.Include` on each one (or the config key `lombok.toString.onlyExplicitlyIncluded`).
- Include a method's output: `@ToString.Include` on an **instance (non-static) method with no arguments**.
- `@ToString.Include(name = "some other name")` changes the label.
- `@ToString.Include(rank = -1)` changes the order: members without a rank have rank 0, higher ranks print first, equal ranks print in source order.
- A method marked for inclusion with the same name as a field replaces that field's output, in the field's position.
- Both `@ToString.Exclude` and `@ToString.Include` on one member: warning, and the member is **excluded**.
- Fields starting with `$` are excluded automatically; only `@ToString.Include` brings them in.
- Old `of` / `exclude` parameters (before lombok 1.16.22) still work but "will be deprecated in the future".
- `includeFieldNames` controls whether names are printed (`fieldName=value`); default true (config key `lombok.toString.includeFieldNames`).
- If a getter exists for an included field, it is called. To read fields directly: `@ToString(doNotUseGetters = true)`.

### `callSuper`
- `callSuper = true` includes the superclass `toString()` output, e.g. `Square(super=..., width=5, height=10)`.
- `java.lang.Object.toString()` is "pretty much meaningless", so use it only when extending another class.
- Default config is `skip` (no call, no warning), unlike `@EqualsAndHashCode` whose default is `warn`.

Example with explicit includes, rank and name:

```java
import lombok.ToString;

@ToString(onlyExplicitlyIncluded = true)
public class Account {
    @ToString.Include(rank = 1) private String id;
    @ToString.Include(name = "owner") private String ownerName;
    private String passwordHash;         // never printed: not marked

    @ToString.Include
    int openOrders() { return 0; }
}

@ToString(callSuper = true)
public class Square extends Shape {
    private final int width, height;
    public Square(int width, int height) { this.width = width; this.height = height; }
}
```

### Small print / pitfalls
- **Existing method**: if there is *any* method named `toString` with no arguments, regardless of return type, nothing is generated and a warning says the annotation does nothing. `@lombok.experimental.Tolerate` hides a method from lombok.
- **Arrays** are printed with `Arrays.deepToString`: an array containing itself causes `StackOverflowError`.
- **Format is not stable**: lombok does not promise to keep the output the same between versions. Never design an API that requires parsing `toString()` output.
- With `lombok.addNullAnnotations` configured, the method or return type gets a non-null annotation.

### Config keys
| Key | Default | Effect |
|---|---|---|
| `lombok.toString.includeFieldNames` = `true` \| `false` | `true` | `false`: print only a comma-separated list of values. An explicit annotation parameter wins. |
| `lombok.toString.doNotUseGetters` = `true` \| `false` | `false` | `true`: access fields directly instead of getters. An explicit annotation parameter wins. |
| `lombok.toString.callSuper` = `call` \| `skip` \| `warn` | `skip` | `call`: call the superclass `toString` if the class extends something. `skip`: no call. `warn`: no call, plus a warning. |
| `lombok.toString.onlyExplicitlyIncluded` = `true` \| `false` | `false` | `true`: nothing is included unless marked `@ToString.Include`. |
| `lombok.toString.flagUsage` = `warning` \| `error` | not set | Flags any usage of `@ToString`. |

Source: https://projectlombok.org/features/ToString (Lombok 1.18.48)

## Beyond the Lombok docs
- (Not from the Lombok docs) On JPA entities, avoid including lazy associations or collections in `equals`/`hashCode`/`toString`: it can trigger lazy loading or infinite recursion through bidirectional relations. Prefer `onlyExplicitlyIncluded = true` with an identifier or business key.
- (Not from the Lombok docs) Exclude secrets (passwords, tokens) from `toString` with `@ToString.Exclude`, since it often ends up in logs.
- (Not from the Lombok docs) Objects used as `HashMap` keys or `HashSet` elements must not change the fields used in `hashCode` while they are stored.
