# Testing & Verification Rules

## 📌 Standard Overview
This rule document governs test requirements, quality gates, and static analysis procedures for this repository.

---

## 📋 Mandatory Rules & Guidelines

### 1. Mandatory Pre-Commit / Pre-Release Testing ("Test is a MUST")
All code modifications, UI updates, architecture adjustments, and resource edits must satisfy:
1. **Automated Unit Test Suite:** `./gradlew test` must execute with 100% pass rate across `LocalizationParityTest`, `DynamicArchitectureAndLocaleTest`, `ReleaseResolverTest`, `ApkmCacheTest`, and `ManifestConfigurationTest`.
2. **Compilation & Assembly Verification:** `./gradlew assemble` must finish without syntax, type, or lint errors.

### 2. Unit Testing Scope
- **String Resource Auditing:** Tests verify XML syntax validity, document parsing, 1:1 key sets parity across EN, TR, and RO, and dynamic format placeholders (`%1$s`, `%1$d`, `%%%1$d`).
- **Dynamic ABI & Architecture Verification:** Package installation split filtering must be verified against 32-bit (`armeabi-v7a`), 64-bit (`arm64-v8a`), and x86 architectures.
- **Release Selection & Versioning:** Stable-tag filtering, numeric version comparison, and update status evaluation.
- **Download Integrity:** Tag-bound cache naming, SHA-256 / size verification, and stale bundle pruning.
- **Manifest Security:** `<queries>` declares F1 TV and `InstallResultReceiver` is not exported.

### 3. CI/CD & Local Test Execution Command
```bash
# Requires JAVA_HOME pointing at a JDK 17–21
./gradlew test
```
