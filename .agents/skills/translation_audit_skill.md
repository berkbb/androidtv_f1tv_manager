# Translation Audit & 1:1 Parity Skill

## 🎯 Purpose
Automated procedure for checking 1:1 parity across all supported language resource files (`values/`, `values-tr/`, `values-ro/`).

---

## 🛠️ Execution Procedure

1. **Verify Existence of All Resource Files:**
   - English (Base): `app/src/main/res/values/strings.xml`
   - Turkish: `app/src/main/res/values-tr/strings.xml`
   - Romanian: `app/src/main/res/values-ro/strings.xml`

2. **Execute Automated Parity Test:**
   ```bash
   export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-21.jdk/Contents/Home
   export PATH=$JAVA_HOME/bin:$PATH
   ./gradlew testDebugUnitTest --tests com.berkbb.f1tv.manager.LocalizationParityTest
   ```

3. **Validate Checks:**
   - Zero missing keys across English, Turkish, Romanian.
   - Zero extra / orphan keys.
   - Format parameter match (`%1$s`, `%1$d`, `%%%1$d`).
   - Non-blank string values.
