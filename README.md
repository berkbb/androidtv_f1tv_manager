# F1 TV Manager for Android TV & Google TV

A lightweight, dedicated Android TV companion and update manager built with **Kotlin** and **Jetpack Compose** designed for Android TV and Google TV devices running **Android 8.0 (API 26) or higher** (including Philips, Sony, TCL, Xiaomi, Chromecast, etc.).

---

## 🏎️ Background & Motivation

Official distribution of the **F1 TV** Android TV application via the Google Play Store restricts availability based on device hardware whitelists. Even though capable 4K Ultra HD smart TVs (such as Philips `49PUS7503/62` on the `TPM171E` chassis, Sony Bravia, TCL, etc.) run official Android TV (Android 8.0 Oreo+), F1 Digital Media does not whitelist many TV models.

Furthermore, official updates are distributed as multi-part Android App Bundles / Split APKMs (`base.apk` + architecture-specific splits like `config.armeabi_v7a.apk` + language/density splits). Traditional sideloading utilities often fail on split APKs or enforce restrictive third-party app store overhead.

**F1 TV Manager** solves this directly on the TV by automatically downloading, extracting, and streaming the latest 4K/UHD patched releases ([Alexvbp/f1tv-4k-patch](https://github.com/Alexvbp/f1tv-4k-patch)) via Android's native `PackageInstaller` Session API.

---

## ✨ Features

- **Direct GitHub Release Tracking:** Automatically checks GitHub API for the latest Alexvbp 4K/UHD release tag and compares it against the locally installed package (`com.formulaone.production`).
- **On-Device DEX Patching & Self-Signing Engine:** Includes a standalone on-device DEX bytecode patcher (`OnDevicePatcher.kt`) and local APK signer (`ApkSignerHelper.kt`), ensuring full independence even if upstream release repositories cease publishing.
- **Dynamic ABI & Architecture Detection:** Inspects TV processor architecture at runtime (`Build.SUPPORTED_ABIS`). Seamlessly selects 32-bit (`armeabi-v7a`) splits for older platforms (like MediaTek MT5596) or 64-bit (`arm64-v8a`) splits for newer 64-bit and Google TV models.
- **Native Split APK Streaming:** Extracts `.apkm` ZIP archives in cache and streams the exact required split APKs directly into Android's native `PackageInstaller.Session`. No root required; uses standard TV user-confirmation prompts.
- **Fresh Install & Update Handling:**
  - If F1 TV is **not installed**: Prompts to download and perform an initial installation.
  - If an **update is available**: Prompts to download and apply the update.
  - If **up to date**: Allows verification or clean reinstallation.
- **Trilingual UI (English default, Turkish & Romanian):** Fully localized interface with an instantaneous in-app language switcher (`🌐 EN` / `🌐 TR` / `🌐 RO`) that switches text and status live without restarting the app.
- **Automated Localization & Architecture Verification:** Full unit test suite (`LocalizationParityTest`, `DynamicArchitectureAndLocaleTest`, `OnDevicePatcherTest`) ensuring 1:1 key parity and ABI integrity.
- **100% D-Pad & TV Remote Optimized:** Built from scratch for 10-foot television viewing with high-contrast focus rings, custom button hover states, and loading indicators.
- **Ultra-Lightweight Footprint:** Native Jetpack Compose UI without bloated webview or cross-platform runtime overhead.

---

## 📺 Compatibility Matrix

| Platform / Series                        | Operating System               | Architecture           | Supported? | Notes                                        |
| :--------------------------------------- | :----------------------------- | :--------------------- | :--------: | :------------------------------------------- |
| **All Android TV & Google TV Sets**      | Android TV 8.0+ (Oreo to 14+)  | 32-bit / 64-bit ARM    |  **Yes**   | Philips, Sony, TCL, Xiaomi, Chromecast, etc. |
| **Philips TPM171E** (e.g. 49PUS7503)     | Android TV 8.0 Oreo            | 32-bit (`armeabi-v7a`) |  **Yes**   | Tested & verified                            |
| **Philips TPM181E / TPM191E**            | Android TV 9 Pie               | 32-bit / 64-bit ARM    |  **Yes**   | Fully compatible                             |
| **Philips TPM211E / TPM221E / TPM231E**  | Android TV 11 / Google TV      | 64-bit (`arm64-v8a`)   |  **Yes**   | Philips The One & OLED                       |
| **Legacy Android TV (Android < 8.0)**    | Android TV 5.0 – 7.1.2         | Any                    |   **No**   | Minimum SDK requires Android 8.0 (API 26+)   |
| **Philips Saphi OS / Titan OS**          | Proprietary Linux              | N/A                    |   **No**   | Linux-based; cannot execute Android APKs     |
| **Samsung (Tizen OS)**                   | Tizen OS                       | N/A                    |   **No**   | Not Android-based; cannot execute APKs       |
| **LG (webOS)**                           | webOS                          | N/A                    |   **No**   | Not Android-based; cannot execute APKs       |

> [!WARNING]
> **Unsupported Systems:**
> - **Philips Saphi OS / Titan OS** (Linux-based, cannot run Android APKs)
> - **Samsung (Tizen OS)** (Not Android-based, cannot run Android APKs)
> - **LG (webOS)** (Not Android-based, cannot run Android APKs)
> - **Legacy Android TV (< 8.0)** (Requires minimum API 26 / Android 8.0 Oreo)

---

## 🛠️ Tech Stack & Requirements

- **Language:** Kotlin 2.0.21
- **UI Framework:** Jetpack Compose (BOM 2024.10.01) + Material 3
- **Build System:** Gradle 8.11.1
- **Java Compatibility:** JDK 21 (LTS)
- **Minimum SDK:** API 26 (Android 8.0 Oreo or newer)
- **Target SDK:** API 34 (Android 14)

---

## 🚀 Building & Compilation

### 💻 Method 1: Command Line Interface (CLI)

Ensure Zulu OpenJDK 21 and the Android SDK are installed, then run the following commands:

```bash
# 1. Set Java 21 environment path (macOS)
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-21.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 2. Run automated unit test suite
./gradlew testDebugUnitTest

# 3. Assemble Debug APK
./gradlew assembleDebug

# 4. Assemble Signed Release APK
./gradlew assembleRelease
```

### 🛠️ Method 2: Android Studio

1. Open Android Studio and select **Open**, targeting the repository root (`androidtv_f1tv_manager`).
2. Wait for Gradle synchronization to finish.
3. From the top menu bar, select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

---

### 📍 Compiled APK Locations

Upon build completion, generated APK files are located at:
- **Debug:** `app/build/outputs/apk/debug/f1tv-manager-v1.0.1-debug.apk`
- **Release:** `app/build/outputs/apk/release/f1tv-manager-v1.0.1-release.apk`

---

## ⚙️ Permissions & TV Installation Guide

### 📺 Daily Usage & In-App Updates
* **No Developer Options Required:** Once the manager app is installed, regular F1 TV updates do not require TV Developer Options to remain enabled.
* **Install Unknown Apps Permission:** Only the standard Android *"Install unknown apps"* (`REQUEST_INSTALL_PACKAGES`) permission is needed. The application automatically redirects you to this system setting upon initial run.

### 📦 Initial TV Installation Methods

#### 🔹 Method 1: USB Flash Drive or File Manager (No Developer Mode Required - Easy)
1. Copy `f1tv-manager-v1.0.1-release.apk` to a USB flash drive or transfer via **Send Files to TV** / **X-plore**.
2. Open the file manager on your TV and install the APK.

#### 🔹 Method 2: Network ADB Sideloading (Requires TV Developer Options)
1. Go to TV Settings > **Device Preferences** > **About** > Click **Build Number** 7 times to enable **Developer Options**.
2. Under **Developer Options**, enable **Network / USB Debugging**.
3. Connect and install from your computer:

```bash
adb connect [TV_IP_ADDRESS]:5555
adb install -r app/build/outputs/apk/release/f1tv-manager-v1.0.1-release.apk
```

---

## 👤 Author & Credits

- **Developer:** **berkbb** ([https://berkbb.github.io](https://berkbb.github.io))
- **F1 TV 4K/UHD Patch:** [Alexvbp/f1tv-4k-patch](https://github.com/Alexvbp/f1tv-4k-patch)

---

## 📄 License & Legal Disclaimers

This project is open-source software licensed under the **[MIT License](file:///Users/berkbabadogan/Documents/GitHub/androidtv_f1tv_manager/LICENSE)**.

### ⚖️ Trademark & Non-Affiliation Notice
- **Formula 1, F1, FORMULA ONE, F1 TV**, and related marks, logos, and emblems are registered trademarks of **Formula One Licensing B.V.**, a Formula 1 company.
- This project is an **independent, unofficial open-source utility** developed strictly for personal, educational, and interoperability purposes on Android TV and Google TV platforms.
- This software is **not affiliated with, endorsed by, sponsored by, authorized by, or in any way officially connected** with Formula One Licensing B.V., Formula One Group, Liberty Media, or any of their subsidiaries or affiliates.
- This repository does not host, distribute, or stream any copyrighted audio, video, or proprietary application binaries. All installation operations interact with user-authorized, publicly accessible packages using standard Android APIs.
