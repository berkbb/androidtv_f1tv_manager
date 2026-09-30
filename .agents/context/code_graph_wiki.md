# Code Graph Wiki & Symbol Index

## 🧬 Component Map

### 1. `MainActivity.kt`
- **Role:** Entry point, Jetpack Compose root, reactive `ConfigurationContext` locale manager, remote D-Pad focus handlers.
- **Key Symbols:** `MainActivity`, `F1TVUpdaterScreen`, `InfoCard`, `TvActionButton`.

### 2. `UpdaterViewModel.kt`
- **Role:** MVVM StateFlow manager, GitHub release polling, streaming HTTP download engine, install permission dispatcher.
- **Key Symbols:** `UpdaterViewModel`, `UpdaterUiState`, `InstallEvent`, `InstallEvents`.

### 3. `PackageInstallerHelper.kt`
- **Role:** Dynamic ABI resolution, `.apkm` ZIP extraction, and native `PackageInstaller.Session` streaming.
- **Key Symbols:** `PackageInstallerHelper.installApkm`.

### 4. `LocaleHelper.kt`
- **Role:** Pure dynamic locale resolution, system fallback, and language cycling engine.
- **Key Symbols:** `LocaleHelper.resolveInitialLanguage`, `LocaleHelper.getNextLanguage`.

### 5. `InstallResultReceiver.kt`
- **Role:** BroadcastReceiver listening for system package installation status intents.
- **Key Symbols:** `InstallResultReceiver`.

### 6. `LocalizationParityTest.kt` & `DynamicArchitectureAndLocaleTest.kt`
- **Role:** Automated unit tests asserting 1:1 XML string resource key parity, placeholder format integrity, non-empty content across EN, TR, and RO, and dynamic architecture / split selection.
- **Key Symbols:** `LocalizationParityTest`, `DynamicArchitectureAndLocaleTest`.
