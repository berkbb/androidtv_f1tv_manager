# Sentinel Test & Build Validation Skill

## 🎯 Purpose
Comprehensive build and unit testing procedure to validate full repository integrity before finalizing any agent task.

---

## 🛠️ Execution Procedure

```bash
# 1. Environment: JAVA_HOME must point at a JDK 17–21

# 2. Run All Unit Tests (debug variant is disabled; tests run against release)
./gradlew test

# 3. Assemble Release Build
./gradlew assembleRelease
```
