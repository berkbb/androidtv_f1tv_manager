package com.berkbb.f1tv.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicArchitectureAndLocaleTest {

    // ==========================================
    // 1. Dynamic Locale & Persistence Tests
    // ==========================================

    @Test
    fun testSavedLanguageTakesPrecedenceOverSystemLocale() {
        assertEquals("tr", LocaleHelper.resolveInitialLanguage(savedLang = "tr", systemLang = "en"))
        assertEquals("ro", LocaleHelper.resolveInitialLanguage(savedLang = "ro", systemLang = "tr"))
        assertEquals("en", LocaleHelper.resolveInitialLanguage(savedLang = "en", systemLang = "ro"))
    }

    @Test
    fun testSystemLanguageFallbackWhenNoSavedPreference() {
        assertEquals("tr", LocaleHelper.resolveInitialLanguage(savedLang = null, systemLang = "tr"))
        assertEquals("ro", LocaleHelper.resolveInitialLanguage(savedLang = null, systemLang = "ro"))
        assertEquals("en", LocaleHelper.resolveInitialLanguage(savedLang = null, systemLang = "en"))
    }

    @Test
    fun testUnsupportedSystemLanguagesFallbackToEnglish() {
        assertEquals("en", LocaleHelper.resolveInitialLanguage(savedLang = null, systemLang = "de"))
        assertEquals("en", LocaleHelper.resolveInitialLanguage(savedLang = null, systemLang = "fr"))
        assertEquals("en", LocaleHelper.resolveInitialLanguage(savedLang = null, systemLang = "es"))
        assertEquals("en", LocaleHelper.resolveInitialLanguage(savedLang = null, systemLang = "it"))
        assertEquals("en", LocaleHelper.resolveInitialLanguage(savedLang = null, systemLang = null))
    }

    @Test
    fun testLanguageCycleTransitions() {
        assertEquals("tr", LocaleHelper.getNextLanguage("en"))
        assertEquals("ro", LocaleHelper.getNextLanguage("tr"))
        assertEquals("en", LocaleHelper.getNextLanguage("ro"))
        assertEquals("en", LocaleHelper.getNextLanguage("unknown"))
    }

    // ==========================================
    // 2. Dynamic ABI & Split Selection Tests
    // ==========================================

    @Test
    fun testResolveBestAbi() {
        // Modern 64-bit Google TV
        val abis64 = arrayOf("arm64-v8a", "armeabi-v7a", "armeabi")
        assertEquals("arm64_v8a", PackageInstallerHelper.resolveBestAbi(abis64))

        // 32-bit legacy TV (e.g. Philips TPM171E MediaTek MT5596)
        val abis32 = arrayOf("armeabi-v7a", "armeabi")
        assertEquals("armeabi_v7a", PackageInstallerHelper.resolveBestAbi(abis32))

        // x86_64 Emulator
        val abisX86 = arrayOf("x86_64", "x86")
        assertEquals("x86_64", PackageInstallerHelper.resolveBestAbi(abisX86))
    }

    @Test
    fun testShouldIncludeSplitForArm64Device() {
        val targetAbi = "arm64_v8a"

        // Must include base, matching ABI, and all density/language splits
        assertTrue(PackageInstallerHelper.shouldIncludeSplit("base.apk", targetAbi))
        assertTrue(PackageInstallerHelper.shouldIncludeSplit("config.arm64_v8a.apk", targetAbi))
        assertTrue(PackageInstallerHelper.shouldIncludeSplit("config.en.apk", targetAbi))
        assertTrue(PackageInstallerHelper.shouldIncludeSplit("config.tr.apk", targetAbi))
        assertTrue(PackageInstallerHelper.shouldIncludeSplit("config.xxhdpi.apk", targetAbi))

        // Must reject conflicting 32-bit ARM or x86 splits
        assertFalse(PackageInstallerHelper.shouldIncludeSplit("config.armeabi_v7a.apk", targetAbi))
        assertFalse(PackageInstallerHelper.shouldIncludeSplit("config.x86.apk", targetAbi))
        assertFalse(PackageInstallerHelper.shouldIncludeSplit("config.x86_64.apk", targetAbi))
        assertFalse(PackageInstallerHelper.shouldIncludeSplit("metadata.json", targetAbi))
    }

    @Test
    fun testShouldIncludeSplitForArmeabiV7aDevice() {
        val targetAbi = "armeabi_v7a"

        // Must include base, matching 32-bit ABI, and config splits
        assertTrue(PackageInstallerHelper.shouldIncludeSplit("base.apk", targetAbi))
        assertTrue(PackageInstallerHelper.shouldIncludeSplit("config.armeabi_v7a.apk", targetAbi))
        assertTrue(PackageInstallerHelper.shouldIncludeSplit("config.en.apk", targetAbi))
        assertTrue(PackageInstallerHelper.shouldIncludeSplit("config.hdpi.apk", targetAbi))

        // Must reject 64-bit ARM split
        assertFalse(PackageInstallerHelper.shouldIncludeSplit("config.arm64_v8a.apk", targetAbi))
    }
}
