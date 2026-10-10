# Project Roadmap & Enhancement Checklist

- [x] Lightweight Compose UI for 10-foot Android TV.
- [x] Dynamic ABI and split APK installer for 32-bit & 64-bit platforms.
- [x] Dynamic manufacturer and hardware detection.
- [x] Trilingual UI (English default, Turkish, Romanian) with 1:1 key parity.
- [x] Automated localization parity unit testing suite (`LocalizationParityTest`).
- [x] Release build configuration (version 1.0.2, code 3, dedicated release keystore support).
- [x] Verified, tag-bound download cache and numeric version comparison.
- [x] Storage pre-check, cancellable downloads, R8 shrinking, targetSdk 35 and dependency refresh.
- [x] AGP 9.4.1 / Gradle 9.7.1 / compileSdk 37.2 migration with latest AndroidX dependencies.

## Pending
- [ ] Raise `targetSdk` beyond 35 only after on-device behavior review on Android TV (compileSdk is already 37.2).
