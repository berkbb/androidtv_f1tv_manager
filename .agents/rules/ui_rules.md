# UI & Presentation Standards

## 📌 Standard Overview
Defines Android TV 10-foot UI design, remote D-Pad navigation, and dynamic presentation guidelines.

---

## 📋 Mandatory Rules & Guidelines

### 1. Hardcoded Text Ban
- No hardcoded string literals are permitted inside Compose `@Composable` functions.
- All display labels, titles, descriptions, and button texts must be retrieved via `stringResource(R.string.key)` or resource IDs.

### 2. 10-Foot TV Remote Usability
- Every interactive element (Button, Toggle, Modal) must support remote D-Pad focus with high-contrast visible focus rings.
- Focused states must present clear visual contrast (e.g., gold `#FFD700`, cyan `#00ADB5`, or light platinum `#E0E0E0` border).

### 3. Hardware Independence
- Device information displayed in header or dialogs must dynamically read `Build.MANUFACTURER`, `Build.MODEL`, `Build.PRODUCT`, and `Build.SUPPORTED_ABIS`.
- Specific brand names must never be hardcoded into UI layout files.
