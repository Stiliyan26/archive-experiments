# jlox - Crafting Interpreters

A Java implementation of the Lox programming language from the book "Crafting Interpreters" by Robert Nystrom.

## Setup

1. Make sure you have Java 11+ installed
2. Make sure you have Gradle installed, or use the included Gradle wrapper

## Running

### Option 1: Simple compilation (recommended for beginners)

```bash
./compile.sh
```

### Option 2: Using Gradle

If you have Gradle installed:

```bash
gradle run
```

Or use the Gradle wrapper (requires gradle-wrapper.jar):

```bash
./gradlew run
```

## Project Structure

- `src/main/java/com/craftinginterpreters/lox/Lox.java` - Main entry point
- `build.gradle` - Build configuration

## Development

Add your interpreter code to the `com.craftinginterpreters.lox` package or create additional classes in the `src/main/java/` directory.
