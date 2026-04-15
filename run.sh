#!/bin/bash
# Run script cho QuanLyDatVe Java Swing project

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
LIB_DIR="$PROJECT_DIR/lib"
JAR_NAME="$PROJECT_DIR/quanlydatvemaybay.jar"
BIN_DIR="$PROJECT_DIR/bin"

# Find JDBC jar
JDBC_JAR=""
for jar in "$LIB_DIR"/*.jar; do
    if [[ -f "$jar" ]]; then
        JDBC_JAR="$jar"
    fi
done

# Check if compiled classes exist
if [[ -d "$BIN_DIR" ]] && [[ -n "$(ls -A $BIN_DIR 2>/dev/null)" ]]; then
    # Run from compiled classes
    if [[ -n "$JDBC_JAR" ]]; then
        CP="$BIN_DIR:$JDBC_JAR"
    else
        CP="$BIN_DIR"
    fi
    echo "Running from compiled classes..."
    java -Dfile.encoding=UTF-8 -cp "$CP" com.quanlydatvemaybay.Main
elif [[ -f "$JAR_NAME" ]]; then
    # Run from JAR
    if [[ -n "$JDBC_JAR" ]]; then
        CP="$JAR_NAME:$JDBC_JAR"
    else
        CP="$JAR_NAME"
    fi
    echo "Running from JAR..."
    java -Dfile.encoding=UTF-8 -cp "$CP" com.quanlydatvemaybay.Main
else
    echo "Application not built yet. Running build.sh first..."
    chmod +x "$PROJECT_DIR/build.sh"
    "$PROJECT_DIR/build.sh"
    if [[ $? -eq 0 ]]; then
        exec "$0"
    fi
fi
