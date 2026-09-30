package com.berkbb.f1tv.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class OnDevicePatcherTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createDummyApk(file: File, entries: Map<String, String>) {
        ZipOutputStream(FileOutputStream(file)).use { zipOut ->
            entries.forEach { (name, content) ->
                val entry = ZipEntry(name)
                zipOut.putNextEntry(entry)
                zipOut.write(content.toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()
            }
        }
    }

    @Test
    fun testApkSignerCreatesValidSignedJar() {
        val unsignedApk = tempFolder.newFile("dummy_unsigned.apk")
        createDummyApk(unsignedApk, mapOf("AndroidManifest.xml" to "<manifest/>", "classes.dex" to "dex035_dummy"))

        val signedApk = tempFolder.newFile("dummy_signed.apk")
        val success = ApkSignerHelper.signApk(unsignedApk, signedApk)

        assertTrue("Signing must succeed", success)
        assertTrue("Signed APK must exist and not be empty", signedApk.exists() && signedApk.length() > 0)

        // Verify signed APK contents
        val zip = ZipFile(signedApk)
        val entryNames = zip.entries().asSequence().map { it.name }.toList()
        zip.close()

        assertTrue("Signed APK must contain AndroidManifest.xml", entryNames.contains("AndroidManifest.xml"))
        assertTrue("Signed APK must contain classes.dex", entryNames.contains("classes.dex"))
        assertTrue("Signed APK must contain META-INF/MANIFEST.MF", entryNames.contains("META-INF/MANIFEST.MF"))
    }

    @Test
    fun testOnDevicePatcherEndToEndPipeline() {
        // Create dummy base.apk and config split inside a mock .apkm bundle
        val dummyBase = tempFolder.newFile("mock_base.apk")
        createDummyApk(dummyBase, mapOf(
            "classes.dex" to "validateTmSdkSupport validateIsUhdSupportedDevice dummy_bytecode",
            "AndroidManifest.xml" to "<manifest/>"
        ))

        val dummySplit = tempFolder.newFile("config.arm64_v8a.apk")
        createDummyApk(dummySplit, mapOf("lib/arm64-v8a/libclearvr.so" to "binary_data"))

        val mockApkm = tempFolder.newFile("mock_f1tv.apkm")
        ZipOutputStream(FileOutputStream(mockApkm)).use { zipOut ->
            // Put base.apk
            zipOut.putNextEntry(ZipEntry("base.apk"))
            dummyBase.inputStream().use { it.copyTo(zipOut) }
            zipOut.closeEntry()

            // Put config.arm64_v8a.apk
            zipOut.putNextEntry(ZipEntry("config.arm64_v8a.apk"))
            dummySplit.inputStream().use { it.copyTo(zipOut) }
            zipOut.closeEntry()
        }

        val outputDir = tempFolder.newFolder("output_patch")
        val progressLogs = mutableListOf<String>()

        val resultApkm = OnDevicePatcher.patchApkmBundle(mockApkm, outputDir) { progressLogs.add(it) }

        assertTrue("Patcher result must not be null", resultApkm != null)
        assertTrue("Patched APKM must exist", resultApkm!!.exists() && resultApkm.length() > 0)
        assertTrue("Progress logs must contain extraction and completion", progressLogs.isNotEmpty())

        // Verify output bundle contains re-signed splits
        val zip = ZipFile(resultApkm)
        val names = zip.entries().asSequence().map { it.name }.toList()
        zip.close()

        assertTrue("Patched APKM must contain base.apk", names.contains("base.apk"))
        assertTrue("Patched APKM must contain config.arm64_v8a.apk", names.contains("config.arm64_v8a.apk"))
    }
}
