# Domain Wiki & Business Workflows

## 🏎️ F1 TV Updater Architecture

### Problem Statement
Official F1 TV Android TV distribution restricts device compatibility through manufacturer whitelists on Google Play Store. Sideloading requires dealing with split APK bundles (`base.apk` + architecture + density + language splits).

### Solution Architecture
1. **GitHub API Integration:** Resolves the newest stable patched 4K/UHD build from `Alexvbp/f1tv-4k-patch` (tags like `v3.0.49.4`; `-dev` / `-shield-…` variants are pre-releases and skipped). Installed `versionName` format: `3.0.49.4-SP166.4.1-release-R54.2-tv`.
   - **Verified Cache:** Bundles are cached per tag and verified against the GitHub asset `digest` (`sha256:…`).
2. **Native PackageInstaller Session Pipeline:** In-memory extraction of `.apkm` ZIP archives to stream exact required splits to Android TV system without requiring third-party app stores or root permissions.
3. **Dynamic Hardware Detection:** Runtime inspection of `Build.MANUFACTURER`, `Build.MODEL`, `Build.PRODUCT`, `Build.SUPPORTED_ABIS`.
4. **Signing-Key Conflict:** Patched builds use the upstream keystore, so an official Play Store install must be uninstalled first; the app detects `STATUS_FAILURE_CONFLICT` and offers the system uninstall prompt.
5. **Package Visibility:** `<queries>` for `com.formulaone.production` is mandatory on Android 11+.
6. **Trilingual Localization:** 1:1 complete parity in English (default), Turkish, and Romanian.
