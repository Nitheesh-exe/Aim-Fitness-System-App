#!/usr/bin/env bash
set -e

# ==============================================================================
# AIM Fitness Park - Single Setup File & Installer Packaging Script
# Generates self-contained native macOS .dmg and .pkg installers using jpackage.
# ==============================================================================

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

echo "🏋️‍♂️ Building AIM Fitness Park Application..."

# 1. Resolve JDK with JavaFX (Prefer Zulu 25 / 26)
if [ -z "$JAVA_HOME" ] || [ ! -x "$JAVA_HOME/bin/javac" ]; then
    if [ -d "/Library/Java/JavaVirtualMachines/zulu-25.jdk/Contents/Home" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/zulu-25.jdk/Contents/Home"
    elif [ -d "/Library/Java/JavaVirtualMachines/zulu-26.jdk/Contents/Home" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/zulu-26.jdk/Contents/Home"
    else
        export JAVA_HOME="$(/usr/libexec/java_home 2>/dev/null || true)"
    fi
fi

echo "Using Java Home: $JAVA_HOME"
"$JAVA_HOME/bin/java" -version

# 2. Build Clean JAR with Ant
echo "🔨 Compiling and packaging JAR..."
ant clean jar

# 3. Prepare packaging inputs
PKG_TMP="/tmp/aim_fitness_build_$$"
mkdir -p "$PKG_TMP/input/lib"
cp dist/Aim_0.0.jar "$PKG_TMP/input/"
cp dist/lib/*.jar "$PKG_TMP/input/lib/"
cp dist/lib/*.jar "$PKG_TMP/input/"

# 4. Generate native .icns if missing
mkdir -p resources
if [ ! -f "resources/AppIcon.icns" ]; then
    echo "🎨 Generating macOS AppIcon.icns..."
    ICON_TMP="/tmp/AppIcon_$$.iconset"
    mkdir -p "$ICON_TMP"
    sips -z 16 16     src/Login/icon.png --out "$ICON_TMP/icon_16x16.png" >/dev/null
    sips -z 32 32     src/Login/icon.png --out "$ICON_TMP/icon_16x16@2x.png" >/dev/null
    sips -z 32 32     src/Login/icon.png --out "$ICON_TMP/icon_32x32.png" >/dev/null
    sips -z 64 64     src/Login/icon.png --out "$ICON_TMP/icon_32x32@2x.png" >/dev/null
    sips -z 128 128   src/Login/icon.png --out "$ICON_TMP/icon_128x128.png" >/dev/null
    sips -z 256 256   src/Login/icon.png --out "$ICON_TMP/icon_128x128@2x.png" >/dev/null
    sips -z 256 256   src/Login/icon.png --out "$ICON_TMP/icon_256x256.png" >/dev/null
    sips -z 512 512   src/Login/icon.png --out "$ICON_TMP/icon_256x256@2x.png" >/dev/null
    sips -z 512 512   src/Login/icon.png --out "$ICON_TMP/icon_512x512.png" >/dev/null
    sips -z 1024 1024 src/Login/icon.png --out "$ICON_TMP/icon_512x512@2x.png" >/dev/null
    iconutil -c icns "$ICON_TMP" -o resources/AppIcon.icns
    rm -rf "$ICON_TMP"
fi

# 5. Run jpackage for DMG
mkdir -p dist/installer
echo "📦 Creating Apple Disk Image Installer (.dmg)..."
"$JAVA_HOME/bin/jpackage" \
  --type dmg \
  --input "$PKG_TMP/input" \
  --main-jar Aim_0.0.jar \
  --main-class Login.Launcher \
  --name "AIM-Fitness-Park" \
  --app-version "1.0.0" \
  --vendor "AIM Fitness Park" \
  --icon resources/AppIcon.icns \
  --java-options "--enable-native-access=javafx.graphics,ALL-UNNAMED" \
  --java-options "--add-modules=javafx.controls,javafx.fxml" \
  --dest dist/installer

# 6. Run jpackage for PKG
echo "📦 Creating macOS Installer Package (.pkg)..."
"$JAVA_HOME/bin/jpackage" \
  --type pkg \
  --input "$PKG_TMP/input" \
  --main-jar Aim_0.0.jar \
  --main-class Login.Launcher \
  --name "AIM-Fitness-Park" \
  --app-version "1.0.0" \
  --vendor "AIM Fitness Park" \
  --icon resources/AppIcon.icns \
  --java-options "--enable-native-access=javafx.graphics,ALL-UNNAMED" \
  --java-options "--add-modules=javafx.controls,javafx.fxml" \
  --dest dist/installer

rm -rf "$PKG_TMP"

echo ""
echo "🎉 Build Complete! Single setup installers available at:"
echo "   - DMG: $PROJECT_DIR/dist/installer/AIM-Fitness-Park-1.0.0.dmg"
echo "   - PKG: $PROJECT_DIR/dist/installer/AIM-Fitness-Park-1.0.0.pkg"
