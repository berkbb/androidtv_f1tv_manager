# Localization & 1:1 Translation Rules

## 📌 Standard Overview
This rule document governs all multi-language assets, translation workflows, and locale synchronization across the application.

---

## 📋 Mandatory Rules & Guidelines

### 1. Supported Locales & Startup Detection
- **Base / Default Fallback Locale:** `English (en)` -> `app/src/main/res/values/strings.xml`
- **Turkish Locale:** `Turkish (tr)` -> `app/src/main/res/values-tr/strings.xml`
- **Romanian Locale:** `Romanian (ro)` -> `app/src/main/res/values-ro/strings.xml`
- **Startup Resolution:** Inspect system locale on boot. If system is `tr`, initialize as Turkish. If system is `ro`, initialize as Romanian. For all other languages, fallback to English (`en`).

### 2. Strict 1:1 Key Parity Invariant
Every `<string name="...">` defined in `values/strings.xml` must exist in all other localized `strings.xml` files with:
- The exact same key name.
- Non-empty, accurate translation in the target language.
- Zero untranslated dummy text or placeholder artifacts.

### 3. Format Specifier & Argument Integrity
- Format arguments such as `%1$s`, `%1$d`, `%2$s` must remain in exact argument alignment.
- Escaped characters such as percentage signs (`%%%1$d` or `%1$d%%`) and quotes (`\'`, `\"`) must be correctly escaped according to Android AAPT2 XML resource standards.

### 4. Language Switcher Contract
- The in-app language switcher cycle must follow: `EN` -> `TR` -> `RO` -> `EN`.
- Switching languages must execute live via dynamic `ConfigurationContext` without forcing activity destruction or resetting in-progress background operations.

### 5. Translation Audit Enforcement
- Whenever a string is added, removed, or modified in any locale file, all corresponding files must be updated simultaneously.
- Automated validation via `LocalizationParityTest` must be executed to verify key parity, placeholder alignment, and content completeness.
