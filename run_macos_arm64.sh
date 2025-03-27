#!/bin/bash

# Script to run the application on macOS ARM64 (Apple Silicon)

# Set the Java library path to include ARM64 natives
export JAVA_OPTS="-XstartOnFirstThread -Djava.library.path=./natives/macos/arm64 -Dorg.lwjgl.librarypath=./natives/macos/arm64 -Dimgui.library.path=./natives/macos/arm64"

# Run the application with Gradle
./gradlew lwjgl3:run -Dorg.gradle.java.home=$JAVA_HOME