# Changelog

All notable changes to the **F1 TV Updater for Philips Smart TVs** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
