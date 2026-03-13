#!/bin/bash

# X-O Network Arena Build Script (Bash)

# Create bin directory
mkdir -p bin

# Check if javac is available
JAVAC_CMD="javac"
if ! command -v "$JAVAC_CMD" &> /dev/null; then
    echo "javac not found in PATH. Searching for local JDKs..."
    
    # Common IntelliJ/User JDK paths on Windows (converted for bash/git bash)
    POTENTIAL_PATHS=(
        "$HOME/.jdks/openjdk-24.0.2/bin/javac.exe"
        "$HOME/.jdks/corretto-23.0.1/bin/javac.exe"
        "$HOME/.jdks/graalvm-jdk-21.0.2/bin/javac.exe"
        "/c/Program Files/Java/jdk*/bin/javac.exe"
    )
    
    for path in "${POTENTIAL_PATHS[@]}"; do
        if [ -f "$path" ]; then
            JAVAC_CMD="$path"
            echo "Found javac at: $JAVAC_CMD"
            break
        fi
    done
fi

if ! command -v "$JAVAC_CMD" &> /dev/null && [ "$JAVAC_CMD" == "javac" ]; then
    echo "ERROR: javac not found. Please install JDK or add it to PATH."
    exit 1
fi

echo "Compiling project..."

# Find all Java files and compile them
find src -name "*.java" > sources.txt
"$JAVAC_CMD" -d bin -cp "lib/*" @sources.txt

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    rm sources.txt
    echo "To run the server: java -cp bin:lib/* com.xonetwork.server.GameServer"
    echo "To run the client: java -cp bin com.xonetwork.client.GameClient"
else
    echo "Compilation failed. Please ensure Java JDK is installed and 'javac' is in your PATH."
    rm sources.txt
fi
