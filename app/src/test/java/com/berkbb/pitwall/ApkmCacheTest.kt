package com.berkbb.pitwall

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ApkmCacheTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val payload = "dummy apkm payload".toByteArray()

    private fun releaseFor(sha256: String?, size: Long = payload.size.toLong()) =
        ReleaseInfo(tag = "v3.0.49.4", downloadUrl = "https://example.invalid/a.apkm", size = size, sha256 = sha256)

    @Test
    fun testCachedFileIsValidOnlyWhenDigestMatches() {
        val file = tempFolder.newFile("bundle.apkm").apply { writeBytes(payload) }
        val realSha = ApkmCache.sha256Of(file)

        assertTrue(ApkmCache.isValid(file, releaseFor(realSha)))
        assertFalse("Corrupted/stale file must be rejected", ApkmCache.isValid(file, releaseFor("0".repeat(64))))
    }

    @Test
    fun testSizeFallbackWhenNoDigestPublished() {
        val file = tempFolder.newFile("bundle.apkm").apply { writeBytes(payload) }

        assertTrue(ApkmCache.isValid(file, releaseFor(null)))
        assertFalse("Truncated download must be rejected", ApkmCache.isValid(file, releaseFor(null, size = payload.size + 1L)))
        assertFalse("Unknown size and digest must never be trusted", ApkmCache.isValid(file, releaseFor(null, size = 0L)))
    }

    @Test
    fun testMissingOrEmptyFileIsInvalid() {
        val release = releaseFor(null)
        assertFalse(ApkmCache.isValid(tempFolder.root.resolve("missing.apkm"), release))
        assertFalse(ApkmCache.isValid(tempFolder.newFile("empty.apkm"), release))
    }

    @Test
    fun testRequiredFreeBytesCoversDownloadInstallAndMargin() {
        val size = 48L * 1024 * 1024
        val margin = ApkmCache.INSTALL_SAFETY_MARGIN_BYTES

        // Not cached: download + staged install copy + margin
        assertEquals(2 * size + margin, ApkmCache.requiredFreeBytes(size, alreadyCached = false))
        // Cached: only the staged install copy + margin
        assertEquals(size + margin, ApkmCache.requiredFreeBytes(size, alreadyCached = true))
        // Unknown size never goes negative
        assertEquals(margin, ApkmCache.requiredFreeBytes(-1L, alreadyCached = false))
    }

    @Test
    fun testMegabytesRoundUp() {
        assertEquals(0, ApkmCache.toMegabytesCeil(0L))
        assertEquals(1, ApkmCache.toMegabytesCeil(1L))
        assertEquals(1, ApkmCache.toMegabytesCeil(1024L * 1024))
        assertEquals(2, ApkmCache.toMegabytesCeil(1024L * 1024 + 1))
    }

    @Test
    fun testPruneRemovesOldBundlesAndPartialsOnly() {
        val cacheDir = tempFolder.newFolder("cache")
        val release = releaseFor(null)
        val keep = ApkmCache.fileFor(cacheDir, release).apply { writeBytes(payload) }
        val old = cacheDir.resolve("f1tv_v3.0.49.3.apkm").apply { writeBytes(payload) }
        val legacy = cacheDir.resolve("f1tv_update.apkm").apply { writeBytes(payload) }
        val partial = ApkmCache.partialFileFor(cacheDir.resolve("f1tv_v3.0.49.2.apkm")).apply { writeBytes(payload) }
        val unrelated = cacheDir.resolve("other.txt").apply { writeText("keep") }

        ApkmCache.pruneExcept(cacheDir, keep)

        assertTrue(keep.exists())
        assertTrue(unrelated.exists())
        assertFalse(old.exists())
        assertFalse(legacy.exists())
        assertFalse(partial.exists())
        assertEquals("f1tv_v3.0.49.4.apkm.part", ApkmCache.partialFileFor(keep).name)
    }
}
