# Development Guide

This guide details the architectural decisions, project structure, developer workflows, and debugging procedures for the **F1 TV Manager for Android TV & Google TV (Android 8.0+)**.

---

## 🏗️ Architecture & Component Overview

The application follows Modern Android Architecture principles:

- **Presentation Layer:** Jetpack Compose (BOM `2024.10.01`) + Material 3.
- **State Management:** MVVM pattern using Kotlin Coroutines and `StateFlow`.
- **System Integration:** Android `PackageInstaller` Session API via `PackageInstallerHelper`.

```
com.berkbb.f1tv.manager/
├── MainActivity.kt               # Compose UI entry point, D-Pad focus & locale provider
├── LocaleHelper.kt               # Dynamic locale resolver & cycling helper (EN, TR, RO)
├── UpdaterViewModel.kt           # StateFlow UI state, GitHub API client, streaming download
├── PackageInstallerHelper.kt     # Multi-split APK extraction & PackageInstaller session pipeline
├── OnDevicePatcher.kt            # On-device DEX bytecode patcher for 4K/UHD unlocking
├── ApkSignerHelper.kt            # Local APK JAR/Zip signer with self-signed certificate
└── InstallResultReceiver.kt      # BroadcastReceiver listening for package install status intents
```

### 1. `MainActivity.kt`

- Implements a reactive `CompositionLocalProvider` wrapping `LocalConfiguration` and `LocalContext` created with `createConfigurationContext()`. This enables instantaneous switching between English and Turkish without restarting the process.
- Implements `TvActionButton` with `MutableInteractionSource` and `collectIsFocusedAsState()` to render TV D-pad focus states:
  - Default Check Button: Platinum/White outline on focus (`#E0E0E0`).
  - Action / Update Button: F1 Red default with Championship Gold outline on focus (`#FFD700`).
  - Language Toggle: Cyan focus ring (`#00ADB5`).

### 2. `UpdaterViewModel.kt`

- Manages `UpdaterUiState`:
  ```kotlin
  data class UpdaterUiState(
      val installedVersion: String = "",
      val latestVersion: String = "",
      val downloadUrl: String = "",
      val isInstalled: Boolean = false,
      val isUpdateAvailable: Boolean = false,
      val isLoading: Boolean = false,
      val isDownloading: Boolean = false,
      val downloadProgress: Int = 0,
      val statusResId: Int = R.string.status_detecting,
      val statusArg: String = ""
  )
  ```
- Uses string resource IDs (`statusResId`) instead of pre-rendered strings to allow Compose to re-evaluate localized text immediately upon language changes.
- Fetches the latest release from the GitHub API:
  `https://api.github.com/repos/Alexvbp/f1tv-4k-patch/releases/latest`
  and parses the `.apkm` download asset URL.

### 3. `PackageInstallerHelper.kt`

- Resolves the device's supported ABIs from `android.os.Build.SUPPORTED_ABIS`.
- Reads the downloaded `.apkm` (ZIP archive) and extracts only:
  - `base.apk`
  - Architecture-specific split (`config.armeabi_v7a.apk` or `config.arm64_v8a.apk`)
  - Screen density split (matching device display density, or `config.xxhdpi.apk`)
  - Language splits (e.g. `config.en.apk`, `config.tr.apk` if present)
- Streams chunks directly to `PackageInstaller.openSession()` using `session.openWrite()`.
- Creates a `PendingIntent` targeted to `InstallReceiver` to receive status feedback (`STATUS_PENDING_USER_ACTION` launches the TV confirmation dialog).

---

## 💻 Development Environment Setup

### Prerequisites

- **macOS** (Apple Silicon arm64 or Intel)
- **Java Development Kit:** Zulu OpenJDK 21 LTS (`/Library/Java/JavaVirtualMachines/zulu-21.jdk`)
- **Android SDK Tools:** `adb` located at `/Users/berkbabadogan/SDK/Android/platform-tools/adb`
- **Gradle:** 8.11.1 (via `./gradlew` wrapper)

### Gradle Configuration (`gradle.properties`)

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
org.gradle.java.home=/Library/Java/JavaVirtualMachines/zulu-21.jdk/Contents/Home
```

---

## 🔍 Validation & Testing Workflows

### 1. Connecting to the Target TV

Ensure the TV is on the same local Wi-Fi / Ethernet subnet:

```bash
adb connect [IP_ADDRESS]:5555
adb devices
```

### 2. Running Automated Unit Tests

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-21.jdk/Contents/Home
./gradlew testDebugUnitTest
```

### 3. Building & Deploying

```bash
# Debug build
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/f1tv-manager-v1.0.1-debug.apk

# Release build
./gradlew assembleRelease
adb install -r app/build/outputs/apk/release/f1tv-manager-v1.0.1-release.apk
```

### 4. Remote Inspection & Headless Testing

To inspect the TV interface remotely:

```bash
# Capture TV screenshot
adb exec-out screencap -p > /tmp/tv_screen.png

# Simulate remote D-Pad navigation
adb shell input keyevent 19  # DPAD_UP
adb shell input keyevent 20  # DPAD_DOWN
adb shell input keyevent 21  # DPAD_LEFT
adb shell input keyevent 22  # DPAD_RIGHT
adb shell input keyevent 23  # DPAD_CENTER (Select)
adb shell input keyevent 66  # ENTER
```

---

## 🛡️ Key Lessons Learned & Gotchas

1. **Signature Differences:**
   Official Google Play Store APKs and GitHub patched UHD builds use different signing keys. If replacing an existing official build via `PackageInstaller`, Android will reject with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`. Sideloading requires a one-time uninstall of the official package first.
2. **Java 27 Incompatibility:**
   Kotlin Gradle Plugin (`2.0.21`) and Gradle `8.11` fail on JDK 25+ / 27 with `IllegalArgumentException: 27`. Always maintain Zulu JDK 21 LTS as the build runtime.
3. **Philips Standby State:**
   Philips Android TVs enter low-power deep standby after extended inactivity, which may temporarily terminate the ADB daemon. Waking the TV via remote or sending `adb shell input keyevent 224` (`KEYCODE_WAKEUP`) restores network connectivity.
4. **Unsupported Smart TV Platforms:**
   - **Philips Saphi OS / Titan OS:** Linux-based; cannot run APKs.
   - **Samsung (Tizen OS) & LG (webOS):** Proprietary operating systems; cannot run Android APKs.
   - **Android < 8.0:** Unsupported due to `minSdk = 26` requirement.
