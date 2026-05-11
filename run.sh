#!/bin/bash
# Run script cho QuanLyDatVe Java Swing project

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
LIB_DIR="$PROJECT_DIR/lib"
JAR_NAME="$PROJECT_DIR/quanlydatvemaybay.jar"
BIN_DIR="$PROJECT_DIR/bin"

# Collect ALL jars in lib/
ALL_JARS=""
for jar in "$LIB_DIR"/*.jar; do
    if [[ -f "$jar" ]]; then
        ALL_JARS="$ALL_JARS:$jar"
    fi
done

# Check if compiled classes exist
if [[ -d "$BIN_DIR" ]] && [[ -n "$(ls -A $BIN_DIR 2>/dev/null)" ]]; then
    echo "Running from compiled classes..."
    java -Dfile.encoding=UTF-8 -cp "$BIN_DIR$ALL_JARS" com.quanlydatvemaybay.Main
elif [[ -f "$JAR_NAME" ]]; then
    echo "Running from JAR..."
    java -Dfile.encoding=UTF-8 -cp "$JAR_NAME$ALL_JARS" com.quanlydatvemaybay.Main
else
    echo "Application not built yet. Running build.sh first..."
    chmod +x "$PROJECT_DIR/build.sh"
    "$PROJECT_DIR/build.sh"
    if [[ $? -eq 0 ]]; then
        exec "$0"
    fi
fi
