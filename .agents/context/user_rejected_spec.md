# User Rejected Specs & Redlines

## 🚫 Strictly Forbidden Implementations

1. **Non-1:1 Translations / Missing Localization Keys:**
   - Any commit or pull request introducing an untranslated string or missing key across EN, TR, and RO is strictly prohibited.
2. **Hardcoded Vendor Logic:**
   - Hardcoding specific TV brands or chassis names without dynamic runtime hardware detection is prohibited.
3. **Targeting Unsupported Platforms:**
   - Attempting to support non-Android OS smart TVs (Philips Saphi OS, Philips Titan OS, Samsung Tizen OS, LG webOS) via Android APK mechanisms is prohibited.
   - Targeting Android < 8.0 without addressing API 26 PackageInstaller requirements is prohibited.
