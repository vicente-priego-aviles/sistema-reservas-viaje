# Lombok with Spring, JPA, Jackson and MapStruct

A cookbook for using Lombok in the common Java stack. Read it when you add Lombok to Spring beans, JPA entities, JSON DTOs or MapStruct mappers, or when reviewing such code. Part 1 states only what the Lombok docs say; part 2 (`## Beyond the Lombok docs`) is general practice, not Lombok documentation.

## Contents
- Part 1, what the Lombok docs say
  - [Equality in hierarchies and JPA proxies (canEqual)](#equality-in-hierarchies-and-jpa-proxies-canequal)
  - [No-args constructors for frameworks](#no-args-constructors-for-frameworks)
  - [Copying annotations to generated code (lombok.copyableAnnotations)](#copying-annotations-to-generated-code-lombokcopyableannotations)
  - [onX: annotations on generated members, last resort](#onx-annotations-on-generated-members-last-resort)
  - [@Builder with @NoArgsConstructor and @AllArgsConstructor](#builder-with-noargsconstructor-and-allargsconstructor)
  - [Jackson: @Jacksonized, Jackson 3 and annotation copying](#jackson-jacksonized-jackson-3-and-annotation-copying)
  - [Excluding fields from toString, equals and hashCode](#excluding-fields-from-tostring-equals-and-hashcode)
  - [MapStruct and other annotation processors](#mapstruct-and-other-annotation-processors)
- [Part 2, beyond the Lombok docs](#beyond-the-lombok-docs)

## Equality in hierarchies and JPA proxies (canEqual)

`@EqualsAndHashCode` (and `@Data`, `@Value`) generates a `protected boolean canEqual(Object other)` unless the class is `final` and extends `java.lang.Object`. Per the docs, this means "JPA proxies can still be equal to their base class, but subclasses that add new state don't break the equals contract".

- In a subclass, always decide `callSuper`. Without it Lombok warns and compares only the subclass's own fields. `callSuper = true` when extending `Object` is a compile error.
- `callSuper = true` is safe when the superclass `equals` is also Lombok-generated; hand-written ones may not handle it.
- If you write your own `equals`, override `canEqual` too whenever you change `equals` and `hashCode`.
- If any method named `equals` or `hashCode` exists, nothing is generated (with a warning). `@Data` skips silently when a method with the same name and parameter count exists.
- If a getter exists for an included field, the generated code calls the getter instead of reading the field. Suppress with `@EqualsAndHashCode(doNotUseGetters = true)`.
- `cacheStrategy` (since v1.18.16): do not use it if the object can change in a way that changes `hashCode()`.

```java
@Getter @Setter
@EqualsAndHashCode(callSuper = true)
public class Employee extends Person {
    private String department;
}
```

| Key | Default | Effect |
|---|---|---|
| `lombok.equalsAndHashCode.callSuper` | `warn` | `call` / `skip` / `warn` when the class extends something |
| `lombok.equalsAndHashCode.doNotUseGetters` | `false` | if `true`, read fields directly |

Source: https://projectlombok.org/features/EqualsAndHashCode (Lombok 1.18.48)

## No-args constructors for frameworks

The docs: "Certain java constructs, such as hibernate and the Service Provider Interface require a no-args constructor". `@NoArgsConstructor` is "useful primarily in combination with either `@Data` or one of the other constructor generating annotations".

```java
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Address {
    private String street;
    private String city;
}
```

- With `final` fields, `@NoArgsConstructor` is a compile error unless `force = true`, which sets them to `0`/`false`/`null`. No null checks are generated for `@NonNull` fields, so those constraints are not met until the fields are set later.
- Unlike most Lombok annotations, an explicit constructor does not stop the constructor annotations; a signature clash is a compile error.
- `lombok.noArgsConstructor.extraPrivate = true` makes `@Data`/`@Value` classes also get a private no-args constructor that sets every field to `null`/`0`/`false` (added for deserialization frameworks such as Jackson).
- `@java.beans.ConstructorProperties` is generated only if `lombok.anyConstructor.addConstructorProperties = true` (needs module `java.desktop` under jigsaw); never on no-args constructors or static factories.

| Key | Default | Effect |
|---|---|---|
| `lombok.noArgsConstructor.extraPrivate` | `false` | private no-args constructor on `@Data`/`@Value` |
| `lombok.anyConstructor.addConstructorProperties` | `false` | add `@ConstructorProperties` to generated constructors |

Source: https://projectlombok.org/features/constructor (Lombok 1.18.48)

## Copying annotations to generated code (lombok.copyableAnnotations)

What the docs say about the key: `lombok.copyableAnnotations` = a list of fully qualified types (default: empty list). "Lombok will copy any of these annotations from the field to the constructor parameter, the setter parameter, and the getter method." Lombok already copies "all popular nullable/nonnull annotations" out of the box. List keys take `+=` to add and `-=` to remove an entry.

Example (Spring's `@Qualifier` and `@Value` on fields of a bean with `@RequiredArgsConstructor`):

```properties
# lombok.config
lombok.copyableAnnotations += org.springframework.beans.factory.annotation.Qualifier
lombok.copyableAnnotations += org.springframework.beans.factory.annotation.Value
```

```java
@Service
@RequiredArgsConstructor
public class PaymentService {
    @Qualifier("fastGateway")
    private final Gateway gateway;            // the generated constructor parameter also gets @Qualifier("fastGateway")

    @Value("${payments.retries}")
    private final int retries;
}
```

- The annotations are copied to *every* place listed above, including the getter if you also generate one.
- Since v1.18.10, configuration keys dealing with identifiers and types, `lombok.copyableAnnotations` among them, are validated more strictly.

Source: https://projectlombok.org/features/constructor (Lombok 1.18.48)

## onX: annotations on generated members, last resort

When copying from the field is not enough, `onConstructor_`, `onMethod_` and `onParam_` put annotations on generated constructors, methods and parameters. The docs call it "workaround status": ugly syntax, may break on future javac, may disappear without a reasonable deprecation period. Prefer `lombok.copyableAnnotations` or a hand-written member.

```java
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LegacyBean { private final Repo repo; }
```

Not legal on type-wide variants per the onX page. Full rules: `experimental-and-misc.md`.

Source: https://projectlombok.org/features/experimental/onX (Lombok 1.18.48)

## @Builder with @NoArgsConstructor and @AllArgsConstructor

`@Builder` on a class acts as if you added `@AllArgsConstructor(access = AccessLevel.PACKAGE)` and put `@Builder` on it. This only works if you have not written any explicit constructor **or let Lombok create one, such as with `@NoArgsConstructor`**. So when a framework needs a no-args constructor and you also want a builder, add the all-args constructor yourself:

```java
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)   // required: @NoArgsConstructor stops @Builder's own one
public class OrderLine {
    private String sku;
    @Builder.Default private int quantity = 1;
}
```

- Lombok-generated constructors (such as `@NoArgsConstructor`) also apply `@Builder.Default` values; explicit constructors do not (call `this()` or set them by hand).
- The `@Builder.Default` initializer moves to a static method: it cannot use `this`, `super` or non-static members.
- With an explicit constructor, put `@Builder` on that constructor instead of on the class.
- `@Value` + `@Builder`: the builder's package-private constructor wins and suppresses `@Value`'s.

Source: https://projectlombok.org/features/Builder (Lombok 1.18.48)

## Jackson: @Jacksonized, Jackson 3 and annotation copying

`@Jacksonized` (experimental, since v1.18.14 per its page; the changelog lists it under v1.18.16) makes Jackson deserialize through a `@Builder`/`@SuperBuilder`. It only acts where there is also a `@Builder` or `@SuperBuilder` (warning otherwise) and does not change the builder.

```java
@Jacksonized @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class JacksonExample {
    private List<Foo> foos;
}
```

What it does: adds `@JsonDeserialize(builder = Foobar.FoobarBuilder[Impl].class)` (error if one already exists); copies Jackson configuration annotations such as `@JsonIgnoreProperties` from the class to the builder; adds `@JsonPOJOBuilder(withPrefix = "")` (or your `setterPrefix`) and passes a renamed `buildMethodName` to Jackson; for `@SuperBuilder`, makes the builder implementation class package-private. Since v1.18.40 it also supports `@Accessors(fluent = true)` on a type (not on fields) by inserting `@JsonProperty`.

Jackson 3: since v1.18.44, set `lombok.jacksonized.jacksonVersion`. Without it Lombok generates Jackson 2 annotations **and warns**. It is a list key (`+=`), so during a migration you can ask for both variants.

```properties
lombok.jacksonized.jacksonVersion += 3
```

The page documents the value only as `version` (default 2); `3` above follows from that. For both variants, add one `+=` line per version.

Without `@Jacksonized`, customize the builder by hand: write `@JsonDeserialize(builder = X.XBuilder.class)` on the class and declare `public static class XBuilder` with `@JsonPOJOBuilder(withPrefix = "")`; Lombok fills in the rest.

Small print from the changelog:
- v1.18.40: Lombok no longer copies Jackson annotations such as `@JsonProperty` from fields to getters/setters (it did from v1.18.16 to v1.18.38). Restore with `lombok.copyJacksonAnnotationsToAccessors = true`.
- v1.18.44: `@Jacksonized` no longer serializes `transient` fields.
- v1.18.46: `@JsonProperty` generation no longer stops at the first explicit `@JsonIgnore`; Jackson 3 finishing touches.

| Key | Default | Effect |
|---|---|---|
| `lombok.jacksonized.flagUsage` | not set | `warning`/`error` on any use |
| `lombok.jacksonized.jacksonVersion` | 2 + warning | which Jackson major's annotations to generate (list) |
| `lombok.copyJacksonAnnotationsToAccessors` | not copied since v1.18.40 | `true` restores field-to-accessor copying |

Source: https://projectlombok.org/features/experimental/Jacksonized (Lombok 1.18.48)

## Excluding fields from toString, equals and hashCode

```java
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Book {
    @EqualsAndHashCode.Include private String isbn;
    private String title;
    @ToString.Exclude private Author author;
}
```

- `@ToString.Exclude` / `@EqualsAndHashCode.Exclude` skip a field; `onlyExplicitlyIncluded = true` with `.Include` lists exactly what counts.
- Both `.Exclude` and `.Include` on a member: warning, and it is excluded.
- Excluding a field that does not exist or is excluded anyway (static, transient): warning.
- Fields starting with `$` are excluded by default.
- The old `of`/`exclude` parameters still work but will be deprecated; they need string literals (not `@FieldNameConstants` constants).
- Arrays are compared deeply; an array that contains itself gives `StackOverflowError`.

| Key | Default | Effect |
|---|---|---|
| `lombok.toString.onlyExplicitlyIncluded` | `false` | only `@ToString.Include` members are printed |

Source: https://projectlombok.org/features/ToString (Lombok 1.18.48)

## MapStruct and other annotation processors

- Since v1.18.16 (breaking change), MapStruct users must add a dependency on `lombok-mapstruct-binding`.
- Since v1.16.14, processors that look for Lombok-generated methods work only if Lombok runs first; MapStruct was specifically adapted to allow any order.
- With Maven, lombok must be in `annotationProcessorPaths`; mandatory since JDK23, or JDK9+ when compiling modules.
- `@FieldNameConstants` constants used in more than one `@Mapping` on a node must be wrapped in `@Mappings({...})` by hand.

Source: https://github.com/projectlombok/lombok/blob/b48657c83ce44d027984f539bd36048293ce5e8e/doc/changelog.markdown (Lombok 1.18.48)

## Beyond the Lombok docs

Spring beans:
- (Not from the Lombok docs) Inject dependencies through the constructor: `private final` fields plus `@RequiredArgsConstructor`. A single constructor needs no `@Autowired` in Spring.
- (Not from the Lombok docs) Do not use `@Data` or `@Setter` on singleton beans; their state should not be mutable.

JPA entities:
- (Not from the Lombok docs) Avoid `@Data` on entities: it adds setters for the id, `equals`/`hashCode` over all fields and a `toString` that touches every relation.
- (Not from the Lombok docs) Use `@Getter`, targeted `@Setter`, and `@NoArgsConstructor(access = AccessLevel.PROTECTED)` (JPA needs a no-args constructor; protected keeps it out of business code).
- (Not from the Lombok docs) Keep `equals`/`hashCode` away from lazy relations and from database-generated ids that change on persist; either write them by hand around a stable business key, or use `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` on that key.
- (Not from the Lombok docs) Put `@ToString.Exclude` on every relation (`@OneToMany`, `@ManyToOne`, ...) to avoid lazy-loading exceptions and infinite recursion on bidirectional links.
- (Not from the Lombok docs) Be careful with `@Builder` on entities: collections initialized in the field need `@Builder.Default`, and the builder bypasses any invariants in your constructors.

Configuration properties:
- (Not from the Lombok docs) For `@ConfigurationProperties`, prefer Java records or immutable classes (`@Value`, or `final` fields with a constructor) bound through the constructor, over mutable `@Data` classes.

MapStruct build setup:
- (Not from the Lombok docs) In Maven, list `lombok`, `lombok-mapstruct-binding` and `mapstruct-processor` together in the compiler plugin's `annotationProcessorPaths`, with versions managed in one place.

```xml
<annotationProcessorPaths>
    <path><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><version>${lombok.version}</version></path>
    <path><groupId>org.projectlombok</groupId><artifactId>lombok-mapstruct-binding</artifactId><version>${lombok-mapstruct-binding.version}</version></path>
    <path><groupId>org.mapstruct</groupId><artifactId>mapstruct-processor</artifactId><version>${mapstruct.version}</version></path>
</annotationProcessorPaths>
```
