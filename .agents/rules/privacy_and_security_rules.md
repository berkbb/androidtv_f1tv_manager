# Privacy, Sanitization & Security Rules

## 📌 Standard Overview
This rule document establishes strict privacy, metadata sanitization, and security invariants across all agent workflows and codebase operations.

---

## 📋 Mandatory Rules & Guidelines

### 1. Absolute Local Path Prohibition
- **Zero Local User System Paths:** Agents must never write, embed, or commit local system paths (e.g. `/Users/username/...`, `C:\Users\username\...`, `file:///Users/...`) into any source code, documentation file (`.md`), or build configuration.
- **Relative Markdown Links:** All documentation cross-references must use relative Markdown links (e.g. `[LICENSE](LICENSE)`).

### 2. Identity & Certificate Sanitization
- **Self-Signed Certificates:** Any programmatic or build-time X.509 certificate generation (e.g. in `ApkSignerHelper.kt`) must use standard open-source distinguished names:
  `CN=F1TVManager, O=OpenSource, C=US`
- **Zero Sensitive Data:** No private email addresses, personal phone numbers, physical addresses, API secrets, or passwords may be stored anywhere in the codebase.

### 3. Zero Telemetry & Privacy Preservation
- **No Analytics / Tracking SDKs:** The application does not include any analytics libraries, telemetry trackers, or advertising SDKs.
- **Minimal Network Surface:** The only permitted outbound HTTP calls are public GitHub release tag queries and `.apkm` asset downloads.
