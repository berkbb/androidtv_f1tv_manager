# Sentinel Test & Build Validation Skill

## 🎯 Purpose
Comprehensive build and unit testing procedure to validate full repository integrity before finalizing any agent task.

---

## 🛠️ Execution Procedure

```bash
# 1. Environment Setup
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-21.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 2. Run All Unit Tests
./gradlew testDebugUnitTest

# 3. Assemble Release Build
./gradlew assembleRelease
```
