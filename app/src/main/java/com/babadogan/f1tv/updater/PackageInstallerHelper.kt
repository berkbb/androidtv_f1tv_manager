package com.babadogan.f1tv.updater

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipFile

object PackageInstallerHelper {

    fun installApkm(context: Context, apkmFile: File): Boolean {
        val packageInstaller = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        params.setAppPackageName("com.formulaone.production")

        var sessionId = -1
        var session: PackageInstaller.Session? = null
        try {
            sessionId = packageInstaller.createSession(params)
            session = packageInstaller.openSession(sessionId)

            val zip = ZipFile(apkmFile)
            val entries = zip.entries()

            // Cihazın mimarisini dinamik tespit et (Tüm 32-bit ve 64-bit Philips TV'ler için)
            val supportedAbis = android.os.Build.SUPPORTED_ABIS.toList()
            val is64Bit = supportedAbis.contains("arm64-v8a")
            val targetAbi = if (is64Bit) "arm64_v8a" else "armeabi_v7a"

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val name = entry.name

                // TV'nin mimarisine (arm64-v8a veya armeabi-v7a) uygun splitleri al
                val isNeeded = name == "base.apk" ||
                        name.contains(targetAbi) ||
                        name.contains("config.en") ||
                        name.contains("dpi")

                if (isNeeded && name.endsWith(".apk")) {
                    zip.getInputStream(entry).use { inStream ->
                        session.openWrite(name, 0, entry.size).use { outStream ->
                            inStream.copyTo(outStream)
                            session.fsync(outStream)
                        }
                    }
                }
            }
            zip.close()

            // Kurulumu başlat (Android TV yerel onay kutucuğunu açar)
            val intent = Intent(context, InstallResultReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            session.commit(pendingIntent.intentSender)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                session?.abandon()
            } catch (_: Exception) {}
            return false
        } finally {
            try {
                session?.close()
            } catch (_: Exception) {}
        }
    }
}
