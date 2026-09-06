#!/bin/bash

# Create build directory if it doesn't exist
mkdir -p build/classes

# Compile the Java application to build directory
echo "Compiling Java application..."
find src/main/java -name "*.java" -exec javac -d build/classes {} +

# Run the application
echo "Running application..."
java -cp build/classes com.craftinginterpreters.lox.Lox "$@"
