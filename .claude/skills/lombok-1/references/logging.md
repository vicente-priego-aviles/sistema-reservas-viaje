# Logging: `@Log` and friends, `@CustomLog`

Read this when you add a logger field with a Lombok annotation (`@Slf4j`, `@Log4j2`, `@Log`, `@CustomLog`…), change the logger name or topic, or configure logging keys in `lombok.config`.

## Contents
- [What the annotations generate](#what-the-annotations-generate)
- [Variants and the logger each creates](#variants-and-the-logger-each-creates)
- [@CustomLog](#customlog)
- [Config keys](#config-keys)
- [Beyond the Lombok docs](#beyond-the-lombok-docs)

## What the annotations generate

Put the variant for your logging system on the class; you get a `private static final` field named `log`, initialized the way that framework prescribes. Use it for log statements.

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderService {
    public void place(Order order) {
        log.info("Placing order {}", order.getId());
    }
}
// generates:
// private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrderService.class);
```

### Since versions
- The `@Log` variants: since lombok v0.10.
- `@Flogger`: the feature page says "NEW in lombok v1.16.24"; the changelog lists it under v1.18.0 ("Google's Flogger (a.k.a. FluentLogger) is now available via `@Flogger`").
- `@CustomLog`: since lombok v1.18.10.
- `topic` parameter: the changelog adds it in v1.12.6 ("The default remains the fully qualified type name of the class itself").
- Access level: the changelog for v1.18.42 says "All the various `@Log` annotations now allow you to change their access level (they still default to `private`)". The feature page does not document this parameter (name or values); check the annotation's Javadoc before using it.
- Since v1.18.24 (changelog), the `@Log` annotations can be placed on inner enums and records.

### Topic
- By default the logger's topic (name) is the annotated class. Change it with `topic`: `@XSlf4j(topic="reporting")`.
- With a topic, the class argument is replaced by the string, e.g. `@CommonsLog(topic="CounterLog")` generates `LogFactory.getLog("CounterLog")`.

```java
import lombok.extern.apachecommons.CommonsLog;

@CommonsLog(topic = "CounterLog")
public class LogExampleCategory {
    public static void main(String... args) {
        log.error("Calling the 'CounterLog' with a message");
    }
}
```

### Small print / pitfalls
- If a field called `log` already exists, a warning is emitted and **no code is generated**.
- **Do not put side effects in the expression you log.** A future feature may find calls to the logger field and, where the framework supports it and the level is known at compile time, guard them with an `if`, so an ignored log statement would not evaluate its arguments at all.
- The field is `static` unless `lombok.log.fieldIsStatic = false`.

Source: https://projectlombok.org/features/log (Lombok 1.18.48)

## Variants and the logger each creates

In each line, `LogExample` is the annotated class. Annotation packages are those named by the flagUsage keys on the page.

| Annotation | Generated field |
|---|---|
| `@lombok.extern.apachecommons.CommonsLog` | `private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(LogExample.class);` |
| `@lombok.extern.flogger.Flogger` | `private static final com.google.common.flogger.FluentLogger log = com.google.common.flogger.FluentLogger.forEnclosingClass();` |
| `@lombok.extern.jbosslog.JBossLog` | `private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(LogExample.class);` |
| `@lombok.extern.java.Log` | `private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(LogExample.class.getName());` |
| `@lombok.extern.log4j.Log4j` | `private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LogExample.class);` |
| `@lombok.extern.log4j.Log4j2` | `private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(LogExample.class);` |
| `@lombok.extern.slf4j.Slf4j` | `private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogExample.class);` |
| `@lombok.extern.slf4j.XSlf4j` | `private static final org.slf4j.ext.XLogger log = org.slf4j.ext.XLoggerFactory.getXLogger(LogExample.class);` |
| `@lombok.CustomLog` | whatever `lombok.log.custom.declaration` says (see below) |

Pitfalls:
- `@Log` is **java.util.logging**, not a generic logger. For SLF4J use `@Slf4j`; for Log4j 2 use `@Log4j2` (`@Log4j` is Log4j 1, `org.apache.log4j`).
- `@Log4j` and `@Log4j2` share the package `lombok.extern.log4j`; `@Slf4j` and `@XSlf4j` share `lombok.extern.slf4j`.
- Lombok only generates the field; the logging library itself must be on your classpath (the changelog's note on CVE-2021-44228: "You have to ship your own log4j2 dependency in your app").

Source: https://projectlombok.org/features/log (Lombok 1.18.48)

## @CustomLog

For loggers without a dedicated annotation; its primary purpose is "your in-house, private logging frameworks". It **requires** a `lombok.log.custom.declaration` entry in `lombok.config`.

Generated shape: `private static final com.foo.your.Logger log = com.foo.your.LoggerFactory.createYourLogger(LogExample.class);`

### Declaration syntax

```properties
# lombok.config
lombok.log.custom.declaration = com.foo.your.Logger com.foo.your.LoggerFactory.createYourLog(TYPE)(TOPIC)
```

In order:
1. The logger type (optional; if omitted, the logger factory type is used, which fits a logger class with a static factory method).
2. A space.
3. The logger factory type, a dot, and the factory method name.
4. One or two parenthesised parameter definitions: at most one **with** `TOPIC` (used when `@CustomLog(topic = ...)` is set) and at most one **without** `TOPIC` (used when no topic is set).

Each parameter definition is a comma-separated list of zero or more parameter kinds:

| Kind | Passes |
|---|---|
| `TYPE` | the annotated type, as a class |
| `NAME` | the annotated type's fully qualified name |
| `TOPIC` | the topic string set on `@CustomLog` |
| `NULL` | `null` |

Example of a shortened declaration (logger type omitted, factory method on the logger class itself):

```properties
lombok.log.custom.declaration = com.acme.log.AcmeLogger.create(NAME)(TOPIC)
```

```java
import lombok.CustomLog;

@CustomLog
public class Billing { }                 // AcmeLogger.create(<fully qualified name of Billing>)

@CustomLog(topic = "audit")
public class Audit { }                   // AcmeLogger.create("audit")
```

Pitfalls:
- The page says `@CustomLog` *requires* the config key; always add it before using the annotation.
- The page's own example is inconsistent: the declaration uses method `createYourLog`, but the generated statement shown calls `createYourLogger`. Trust the syntax rule (the method name is what you write in the declaration).

Source: https://projectlombok.org/features/log (Lombok 1.18.48)

## Config keys

| Key | Default | Effect |
|---|---|---|
| `lombok.log.fieldName` = *an identifier* | `log` | Name of the generated logger field. |
| `lombok.log.fieldIsStatic` = `true` \| `false` | `true` | `false`: the logger is an instance field. |
| `lombok.log.custom.declaration` = *LoggerType* LoggerFactoryType.loggerFactoryMethod(params) *(params)* | not set | What `@CustomLog` generates (italic parts optional). |
| `lombok.log.flagUsage` = `warning` \| `error` | not set | Flags any usage of any log annotation. |
| `lombok.log.custom.flagUsage` = `warning` \| `error` | not set | Flags `@lombok.CustomLog`. |
| `lombok.log.apacheCommons.flagUsage` = `warning` \| `error` | not set | Flags `@lombok.extern.apachecommons.CommonsLog`. |
| `lombok.log.flogger.flagUsage` = `warning` \| `error` | not set | Flags `@lombok.extern.flogger.Flogger`. |
| `lombok.log.jbosslog.flagUsage` = `warning` \| `error` | not set | Flags `@lombok.extern.jbosslog.JBossLog`. |
| `lombok.log.javaUtilLogging.flagUsage` = `warning` \| `error` | not set | Flags `@lombok.extern.java.Log`. |
| `lombok.log.log4j.flagUsage` = `warning` \| `error` | not set | Flags `@lombok.extern.log4j.Log4j`. |
| `lombok.log.log4j2.flagUsage` = `warning` \| `error` | not set | Flags `@lombok.extern.log4j.Log4j2`. |
| `lombok.log.slf4j.flagUsage` = `warning` \| `error` | not set | Flags `@lombok.extern.slf4j.Slf4j`. |
| `lombok.log.xslf4j.flagUsage` = `warning` \| `error` | not set | Flags `@lombok.extern.slf4j.XSlf4j`. |

To standardise on one framework, flag the others, e.g. `lombok.log.javaUtilLogging.flagUsage = error` and `lombok.log.log4j.flagUsage = error`.

Since v1.18.10 (changelog), `lombok.log.fieldName` is validated more strictly as an identifier.

Source: https://projectlombok.org/features/log (Lombok 1.18.48)

## Beyond the Lombok docs
- (Not from the Lombok docs) In Spring Boot applications, `@Slf4j` is the usual choice: Spring Boot's default logging starter routes SLF4J to Logback.
- (Not from the Lombok docs) With SLF4J, prefer `{}` placeholders over string concatenation so the message is only built when the level is enabled.
