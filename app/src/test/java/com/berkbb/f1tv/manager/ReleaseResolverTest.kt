package com.berkbb.f1tv.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseResolverTest {

    private val sha = "a".repeat(64)

    private fun release(tag: String, prerelease: Boolean = false, draft: Boolean = false, assetName: String = "f1tv-uhd-patched.apkm") =
        RawRelease(
            tagName = tag,
            isDraft = draft,
            isPrerelease = prerelease,
            assets = listOf(ReleaseAsset(assetName, "https://example.invalid/$tag/$assetName", 48_000_000L, "sha256:$sha"))
        )

    // ==========================================
    // 1. Version parsing & comparison
    // ==========================================

    @Test
    fun testParseVersionFromTagAndInstalledVersionName() {
        assertEquals(listOf(3, 0, 49, 4), ReleaseResolver.parseVersion("v3.0.49.4"))
        assertEquals(listOf(3, 0, 49, 4), ReleaseResolver.parseVersion("3.0.49.4-SP166.4.1-release-R54.2-tv"))
        assertEquals(listOf(3, 0, 48, 2), ReleaseResolver.parseVersion("v3.0.48.2-shield-4k-hdr-unlock"))
        assertEquals(emptyList<Int>(), ReleaseResolver.parseVersion("N/A"))
        assertEquals(emptyList<Int>(), ReleaseResolver.parseVersion(null))
    }

    @Test
    fun testNumericComparisonIsNotSubstringBased() {
        // "3.0.10".contains("3.0.1") was true with the previous string logic
        assertTrue(ReleaseResolver.compareVersions(listOf(3, 0, 10), listOf(3, 0, 1)) > 0)
        assertTrue(ReleaseResolver.compareVersions(listOf(3, 0, 49, 3), listOf(3, 0, 49, 4)) < 0)
        assertEquals(0, ReleaseResolver.compareVersions(listOf(3, 0), listOf(3, 0, 0)))
    }

    @Test
    fun testEvaluateUpdateStatus() {
        val installed = "3.0.49.3-SP165.5.0-release-R54.1-tv"
        assertEquals(UpdateStatus.NOT_INSTALLED, ReleaseResolver.evaluate(null, false, "v3.0.49.4"))
        assertEquals(UpdateStatus.UPDATE_AVAILABLE, ReleaseResolver.evaluate(installed, true, "v3.0.49.4"))
        assertEquals(UpdateStatus.UP_TO_DATE, ReleaseResolver.evaluate(installed, true, "v3.0.49.3"))
        assertEquals(UpdateStatus.UP_TO_DATE, ReleaseResolver.evaluate("3.0.50.0", true, "v3.0.49.4"))
        assertEquals(UpdateStatus.UP_TO_DATE, ReleaseResolver.evaluate("3.0.10", true, "v3.0.1"))
        // Unknown latest release: never claim an update
        assertEquals(UpdateStatus.UP_TO_DATE, ReleaseResolver.evaluate(installed, true, null))
    }

    // ==========================================
    // 2. Release selection
    // ==========================================

    @Test
    fun testStableTagFilter() {
        assertTrue(ReleaseResolver.isStableTag("v3.0.49.4"))
        assertFalse(ReleaseResolver.isStableTag("v3.0.48.2-shield-4k-hdr-unlock"))
        assertFalse(ReleaseResolver.isStableTag("v3.0.48.2-dev"))
    }

    @Test
    fun testSelectLatestStableSkipsVariantsDraftsAndMissingAssets() {
        val releases = listOf(
            release("v3.0.50.0-dev", prerelease = true),
            release("v3.0.49.9-shield-4k-hdr-unlock"),
            release("v3.0.49.8", draft = true),
            release("v3.0.49.7", assetName = "notes.txt"),
            release("v3.0.49.3"),
            release("v3.0.49.4")
        )
        val selected = ReleaseResolver.selectLatestStable(releases)
        assertEquals("v3.0.49.4", selected?.tag)
        assertEquals(sha, selected?.sha256)
        assertNull(ReleaseResolver.selectLatestStable(emptyList()))
    }

    @Test
    fun testParseSha256Digest() {
        assertEquals(sha, ReleaseResolver.parseSha256("sha256:${sha.uppercase()}"))
        assertNull(ReleaseResolver.parseSha256(null))
        assertNull(ReleaseResolver.parseSha256("md5:abc"))
        assertNull(ReleaseResolver.parseSha256("sha256:short"))
    }

    @Test
    fun testCacheFileNameIsTagBoundAndSafe() {
        assertEquals("f1tv_v3.0.49.4.apkm", ReleaseResolver.cacheFileName("v3.0.49.4"))
        assertNotEquals(ReleaseResolver.cacheFileName("v3.0.49.3"), ReleaseResolver.cacheFileName("v3.0.49.4"))
        assertFalse(ReleaseResolver.cacheFileName("../../evil").contains("/"))
    }
}
