# Reflexion Memory Log & Self-Correction Trajectories

## 🧠 Episodes & Lessons Learned

### Episode 1: Hardcoded Device Strings
- **Issue:** Hardcoded "Philips" and model numbers in UI and resource strings prevented universal Android TV support.
- **Resolution:** Replaced with runtime `Build.MANUFACTURER`, `Build.MODEL`, `Build.PRODUCT`, and `Build.SUPPORTED_ABIS` combined with dynamic localized string formatting `%1$s %2$s • %3$s (%4$s)`.

### Episode 2: Multi-Language Parity & Testing
- **Issue:** Adding new languages risks missing keys or mismatched format parameters (`%1$d`, `%%%1$d`).
- **Resolution:** Established mandatory 1:1 translation rules under `.agents/rules/localization_and_translation_rules.md` and implemented an automated `LocalizationParityTest` that validates key sets equality, non-empty values, and placeholder matching.
