# abbayedesmorts-java

Port of [Abbaye Des Morts GPL version](https://github.com/nevat/abbayedesmorts-gpl) to Java / LWJGL.

## Requirements

* Java 17+

* Maven

## Build

```
mvn clean spotless:apply package
```

## Run

```
java -XstartOnFirstThread -jar target/abbayedesmorts-1.0.0-SNAPSHOT.jar
```

or
```
mvn exec:java
```

The -XstartOnFirstThread flag is only required on Mac

## CREDITS

Ben Evans @kittylyst
