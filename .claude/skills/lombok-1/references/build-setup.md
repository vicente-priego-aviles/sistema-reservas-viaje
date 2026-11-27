# Build setup and delombok (Lombok 1.18.48)

Read this to add Lombok to a Maven, Gradle, javac or Android build, to set up an IDE, or to run delombok (see the generated code, feed javadoc or other source tools). The current version is **1.18.48**.

## Contents
1. [The rule for every build tool](#the-rule-for-every-build-tool)
2. [Maven](#maven)
3. [Gradle](#gradle)
4. [javac and module-info](#javac-and-module-info)
5. [IDEs](#ides)
6. [Android](#android)
7. [Delombok](#delombok)
8. [Beyond the Lombok docs](#beyond-the-lombok-docs)

## The rule for every build tool

Lombok is needed to **compile** the sources but must not be present when running, testing, jarring or otherwise deploying the code: declare it as a *provided* dependency. It is on Maven Central with coordinates `org.projectlombok:lombok`. Besides the compile-only dependency, register it as an **annotation processor** where the build tool separates the processor path (Maven `annotationProcessorPaths`, Gradle `annotationProcessor`).

Source: https://projectlombok.org/setup/maven (Lombok 1.18.48)

## Maven

To add Lombok to a Maven build, do both steps below.

1. Add the dependency with `provided` scope.
2. Add Lombok to `annotationProcessorPaths` of `maven-compiler-plugin`. The docs state this is **mandatory starting with JDK 23**, or with **JDK 9+ when compiling as modules** (the build has `module-info.java` files). Recommendation: add it always, so the build keeps working when the JDK is upgraded.

Ready-to-copy `pom.xml` fragment:

```xml
<properties>
    <lombok.version>1.18.48</lombok.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Small print:
- With the Eclipse compiler (ECJ/JDT) follow https://projectlombok.org/setup/ecj instead.
- To delombok from Maven, the docs recommend the third-party lombok-maven-plugin (http://awhitford.github.io/lombok.maven/lombok-maven-plugin/), useful to run source analysis tools on the code *after* Lombok has been applied, or to generate javadoc.

Source: https://projectlombok.org/setup/maven (Lombok 1.18.48)

## Gradle

Recommended: the Lombok Gradle plugin `io.freefair.lombok` (https://plugins.gradle.org/plugin/io.freefair.lombok). The docs recommend it because it makes deployment easy and adds tasks such as delombok. Look up its current version on the plugin portal; the Lombok docs do not give one.

Without the plugin, use the built-in `compileOnly` scope plus `annotationProcessor`, and the `test…` variants for test sources. Ready-to-copy `build.gradle.kts` (the same lines work in Groovy `build.gradle`):

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")

    testCompileOnly("org.projectlombok:lombok:1.18.48")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.48")
}
```

Small print:
- `compileOnly`/`annotationProcessor` cover only main sources; if tests use Lombok annotations, the two `test…` lines are required.
- Without the plugin, Eclipse users still have to download `lombok.jar` (or find it in Gradle's caches) and run it to install Lombok into Eclipse; the plugin makes that easier.

Source: https://projectlombok.org/setup/gradle (Lombok 1.18.48)

## javac and module-info

| Situation | Command |
|---|---|
| JDK 1.6–1.8 | `javac -cp lombok.jar ...` |
| JDK 9+ without `module-info.java` (supported since Lombok 1.16.20) | `javac -cp lombok.jar ...` |
| JDK 9+ with `module-info.java` | `javac -cp lombok.jar -p lombok.jar ...` |

When the project is modularized, add Lombok to `module-info.java`:

```java
module myapp {
    requires static lombok;
}
```

`static` means Lombok is not needed at runtime. Do not write `requires lombok;` without `static`.

Source: https://projectlombok.org/setup/javac (Lombok 1.18.48)

## IDEs

| IDE | What to do |
|---|---|
| Eclipse and offshoots (Spring Tool Suite, MyEclipse, Red Hat JBoss Developer Studio) | Double-click `lombok.jar` (the same jar as on Maven): the installer finds the Eclipse installations and installs (or uninstalls) Lombok. Alternative: update site `https://projectlombok.org/p2` (the installer is unsigned). After installing, **quit Eclipse and start it again**; the regular restart is not enough. Check: the Lombok version appears at the end of the copyright text in Eclipse's About dialog. |
| IntelliJ IDEA | Works without a plugin from 2020.3 to 2023.1. For versions before 2020.3 or later than 2023.1, install the Lombok plugin (https://plugins.jetbrains.com/plugin/6317): `File > Settings > Plugins`, search `Lombok Plugin`, install, restart. |
| NetBeans | Add `lombok.jar` to the project libraries; in project properties, `Build – Compiling`, check `Enable Annotation Processing in Editor`. |
| Visual Studio Code | The Extension Pack for Java (`vscjava.vscode-java-pack`) has built-in Lombok support: `Ctrl + Shift + X`, search `java`, install, reload. |

Sources: https://projectlombok.org/setup/eclipse, https://projectlombok.org/setup/intellij, https://projectlombok.org/setup/netbeans, https://projectlombok.org/setup/vscode (Lombok 1.18.48)

## Android

Lombok is compile-time only, so a correct setup adds nothing to the app.

- Gradle: Android plugin version `>= 0.4.3`. Use the gradle-lombok plugin, or add `compileOnly "org.projectlombok:lombok:1.18.48"` (requires Gradle 2.12 or newer).
- Android Studio: the Gradle setup plus the Lombok IntelliJ plugin, and both lines:

```groovy
dependencies {
    compileOnly 'org.projectlombok:lombok:1.18.48'
    annotationProcessor 'org.projectlombok:lombok:1.18.48'
}
```

- Eclipse: run `java -jar lombok.jar publicApi` to create a lightweight `lombok-api.jar` with only the annotations; add that jar to the Android project instead of the full `lombok.jar`, and install Lombok into Eclipse by double-clicking `lombok.jar` as usual.
- Maven: follow the normal Maven instructions; with Android + Eclipse + Maven together you may need to replace `lombok.jar` in the Eclipse project's build path with `lombok-api.jar`.
- Ant: copy the `-compile` target from `${ANDROID_SDK_ROOT}/tools/ant/build.xml` into your `build.xml` before the `<import file="${sdk.dir}/tools/ant/build.xml">` line, put `lombok.jar` in a `compile-libs` directory and add `<fileset dir="compile-libs" includes="*.jar" />` to the `<classpath>` of its `<javac>` task.

Source: https://projectlombok.org/setup/android (Lombok 1.18.48)

## Delombok

Delombok turns Java sources into Java sources with all Lombok transformations applied. Use it:
- to see exactly what Lombok generates ("under the hood") instead of guessing;
- for tools Lombok cannot plug into and that work on source files, such as **javadoc** and the Google Widget Toolkit (as the docs call it);
- to run source analysis tools on the code after Lombok has been applied.

It ships inside `lombok.jar`. Its standard mode copies a whole directory recursively into another one, skipping class files and transforming every Java source file.

### Command line

```shell
# Copy src/ to src-delomboked/ (created if missing), delomboked
java -jar lombok.jar delombok src -d src-delomboked

# Print one transformed file to standard output
java -jar lombok.jar delombok -p MyJavaFile.java
```

Options:
- `--classpath`, `--sourcepath`, `--module-path`: the javac options `-classpath`, `-sourcepath`, `--module-path`. Pass them when the sources need dependencies to resolve.
- `--format-help`: list of output formatting options (some, such as indent, are detected from the input).
- `--help`: all other options.

### Build tools

- **Maven**: third-party plugin by Anthony Whitford (https://github.com/awhitford/lombok.maven).
- **Gradle**: the `io.freefair.lombok` plugin makes delombok easy (see [Gradle](#gradle)).
- **sbt**: third-party plugin by Yang Bo (https://github.com/ThoughtWorksInc/sbt-delombok).
- **Ant**: `lombok.jar` contains a task. Example that delomboks and then runs javadoc:

```xml
<target name="javadoc">
<taskdef classname="lombok.delombok.ant.Tasks$Delombok" classpath="lib/lombok.jar" name="delombok" />
<mkdir dir="build/src-delomboked" />
<delombok verbose="true" encoding="UTF-8" to="build/src-delomboked" from="src">
    <format value="suppressWarnings:skip" />
</delombok>
<mkdir dir="build/api" />
<javadoc sourcepath="build/src-delomboked" defaultexcludes="yes" destdir="build/api" />
</target>
```

Instead of `from`, nested `<fileset>` elements work. The task accepts `sourcepath`, `classpath` and `modulepath` as attribute, nested element or nested refid, like the `javac` task.

### Limitations

- Comments may move a little, especially those in the middle of a syntax node: `public /*comment*/ static ...` moves the comment to the front of the modifiers. Source parsing tools are not affected.
- A source file with no Lombok transformations is copied unchanged.

Source: https://projectlombok.org/features/delombok (Lombok 1.18.48)

## Beyond the Lombok docs

- (Not from the Lombok docs) When `annotationProcessorPaths` is configured in Maven, only the processors listed there run; if the project uses other processors (for example MapStruct), list them all in that same block, Lombok first.
- (Not from the Lombok docs) The typical symptom of a missing processor setup is a compile error such as "cannot find symbol" for a getter, builder or `log` field that Lombok should have generated.
- (Not from the Lombok docs) Maven's `provided` scope is also on the test compile classpath, so Maven test sources need no extra dependency; the processor path from `maven-compiler-plugin` applies to `testCompile` too.
- (Not from the Lombok docs) Spring Boot's parent POM / BOM manages the Lombok version; if you set `lombok.version` explicitly, you override it.
- (Not from the Lombok docs) Keep the Lombok version of the Maven dependency and of `annotationProcessorPaths` identical; a shared property, as in the snippet, prevents drift.
- (Not from the Lombok docs) IDE support is separate from the build setup: a project can compile with Maven/Gradle and still show errors in an IDE without Lombok support, and the other way round.
