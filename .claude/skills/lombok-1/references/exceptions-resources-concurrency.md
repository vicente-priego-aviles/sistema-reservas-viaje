# Exceptions, resources and locking: `@StandardException`, `@SneakyThrows`, `@Cleanup`, `@Synchronized`, `@Locked`

Read this when you write a custom exception with Lombok, throw checked exceptions without declaring them, close resources with `@Cleanup`, or guard methods with `@Synchronized` / `@Locked`.

## Contents
- [@StandardException (experimental)](#standardexception-experimental)
- [@SneakyThrows](#sneakythrows)
- [@Cleanup](#cleanup)
- [@Synchronized](#synchronized)
- [@Locked](#locked)
- [Beyond the Lombok docs](#beyond-the-lombok-docs)

## @StandardException (experimental)

**Experimental.** The feature page says it "was introduced as an experimental feature in lombok v1.18.21"; the changelog lists it under v1.18.22 ("Added the `@StandardException` feature"). Import: `lombok.experimental.StandardException`.

Put it on your own exception types (classes that extend `Exception` or anything else inheriting from `Throwable`). It generates up to 4 constructors:
- `MyException()`: no message, no cause.
- `MyException(String message)`: the message, no cause.
- `MyException(Throwable cause)`: the cause (message: see the contradiction below).
- `MyException(String message, Throwable cause)`: the full constructor.

Each constructor forwards to the full one. The full one calls `super(message);` and then `super.initCause(cause);` if the cause is not null. Any constructor you write by hand is not generated. The docs: "There are few reasons *not* to put this annotation on all your custom exceptions."

```java
import lombok.experimental.StandardException;

@StandardException
public class OrderNotFoundException extends Exception {
}

// Usage
throw new OrderNotFoundException("Order 42 not found", ioException);
```

### Requirements
- The parent class **must** have a no-args constructor **and** a `(String message)` constructor.
- The parent does not need the `Throwable cause` variants (lombok uses `initCause` instead).
- Lombok does not check that you actually extend an exception type.

### Small print / pitfalls
- **`initCause` after an explicit null cause**: normally `new SomeException(message, null)` sets the cause to "no cause", which cannot be changed later with `initCause`. Lombok's generated exceptions **do** let you overwrite that explicit no-cause with `initCause` later (because `initCause` is only called when the cause is not null).
- **Cause-only constructor and message: the page contradicts itself.**
  - The overview list says the cause-only constructor "will copy the message from the cause, if there is one", and the "Vanilla Java" example shows `this(cause != null ? cause.getMessage() : null, cause);`.
  - The small print says the opposite: normally `super(cause)` sets the message to the cause's message, "however, lombok does not do this - it leaves the exception as having no message at all", because inheriting the message "is fundamentally wrong".
  - The overview and code example are the more specific statement of what is generated; the small print is the "second slight functional difference". Do not rely on either: if the message matters, use the `(String message, Throwable cause)` constructor explicitly, or verify with delombok.

### Config keys
| Key | Default | Effect |
|---|---|---|
| `lombok.standardException.addConstructorProperties` = `true` \| `false` | `false` | Listed on the page without a description. |
| `lombok.standardException.flagUsage` = `warning` \| `error` | not set | Flags any usage of `@StandardException`. |

Source: https://projectlombok.org/features/experimental/StandardException (Lombok 1.18.48)

## @SneakyThrows

Throws checked exceptions without declaring them in the method's `throws` clause. The generated code does not ignore, wrap, replace or modify the exception; it "simply fakes out the compiler" (at the JVM level any exception can be thrown regardless of `throws`). Import: `lombok.SneakyThrows`. Generated shape: `try { body } catch (T t) { throw Lombok.sneakyThrow(t); }`.

```java
import lombok.SneakyThrows;

public class Utf8 implements Runnable {
    @SneakyThrows(UnsupportedEncodingException.class)
    public String utf8ToString(byte[] bytes) {
        return new String(bytes, "UTF-8");
    }

    @SneakyThrows
    public void run() {
        throw new Throwable();
    }
}
```

- Pass any number of exception types; with none, any exception may be thrown sneakily.
- The docs call it "somewhat contentious" and say it "should be used carefully".

### When the docs consider it acceptable
1. A needlessly strict interface such as `Runnable`: whatever escapes `run()` goes to the `Thread`'s unhandled exception handler anyway; wrapping in a `RuntimeException` only obscures the real cause.
2. An "impossible" exception, e.g. `UnsupportedEncodingException` from `new String(bytes, "UTF-8")`: the JVM spec guarantees UTF-8.

Lambdas often hit strict interfaces, but lambdas cannot be annotated, so `@SneakyThrows` does not combine easily with them.

### Small print / pitfalls
- **You cannot catch a sneakily thrown checked type directly**: javac rejects a `catch` for a checked exception that nothing in the `try` body declares. Do not use it "without some deliberation".
- It is not part of the method signature: declaring a checked exception that no called method throws is an **error**, and `@SneakyThrows` is **not inherited**.
- On a constructor, the call to a sibling (`this(...)`) or super constructor is **excluded** from the treatment (it must be the first statement and cannot be inside try/catch).
- On an empty method, or a constructor that is empty or only calls a sibling/super constructor: no try/catch is generated, and a warning is emitted.

### Config keys
| Key | Default | Effect |
|---|---|---|
| `lombok.sneakyThrows.flagUsage` = `warning` \| `error` | not set | Flags any usage of `@SneakyThrows`. |

Source: https://projectlombok.org/features/SneakyThrows (Lombok 1.18.48)

## @Cleanup

Annotate a **local variable declaration**; at the end of the current scope lombok calls `close()` on it, guaranteed by a try/finally. Import: `lombok.Cleanup`.

```java
import lombok.Cleanup;
import java.io.*;

public static void copy(String from, String to) throws IOException {
    @Cleanup InputStream in = new FileInputStream(from);
    @Cleanup OutputStream out = new FileOutputStream(to);
    byte[] b = new byte[10000];
    int r;
    while ((r = in.read(b)) != -1) out.write(b, 0, r);
}
```

- Other cleanup method: `@Cleanup("dispose") CoolBar bar = new CoolBar(parent, 0);`. The method must take **no arguments**; methods with 1 or more arguments cannot be used. Default: `close()`.
- Each `@Cleanup` variable gets its own nested try/finally covering the rest of the scope.

### Small print / pitfalls
- The cleanup method is called in the finally block only if the resource is not `null`.
- With `delombok`, a call to `lombok.Lombok.preventNullAnalysis(Object o)` is inserted (to silence static analysis about the null check). Compiling with `lombok.jar` removes it, so there is no runtime dependency.
- **The cleanup exception hides the original**: if the body throws and the cleanup call also throws, the original exception is lost. Do not rely on this; the authors would prefer the opposite but know no feasible way to implement it.
- You still need to handle any exception the cleanup method can throw.
- The page does not compare `@Cleanup` with try-with-resources.

### Config keys
| Key | Default | Effect |
|---|---|---|
| `lombok.cleanup.flagUsage` = `warning` \| `error` | not set | Flags any usage of `@Cleanup`. |

Source: https://projectlombok.org/features/Cleanup (Lombok 1.18.48)

## @Synchronized

A safer variant of the `synchronized` method modifier. Only on **static and instance methods**. The keyword locks on `this` (or the class object); the annotation locks on a **private** field `$lock` (instance methods) or a static field `$LOCK` (static methods), created if it does not exist. Import: `lombok.Synchronized`.

Why: locking on `this` or your class object lets code outside your control lock on the same objects, causing race conditions and other threading bugs.

```java
import lombok.Synchronized;

public class Counter {
    private final Object readLock = new Object();

    @Synchronized public static void hello() { }        // locks $LOCK
    @Synchronized public int answer() { return 42; }    // locks $lock
    @Synchronized("readLock") public void foo() { }     // locks readLock
}
```

- You may declare `$lock` / `$LOCK` yourself; then they are not generated.
- With a field name as parameter (`@Synchronized("readLock")`), the field is **not** created: you must declare it, or an error is emitted. Reason: a typo would otherwise be a very hard-to-find bug.
- For `java.util.concurrent.locks` locks ("recommended if you're using virtual threads"), use `@Locked`.

### Small print / pitfalls
- Generated lock fields are initialized with an empty `Object[]`, not `new Object()`, because a 0-size array is serializable; `@Synchronized` does not prevent serialization.
- Removing all `@Synchronized` methods removes the lock field, which changes the computed `serialVersionUID`. If you store objects long-term with Java serialization, always declare a `serialVersionUID`.

### Config keys
| Key | Default | Effect |
|---|---|---|
| `lombok.synchronized.flagUsage` = `warning` \| `error` | not set | Flags any usage of `@Synchronized`. |

Source: https://projectlombok.org/features/Synchronized (Lombok 1.18.48)

## @Locked

Since lombok v1.18.32. Wraps all code in the method in a block that acquires a `java.util.concurrent.locks.ReentrantLock` and unlocks it when the method exits (try/finally). "A lot like" `@Synchronized`. Import: `lombok.Locked`.

- Default field: `$LOCK` (static methods) / `$lock` (instance methods), generated if it does not exist.
- Named field: `@Locked("baseLock")`; the field must be a `ReentrantLock`.
- `@Locked.Read` / `@Locked.Write`: use a `ReentrantReadWriteLock` (generated when required); `Read` locks the read lock, `Write` the write lock.
- The page: "When using Virtual threads (introduced in Java 20), these locks are recommended compared to what `@Synchronized` does."

```java
import lombok.Locked;

public class Holder {
    private int value;

    @Locked.Read  public int getValue() { return value; }
    @Locked.Write public void setValue(int v) { value = v; }
    @Locked("baseLock") public void foo() { }   // baseLock must exist and be a ReentrantLock
}
```

### Small print / pitfalls
- `@Locked` uses a different lock type than `@Locked.Read` / `@Locked.Write`, but **all three share the same default field name**. You cannot mix plain `@Locked` with `Read`/`Write` on the default name; give an explicit field name to one of them.

### Config keys
| Key | Default | Effect |
|---|---|---|
| `lombok.locked.flagUsage` = `warning` \| `error` | not set | Flags any usage of `@Locked`. |

Source: https://projectlombok.org/features/Locked (Lombok 1.18.48)

## Beyond the Lombok docs
- (Not from the Lombok docs) For `AutoCloseable` resources, try-with-resources keeps the original exception and adds the close exception as suppressed, which avoids the `@Cleanup` masking problem.
- (Not from the Lombok docs) Virtual threads were a preview feature in Java 19 and 20 and became final in Java 21.
- (Not from the Lombok docs) Code that calls a method using `@SneakyThrows` can still catch the exception with `catch (Exception e)` and an `instanceof` check.
