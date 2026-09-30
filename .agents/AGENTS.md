# AGENTS.md - Master Constitution & Governance Protocol

## 🎯 Purpose & Scope
This repository implements the **F1 TV Manager for Android TV and Google TV** (Android 8.0 Oreo and above).
All AI agents operating within this repository must adhere strictly to this constitution, the declarative domain rules under `.agents/rules/`, and the structured procedural memory under `.agents/skills/`.

---

## 🏛️ Core Constitutional Invariants

### 1. Mandatory 1:1 Translation & Localization Rule
- **Trilingual Parity (EN, TR, RO):** Every string resource defined in the primary English locale (`values/strings.xml`) must possess an exact, 1:1 corresponding key in Turkish (`values-tr/strings.xml`) and Romanian (`values-ro/strings.xml`).
- **Default Locale:** The default application language is **English (`en`)**.
- **Zero Missing / Orphan Keys:** No key may exist in a localized file without existing in the base English resource file, and no key may be omitted.
- **Placeholder Parity:** All dynamic format specifiers (e.g. `%1$s`, `%1$d`, `%%%1$d`) must match in type, index, and quantity across all translation files.
- **No Hardcoded Display Strings:** UI components must never contain hardcoded display strings; all text must be accessed through Compose `stringResource(R.string.key)` or resource IDs.

### 2. Mandatory Verification & Automated Testing Rule
- Any change affecting resources, locales, or APK installation workflows must pass the automated parity test suite (`LocalizationParityTest.kt`).
- Before marking any release task complete, agents must execute or verify test coverage across key parity, placeholder integrity, and non-empty translations.

### 3. Dynamic Hardware & Platform Rule
- **Universal Android TV / Google TV Support:** The codebase must dynamically inspect hardware parameters (`Build.MANUFACTURER`, `Build.MODEL`, `Build.PRODUCT`, `Build.SUPPORTED_ABIS`) at runtime.
- **Zero Static Vendor Assumptions:** No hardcoded TV brand names (e.g., Philips, Sony, TCL) in code or logic.
- **Supported Platforms:** All Android TV / Google TV devices on Android 8.0+ (API 26+).
- **Unsupported Redline:** Non-Android TV operating systems (Philips Saphi OS / Titan OS, Samsung Tizen OS, LG webOS) and legacy Android TV (< 8.0) must never be targeted with incompatible APK installation workflows.

### 4. Language Standard for `.agents/`
- **Strict English Requirement:** All files within `.agents/` (`AGENTS.md`, `rules/`, `skills/`, `context/`) must be written exclusively in English.

---

## 🧠 Memory Structure & Navigation
- **Domain Rules:** Consult `.agents/rules/` before modifying UI, translations, or build scripts.
- **Procedural Skills:** Use `.agents/skills/` for translation auditing and verification routines.
- **Context & Knowledge Assets:** Consult `.agents/context/` for code graphs, domain models, and roadmaps.
