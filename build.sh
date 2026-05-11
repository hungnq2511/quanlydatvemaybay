#!/bin/bash
# Build script cho QuanLyDatVe Java Swing project

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$PROJECT_DIR/src"
BIN_DIR="$PROJECT_DIR/bin"
LIB_DIR="$PROJECT_DIR/lib"
JAR_NAME="quanlydatvemaybay.jar"

echo "===== Quản Lý Đặt Vé Máy Bay - Build Script ====="

# Check Java
if ! command -v javac &> /dev/null; then
    echo "ERROR: Java not found. Please install JDK 11 or later."
    exit 1
fi

echo "Java version: $(java -version 2>&1 | head -1)"

# Collect all JARs in lib/
JDBC_JAR=""
MAIL_JAR=""
ALL_JARS=""
for jar in "$LIB_DIR"/*.jar; do
    if [[ -f "$jar" ]]; then
        name=$(basename "$jar")
        echo "Found library: $jar"
        ALL_JARS="$ALL_JARS:$jar"
        if [[ "$name" == ojdbc* ]]; then JDBC_JAR="$jar"; fi
        if [[ "$name" == javax.mail* ]]; then MAIL_JAR="$jar"; fi
    fi
done

if [[ -z "$JDBC_JAR" ]]; then
    echo "WARNING: Oracle JDBC driver not found in lib/ directory!"
    echo "Please download ojdbc8.jar from Oracle and place it in: $LIB_DIR/"
    echo ""
    echo "Download from: https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html"
    echo ""
    read -p "Continue build without JDBC driver? (y/n): " choice
    if [[ "$choice" != "y" ]]; then exit 1; fi
    CLASSPATH="$SRC_DIR"
else
    CLASSPATH="$SRC_DIR$ALL_JARS"
fi

if [[ -z "$MAIL_JAR" ]]; then
    echo "WARNING: javax.mail-1.6.2.jar not found in lib/ — email features will not work."
    echo "Copy javax.mail-1.6.2.jar from ~/.m2/repository/com/sun/mail/javax.mail/1.6.2/ to lib/"
fi

# Clean
echo ""
echo "Cleaning bin directory..."
rm -rf "$BIN_DIR"
mkdir -p "$BIN_DIR"

# Find all Java files
echo "Finding source files..."
JAVA_FILES=$(find "$SRC_DIR" -name "*.java" -type f)
FILE_COUNT=$(echo "$JAVA_FILES" | wc -l)
echo "Found $FILE_COUNT Java files"

# Compile
echo ""
echo "Compiling..."
javac -encoding UTF-8 -cp "$CLASSPATH" -d "$BIN_DIR" $JAVA_FILES

if [[ $? -ne 0 ]]; then
    echo "COMPILATION FAILED!"
    exit 1
fi

echo "Compilation successful!"

# Create JAR
echo ""
echo "Creating JAR..."

# Create manifest with all lib JARs on Class-Path
MANIFEST="$BIN_DIR/MANIFEST.MF"
echo "Main-Class: com.quanlydatvemaybay.Main" > "$MANIFEST"
CP_LINE=""
for jar in "$LIB_DIR"/*.jar; do
    [[ -f "$jar" ]] && CP_LINE="$CP_LINE lib/$(basename "$jar")"
done
if [[ -n "$CP_LINE" ]]; then
    echo "Class-Path:$CP_LINE" >> "$MANIFEST"
fi

cd "$BIN_DIR"
jar cfm "$PROJECT_DIR/$JAR_NAME" "$MANIFEST" -C "$BIN_DIR" .

if [[ $? -eq 0 ]]; then
    echo "JAR created: $PROJECT_DIR/$JAR_NAME"
else
    echo "JAR creation failed!"
    exit 1
fi

echo ""
echo "===== Build Complete ====="
echo ""
echo "To run the application:"
if [[ -n "$ALL_JARS" ]]; then
    echo "  java -cp \"$JAR_NAME$ALL_JARS\" com.quanlydatvemaybay.Main"
else
    echo "  java -jar $JAR_NAME"
fi
echo ""
echo "Or use run.sh:"
echo "  ./run.sh"
