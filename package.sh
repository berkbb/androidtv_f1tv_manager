#!/usr/bin/env bash
# Pitwall Updater release packager: runs the unit tests, assembles the release APK
# and copies it (with a SHA-256 checksum) into dist/.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

# JDK 17–21: keep an existing JAVA_HOME, otherwise resolve one dynamically on macOS
if [[ -z "${JAVA_HOME:-}" && -x /usr/libexec/java_home ]]; then
    for v in 21 17; do
        if JAVA_HOME="$(/usr/libexec/java_home -v "$v" 2>/dev/null)"; then
            export JAVA_HOME
            break
        fi
        unset JAVA_HOME
    done
fi
if [[ -z "${JAVA_HOME:-}" ]]; then
    echo "❌ JAVA_HOME is not set and no JDK 17–21 was found." >&2
    exit 1
fi
echo "☕ JAVA_HOME: $JAVA_HOME"

VERSION="$(sed -n 's/^val appVersionName = "\(.*\)"/\1/p' app/build.gradle.kts)"
if [[ -z "$VERSION" ]]; then
    echo "❌ Could not read appVersionName from app/build.gradle.kts." >&2
    exit 1
fi
APK_DIR="app/build/outputs/apk/release"
AAB_DIR="app/build/outputs/bundle/release"

if [[ ! -f keystore.properties && -z "${PITWALL_KEYSTORE_FILE:-}" ]]; then
    echo "⚠️  No release keystore configured; the packages will be signed with the debug key."
fi

echo "🧪 Running unit tests and building release v$VERSION (APKs + AAB)..."
# ABI splits and the AAB cannot share one Gradle invocation (AGP 9), so build them separately
./gradlew clean test assembleRelease -Ppitwall.abiSplits=true
./gradlew bundleRelease

# Expected outputs: universal + per-ABI APKs and the App Bundle
OUTPUTS=()
for variant in universal x86_64 armeabi-v7a arm64-v8a; do
    OUTPUTS+=("$APK_DIR/pitwall-updater-v${VERSION}-${variant}-release.apk")
done
OUTPUTS+=("$AAB_DIR/pitwall-updater-v${VERSION}-release.aab")

rm -rf dist
mkdir -p dist
for file in "${OUTPUTS[@]}"; do
    if [[ ! -f "$file" ]]; then
        echo "❌ Expected output not found: $file" >&2
        exit 1
    fi
    cp "$file" dist/
done
(cd dist && shasum -a 256 *.apk *.aab > SHA256SUMS)

echo "✅ Release packages ready in dist/:"
(cd dist && ls -lh *.apk *.aab | awk '{print "   " $NF " (" $5 ")"}')
echo "   SHA256SUMS"
