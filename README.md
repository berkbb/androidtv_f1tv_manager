# F1 TV Updater for Unsuported Android TVs

A lightweight, dedicated Android TV companion application built with **Kotlin** and **Jetpack Compose** designed specifically for Philips / TP-Vision Smart TVs running Android TV and Google TV.

---

## 🏎️ Background & Motivation

Official distribution of the **F1 TV** Android TV application via the Google Play Store restricts availability based on device hardware whitelists. Even though high-end Philips Smart TVs (such as the `49PUS7503/62` on the `TPM171E` chassis) possess capable 4K Ultra HD panels and run official Android TV (Android 8.0 Oreo+), F1 Digital Media does not whitelist many TP-Vision TV models.

Furthermore, official updates are distributed as multi-part Android App Bundles / Split APKMs (`base.apk` + architecture-specific splits like `config.armeabi_v7a.apk` + language/density splits). Traditional sideloading utilities often fail on split APKs or enforce restrictive third-party app store overhead.

**F1 TV Updater** solves this directly on the TV by automatically downloading, extracting, and streaming the latest 4K/UHD patched releases ([Alexvbp/f1tv-4k-patch](https://github.com/Alexvbp/f1tv-4k-patch)) via Android's native `PackageInstaller` Session API.

---

## ✨ Features

- **Direct GitHub Release Tracking:** Automatically checks GitHub API for the latest Alexvbp 4K/UHD release tag and compares it against the locally installed package (`com.formulaone.production`).
- **Dynamic ABI & Architecture Detection:** Inspects TV processor architecture at runtime (`Build.SUPPORTED_ABIS`). Seamlessly selects 32-bit (`armeabi-v7a`) splits for older platforms (like MediaTek MT5596) or 64-bit (`arm64-v8a`) splits for newer Philips OLED and Google TV models.
- **Native Split APK Streaming:** Extracts `.apkm` ZIP archives in cache and streams the exact required split APKs directly into Android's native `PackageInstaller.Session`. No root required; uses standard TV user-confirmation prompts.
- **Fresh Install & Update Handling:**
  - If F1 TV is **not installed**: Prompts to download and perform an initial installation.
  - If an **update is available**: Prompts to download and apply the update.
  - If **up to date**: Allows verification or clean reinstallation.
- **Bilingual UI (English & Turkish):** Fully localized interface with an instantaneous in-app language switcher (`🌐 EN` / `🌐 TR`) that switches text and status without restarting the app.
- **100% D-Pad & TV Remote Optimized:** Built from scratch for 10-foot television viewing with high-contrast focus rings, custom button hover states, and loading indicators.
- **Ultra-Lightweight Footprint:** Native Jetpack Compose UI without bloated webview or cross-platform runtime overhead (~4 MB APK).

---

## 📺 Compatibility Matrix

| Platform / Series                       | Operating System          | Architecture           | Supported? | Notes                        |
| :-------------------------------------- | :------------------------ | :--------------------- | :--------: | :--------------------------- |
| **Philips TPM171E** (e.g. 49PUS7503)    | Android TV 8.0 Oreo       | 32-bit (`armeabi-v7a`) |  **Yes**   | Tested & verified            |
| **Philips QM152E / QM163E**             | Android TV 7 / 8          | 32-bit (`armeabi-v7a`) |  **Yes**   | Standard Android TV          |
| **Philips TPM181E / TPM191E**           | Android TV 9 Pie          | 32-bit / 64-bit        |  **Yes**   | Fully compatible             |
| **Philips TPM211E / TPM221E / TPM231E** | Android TV 11 / Google TV | 64-bit (`arm64-v8a`)   |  **Yes**   | Philips The One & OLED       |
| **Philips Saphi OS / Titan OS**         | Proprietary Linux         | N/A                    |   **No**   | Not Android; cannot run APKs |

---

## 🛠️ Tech Stack & Requirements

- **Language:** Kotlin 2.0.21
- **UI Framework:** Jetpack Compose (BOM 2024.10.01) + Material 3
- **Build System:** Gradle 8.11.1
- **Java Compatibility:** JDK 21 (LTS)
- **Minimum SDK:** API 26 (Android 8.0 Oreo)
- **Target SDK:** API 34 (Android 14)

---

## 🚀 Building & Installation

### 1. Build the APK

Ensure you have Zulu JDK 21 and the Android SDK installed:

```bash
cd /Users/berkbabadogan/Desktop/F1TVUpdaterPhilips
./gradlew assembleDebug
```

The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### 2. Install to TV via ADB

Connect to your Philips TV over your local network (enable USB / Network Debugging in TV Developer Options):

```bash
adb connect [IP_ADDRESS]:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 3. Launch the Application

```bash
adb shell am start -n com.babadogan.f1tv.updater/.MainActivity
```

---

## 👤 Author & Credits

- **Developer:** **berkbb** ([https://berkbb.github.io](https://berkbb.github.io))
- **F1 TV 4K/UHD Patch:** [Alexvbp/f1tv-4k-patch](https://github.com/Alexvbp/f1tv-4k-patch)

---

## 📄 License

This project is open-source software licensed under the **[MIT License](file:///Users/berkbabadogan/Desktop/F1TVUpdaterPhilips/LICENSE)**.

_Disclaimer: This tool is created for personal and educational use to maintain compatible access on Philips Android TV devices. F1 TV and Formula 1 are registered trademarks of Formula One Licensing B.V._
