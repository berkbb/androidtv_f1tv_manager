package com.berkbb.f1tv.manager

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

            // Cihazın mimarisini ve desteklenen ABI listesini dinamik tespit et
            val bestAbi = resolveBestAbi(android.os.Build.SUPPORTED_ABIS)

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val name = entry.name

                if (!shouldIncludeSplit(name, bestAbi)) continue

                // base.apk, cihaza uygun mimari spliti, dil ve ekran yoğunluğu (dpi) splitlerini yükle
                zip.getInputStream(entry).use { inStream ->
                    session.openWrite(name, 0, entry.size).use { outStream ->
                        inStream.copyTo(outStream)
                        session.fsync(outStream)
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

    fun resolveBestAbi(supportedAbis: Array<String>): String {
        val mapped = supportedAbis.map { it.replace("-", "_") }
        val knownAbis = listOf("arm64_v8a", "armeabi_v7a", "x86_64", "x86", "armeabi")
        return mapped.firstOrNull { it in knownAbis } ?: "armeabi_v7a"
    }

    fun extractAbi(entryName: String): String? {
        val normalized = entryName.replace("-", "_")
        val candidates = listOf("arm64_v8a", "armeabi_v7a", "x86_64", "x86", "armeabi")
        for (candidate in candidates) {
            val pattern = Regex("(?:^|[._-])$candidate(?:[._-]|$)")
            if (pattern.containsMatchIn(normalized)) {
                return candidate
            }
        }
        return null
    }

    fun shouldIncludeSplit(entryName: String, bestAbi: String): Boolean {
        if (!entryName.endsWith(".apk")) return false
        val splitAbi = extractAbi(entryName) ?: return true
        val normalizedBestAbi = bestAbi.replace("-", "_")
        return splitAbi == normalizedBestAbi
    }
}
