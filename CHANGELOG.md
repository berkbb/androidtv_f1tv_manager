# Changelog

All notable changes to the **F1 TV Manager for Android TV & Google TV** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.1] - 2026-09-30

### Added & Architecture
- **On-Device DEX Patching Engine:** Created standalone in-memory DEX bytecode patcher (`OnDevicePatcher.kt`) and local APK self-signing engine (`ApkSignerHelper.kt`), enabling direct on-device 4K/UHD unlocking independently of external release pipelines.
- **Dynamic ABI Resolution & Extraction:** Refactored `PackageInstallerHelper.kt` with exact ABI token matching for 32-bit (`armeabi-v7a`), 64-bit (`arm64-v8a`), and x86 architectures.
- **Automated Unit Testing Suite:** Created comprehensive test suites (`LocalizationParityTest.kt`, `DynamicArchitectureAndLocaleTest.kt`, `OnDevicePatcherTest.kt`) covering 1:1 key parity, placeholder formatting, locale fallbacks, and ABI split filtering.

### Localization & Branding
- **Rebranding to F1 TV Manager:** Updated application title across all locales (`F1 TV Manager` in EN, `F1 TV Yöneticisi` in TR, `Manager F1 TV` in RO) and migrated package namespace to `com.berkbb.f1tv.manager`.
- **Romanian (RO) Language Support:** Added 1:1 Romanian localization (`values-ro/strings.xml`) alongside English and Turkish.
- **English Default:** Configured English (`EN`) as the application's default startup language with instantaneous live switcher (`🌐 EN` / `🌐 TR` / `🌐 RO`).

### Build & Packaging
- **Release-Only Streamlined Packaging:** Disabled debug variant packaging to produce a single, optimized, self-signed production artifact: `f1tv-manager-v1.0.1-release.apk`.
- **Custom Output Naming:** Configured Gradle `applicationVariants` rule to dynamically format APK filenames according to release versioning.

### Legal & Documentation
- **Trademark & Non-Affiliation Disclaimers:** Added explicit legal disclaimers to [`LICENSE`](file:///Users/berkbabadogan/Documents/GitHub/androidtv_f1tv_manager/LICENSE), [`README.md`](file:///Users/berkbabadogan/Documents/GitHub/androidtv_f1tv_manager/README.md), and the in-app `ℹ️ Info` dialog confirming independent open-source status with zero official Formula 1 affiliation.
- **100% English Documentation Standard:** Standardized all repository `.md` files (`README.md`, `DEVELOPMENT.md`, `CHANGELOG.md`, `AGENTS.md`) exclusively in English.
- **Universal Android TV / Google TV Support:** Clarified full compatibility with all Android TV & Google TV devices running **Android 8.0 (API 26) or higher** (Philips, Sony, TCL, Xiaomi, Chromecast with Google TV, etc.).
- **Unsupported Systems Matrix:** Explicitly documented unsupported platforms:
  - Philips Saphi OS / Titan OS (Linux-based, cannot run APKs)
  - Samsung Smart TVs (Tizen OS)
  - LG Smart TVs (webOS)
  - Legacy Android TV sets running Android < 8.0 (below `minSdk = 26`).

---

## [1.0.0] - 2026-09-18

### Added
- **Initial Release:** Lightweight native Android TV companion application written in Kotlin and Jetpack Compose.
- **Dynamic ABI Resolution:** Runtime CPU architecture detection supporting both 32-bit (`armeabi-v7a`) for legacy MediaTek platforms (e.g. TPM171E) and 64-bit (`arm64-v8a`) for modern Philips Google TV sets.
- **GitHub Release Integration:** Automated release checking and `.apkm` artifact resolution using the Alexvbp/f1tv-4k-patch repository.
- **Split APK Session Installer:** In-memory extraction and streaming of multi-part Android App Bundles via native `PackageInstaller.Session` API without third-party store dependencies.
- **Full Bilingual Localization:** Complete English and Turkish translations for all UI elements, status messages, card titles, and progress indicators.
- **Live Language Switcher:** Interactive top-bar toggle (`🌐 EN` / `🌐 TR`) for instantaneous runtime language changes without restarting the activity.
- **Fresh Install Capability:** Automatic detection when F1 TV is missing, enabling single-click download and initial setup.
- **Bilingual About & Info Modal:** Integrated `ℹ️ Info` dialog showcasing author credits (`berkbb`, https://berkbb.github.io), upstream patch credits, and live TV hardware metrics with full remote D-pad and Back-key handling.
- **Permissions & Lifecycle Sync:** Automatic `REQUEST_INSTALL_PACKAGES` permission checking with direct Settings intent dispatch, and reactive UI state synchronization via `InstallEvents` and `onResume()`.
- **Smart Local Caching:** Automatically skips redundant 70MB downloads on re-installation / verification if valid cached packages exist.
- **F1 Themed 10-Foot UI:** High-contrast focus borders, D-pad navigation styling, and dynamic loading spinners for TV remote usability.
- **Direct Launch Shortcut:** Quick action button to launch F1 TV directly from the updater when installed.

### Technical & Environment
- Migrated build environment to **Zulu OpenJDK 21 LTS** for full compatibility with Gradle 8.11 and Kotlin 2.0.21.
- Configured double-clickable macOS desktop utility scripts (`install_f1tv_philips_tpm171e.command`) for automated host-to-TV deployment.
