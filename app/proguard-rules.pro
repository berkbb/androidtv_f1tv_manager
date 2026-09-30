# Keep Jetpack Compose & Kotlin coroutines
-keep class androidx.compose.** { *; }
-keep class androidx.tv.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Keep App classes and ViewModels
-keep class com.berkbb.f1tv.manager.** { *; }

# Keep Android PackageInstaller and Broadcast Receivers
-keep class * extends android.content.BroadcastReceiver
