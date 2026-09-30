# Testing & Verification Rules

## 📌 Standard Overview
This rule document governs test requirements, quality gates, and static analysis procedures for this repository.

---

## 📋 Mandatory Rules & Guidelines

### 1. Mandatory Pre-Commit / Pre-Release Testing
All code modifications, UI updates, and resource adjustments must satisfy:
1. **Localization Parity Test:** `LocalizationParityTest` must pass without assertion failures (zero missing keys, zero extra keys, valid placeholders).
2. **Compilation Verification:** Gradle compilation (`./gradlew assembleDebug` / `./gradlew assembleRelease`) must finish without syntax or type errors.

### 2. Unit Testing Scope
- **String Resource Auditing:** Tests verify XML syntax validity, document parsing, key sets difference evaluation, and regex placeholder extraction.
- **Dynamic ABI & Architecture Verification:** Package installation and download workflows must be tested against 32-bit (`armeabi-v7a`), 64-bit (`arm64-v8a`), and x86 architectures.

### 3. CI/CD & Local Test Execution Command
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-21.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
./gradlew testDebugUnitTest
```
