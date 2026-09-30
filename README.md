# F1 TV Manager for Android TV & Google TV

A lightweight, dedicated Android TV companion and update manager built with **Kotlin** and **Jetpack Compose** designed for Android TV and Google TV devices running **Android 8.0 (API 26) or higher** (including Philips, Sony, TCL, Xiaomi, Chromecast, etc.).

---

## 🏎️ Background & Motivation

Official distribution of the **F1 TV** Android TV application via the Google Play Store restricts availability based on device hardware whitelists. Even though capable 4K Ultra HD smart TVs (such as Philips `49PUS7503/62` on the `TPM171E` chassis, Sony Bravia, TCL, etc.) run official Android TV (Android 8.0 Oreo+), F1 Digital Media does not whitelist many TV models.

Furthermore, official updates are distributed as multi-part Android App Bundles / Split APKMs (`base.apk` + architecture-specific splits like `config.armeabi_v7a.apk` + language/density splits). Traditional sideloading utilities often fail on split APKs or enforce restrictive third-party app store overhead.

**F1 TV Manager** solves this directly on the TV by automatically downloading, extracting, and streaming the latest 4K/UHD patched releases ([Alexvbp/f1tv-4k-patch](https://github.com/Alexvbp/f1tv-4k-patch)) via Android's native `PackageInstaller` Session API.

---

## ✨ Features

- **Direct GitHub Release Tracking:** Automatically checks GitHub API for the latest Alexvbp 4K/UHD release tag and compares it against the locally installed package (`com.formulaone.production`).
- **On-Device DEX Patching & Self-Signing Engine:** Includes a standalone on-device DEX bytecode patcher (`OnDevicePatcher.kt`) and local APK signer (`ApkSignerHelper.kt`), ensuring full independence even if upstream release repositories cease publishing.
- **Dynamic ABI & Architecture Detection:** Inspects TV processor architecture at runtime (`Build.SUPPORTED_ABIS`). Seamlessly selects 32-bit (`armeabi-v7a`) splits for older platforms (like MediaTek MT5596) or 64-bit (`arm64-v8a`) splits for newer 64-bit and Google TV models.
- **Native Split APK Streaming:** Extracts `.apkm` ZIP archives in cache and streams the exact required split APKs directly into Android's native `PackageInstaller.Session`. No root required; uses standard TV user-confirmation prompts.
- **Fresh Install & Update Handling:**
  - If F1 TV is **not installed**: Prompts to download and perform an initial installation.
  - If an **update is available**: Prompts to download and apply the update.
  - If **up to date**: Allows verification or clean reinstallation.
- **Trilingual UI (English default, Turkish & Romanian):** Fully localized interface with an instantaneous in-app language switcher (`🌐 EN` / `🌐 TR` / `🌐 RO`) that switches text and status live without restarting the app.
- **Automated Localization & Architecture Verification:** Full unit test suite (`LocalizationParityTest`, `DynamicArchitectureAndLocaleTest`, `OnDevicePatcherTest`) ensuring 1:1 key parity and ABI integrity.
- **100% D-Pad & TV Remote Optimized:** Built from scratch for 10-foot television viewing with high-contrast focus rings, custom button hover states, and loading indicators.
- **Ultra-Lightweight Footprint:** Native Jetpack Compose UI without bloated webview or cross-platform runtime overhead.

---

## 📺 Compatibility Matrix

| Platform / Series                        | Operating System               | Architecture           | Supported? | Notes                                        |
| :--------------------------------------- | :----------------------------- | :--------------------- | :--------: | :------------------------------------------- |
| **All Android TV & Google TV Sets**      | Android TV 8.0+ (Oreo to 14+)  | 32-bit / 64-bit ARM    |  **Yes**   | Philips, Sony, TCL, Xiaomi, Chromecast, etc. |
| **Philips TPM171E** (e.g. 49PUS7503)     | Android TV 8.0 Oreo            | 32-bit (`armeabi-v7a`) |  **Yes**   | Tested & verified                            |
| **Philips TPM181E / TPM191E**            | Android TV 9 Pie               | 32-bit / 64-bit ARM    |  **Yes**   | Fully compatible                             |
| **Philips TPM211E / TPM221E / TPM231E**  | Android TV 11 / Google TV      | 64-bit (`arm64-v8a`)   |  **Yes**   | Philips The One & OLED                       |
| **Legacy Android TV (Android < 8.0)**    | Android TV 5.0 – 7.1.2         | Any                    |   **No**   | Minimum SDK requires Android 8.0 (API 26+)   |
| **Philips Saphi OS / Titan OS**          | Proprietary Linux              | N/A                    |   **No**   | Linux tabanlıdır; APK çalıştıramaz           |
| **Samsung (Tizen OS)**                   | Tizen OS                       | N/A                    |   **No**   | Android tabanlı değildir; APK çalıştıramaz   |
| **LG (webOS)**                           | webOS                          | N/A                    |   **No**   | Android tabanlı değildir; APK çalıştıramaz   |

> [!WARNING]
> **Desteklenmeyen Sistemler / Unsupported Systems:**
> - **Philips Saphi OS / Titan OS** (Linux tabanlıdır, APK çalıştıramaz)
> - **Samsung (Tizen OS)** (Android tabanlı değildir, APK çalıştıramaz)
> - **LG (webOS)** (Android tabanlı değildir, APK çalıştıramaz)
> - **Eski Android TV (< 8.0)** (Minimum API 26 / Android 8.0 Oreo gereklidir)

---

## 🛠️ Tech Stack & Requirements

- **Language:** Kotlin 2.0.21
- **UI Framework:** Jetpack Compose (BOM 2024.10.01) + Material 3
- **Build System:** Gradle 8.11.1
- **Java Compatibility:** JDK 21 (LTS)
- **Minimum SDK:** API 26 (Android 8.0 Oreo or newer)
- **Target SDK:** API 34 (Android 14)

---

## 🚀 Building & Compilation / Nasıl Build Edilir?

### 💻 Yöntem 1: Terminal Üzerinden (Command Line)

Zulu OpenJDK 21 ve Android SDK yüklü olduğundan emin olun, ardından aşağıdaki komutları çalıştırın:

```bash
# 1. Java 21 yolunu tanımlayın (macOS)
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-21.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 2. Automated Unit Testleri Çalıştırın
./gradlew testDebugUnitTest

# 3. Debug APK dosyasını derleyin
./gradlew assembleDebug
```

> **İmzalı Release APK derlemek için:**
> ```bash
> ./gradlew assembleRelease
> ```

### 🛠️ Yöntem 2: Android Studio Üzerinden

1. Android Studio'yu açın ve proje kök dizinini (`androidtv_f1tv_manager`) açın (**Open**).
2. Gradle senkronizasyonunun tamamlanmasını bekleyin.
3. Üst menü çubuğundan **Build > Build Bundle(s) / APK(s) > Build APK(s)** seçeneğine tıklayın.

---

### 📍 Derlenen APK Konumu (Compiled APK Location)

Derleme işlemi bittiğinde oluşturulan APK dosyaları şu dizindedir:
- **Debug:** `app/build/outputs/apk/debug/f1tv-manager-v1.0.1-debug.apk`
- **Release:** `app/build/outputs/apk/release/f1tv-manager-v1.0.1-release.apk`

---

## ⚙️ Permissions & Developer Options / İzinler ve Geliştirici Seçenekleri

### 📺 Günlük Kullanım (Daily Usage & Updates)
* **Geliştirici Seçenekleri Gerekmez (No Developer Options Required):** Uygulama yüklendikten sonra F1 TV güncellemelerini indirmek ve yüklemek için TV Geliştirici Seçenekleri'nin açık olmasına gerek yoktur.
* **Bilinmeyen Uygulama Yükleme İzni:** Sadece standart Android *"Bilinmeyen uygulamaları yükle"* (`REQUEST_INSTALL_PACKAGES`) izni istenir. Uygulama ilk açıldığında sizi otomatik olarak bu ayara yönlendirir.

### 📦 Uygulamayı TV'ye İlk Kez Yükleme Yöntemleri (Initial Setup)

#### 🔹 Yöntem 1: USB Bellek veya Dosya Yöneticisi (Geliştirici Modu GEREKMEZ - Kolay)
1. `f1tv-manager-v1.0.1-release.apk` dosyasını bir USB belleğe kopyalayın veya **Send Files to TV** / **X-plore** uygulaması ile TV'ye aktarın.
2. TV'deki dosya yöneticisinden APK'ya tıklayıp yükleyin.

#### 🔹 Yöntem 2: ADB ile Ağ Üzerinden Yükleme (Geliştirici Seçenekleri Gerekir)
1. TV Ayarları > **Cihaz Tercihleri** > **Hakkında** > **Yapı Numarası**'na (Build Number) 7 kez basarak **Geliştirici Seçenekleri**'ni açın.
2. **Geliştirici Seçenekleri** altından **Ağ Hata Ayıklama / USB Hata Ayıklama** (Network / USB Debugging) seçeneğini etkinleştirin.
3. Bilgisayarınızdan TV'ye bağlanıp APK'yı yükleyin:

```bash
adb connect [TV_IP_ADDRESS]:5555
adb install -r app/build/outputs/apk/release/f1tv-manager-v1.0.1-release.apk
```

---

## 👤 Author & Credits

- **Developer:** **berkbb** ([https://berkbb.github.io](https://berkbb.github.io))
- **F1 TV 4K/UHD Patch:** [Alexvbp/f1tv-4k-patch](https://github.com/Alexvbp/f1tv-4k-patch)

---

## 📄 License

This project is open-source software licensed under the **[MIT License](file:///Users/berkbabadogan/Documents/GitHub/androidtv_f1tv_manager/LICENSE)**.

_Disclaimer: This tool is created for personal and educational use to maintain compatible access on Android TV and Google TV devices. F1 TV and Formula 1 are registered trademarks of Formula One Licensing B.V._
