# Domain Wiki & Business Workflows

## 🏎️ F1 TV Updater Architecture

### Problem Statement
Official F1 TV Android TV distribution restricts device compatibility through manufacturer whitelists on Google Play Store. Sideloading requires dealing with split APK bundles (`base.apk` + architecture + density + language splits).

### Solution Architecture
1. **GitHub API Integration:** Resolves latest patched 4K/UHD builds from `Alexvbp/f1tv-4k-patch`.
2. **Native PackageInstaller Session Pipeline:** In-memory extraction of `.apkm` ZIP archives to stream exact required splits to Android TV system without requiring third-party app stores or root permissions.
3. **Dynamic Hardware Detection:** Runtime inspection of `Build.MANUFACTURER`, `Build.MODEL`, `Build.PRODUCT`, `Build.SUPPORTED_ABIS`.
4. **Trilingual Localization:** 1:1 complete parity in English (default), Turkish, and Romanian.
