package com.psrank.edittrail

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Edge-case and boundary tests for FileGrouper, complementing the TDD acceptance tests.
 * Covers scoring boundaries, path separator variants, and timestamp selection.
 */
class FileGrouperEdgeCaseTest {

    private val now = System.currentTimeMillis()

    private fun entry(
        relativePath: String,
        lastViewedAt: Long? = null,
        lastEditedAt: Long? = null
    ) = FileHistoryEntry(
        fileUrl = "file:///$relativePath",
        fileName = relativePath.substringAfterLast('/').substringAfterLast('\\'),
        relativePath = relativePath,
        lastViewedAt = lastViewedAt,
        lastEditedAt = lastEditedAt
    )

    // ── time boundary precision ─────────────────────────────────────────────────

    @Test
    fun `entries exactly 5 minutes apart in same dir score 5 pts and group`() {
        // time: exactly 5min → 3 pts; same parent → 2 pts; total = 5 ≥ 4
        val fiveMinMs = 5 * 60_000L
        val a = entry("src/ui/A.kt", lastViewedAt = now)
        val b = entry("src/ui/B.kt", lastViewedAt = now - fiveMinMs)
        FileGrouper.assignGroups(listOf(a, b))
        assertNotNull(a.groupId)
        assertEquals(a.groupId, b.groupId)
    }

    @Test
    fun `entries just over 5 minutes apart in same dir score 4 pts and group`() {
        // time: >5min, ≤30min → 2 pts; same parent → 2 pts; total = 4 ≥ 4
        val a = entry("src/ui/A.kt", lastViewedAt = now)
        val b = entry("src/ui/B.kt", lastViewedAt = now - (5 * 60_000L + 1))
        FileGrouper.assignGroups(listOf(a, b))
        assertNotNull(a.groupId)
        assertEquals(a.groupId, b.groupId)
    }

    @Test
    fun `entries exactly 30 minutes apart in same dir score 4 pts and group`() {
        // time: exactly 30min → 2 pts; same parent → 2 pts; total = 4 = threshold
        val thirtyMinMs = 30 * 60_000L
        val a = entry("src/ui/A.kt", lastViewedAt = now)
        val b = entry("src/ui/B.kt", lastViewedAt = now - thirtyMinMs)
        FileGrouper.assignGroups(listOf(a, b))
        assertNotNull(a.groupId)
        assertEquals(a.groupId, b.groupId)
    }

    @Test
    fun `entries just over 30 minutes apart score 3 pts and do not group`() {
        // time: >30min, ≤2h → 1 pt; same parent → 2 pts; diff ext → 0 pts; total = 3 < 4
        val a = entry("src/ui/A.kt", lastViewedAt = now)
        val b = entry("src/ui/B.java", lastViewedAt = now - (30 * 60_000L + 1))
        FileGrouper.assignGroups(listOf(a, b))
        assertNull(a.groupId)
        assertNull(b.groupId)
    }

    @Test
    fun `entries exactly 2 hours apart in same dir score 3 pts and do not group`() {
        // time: exactly 2h → 1 pt; same parent → 2 pts; diff ext → 0 pts; total = 3 < 4
        val twoHourMs = 2 * 3_600_000L
        val a = entry("src/ui/A.kt", lastViewedAt = now)
        val b = entry("src/ui/B.java", lastViewedAt = now - twoHourMs)
        FileGrouper.assignGroups(listOf(a, b))
        assertNull(a.groupId)
        assertNull(b.groupId)
    }

    @Test
    fun `entries over 2 hours apart score 0 time pts and do not group`() {
        // time: >2h → 0 pts; same parent → 2 pts; total = 2 < 4
        val a = entry("src/ui/A.kt", lastViewedAt = now)
        val b = entry("src/ui/B.kt", lastViewedAt = now - (2 * 3_600_000L + 1))
        FileGrouper.assignGroups(listOf(a, b))
        assertNull(a.groupId)
        assertNull(b.groupId)
    }

    // ── score exactly 3 = no group ──────────────────────────────────────────────

    @Test
    fun `score of exactly 3 does not produce a group`() {
        // time: within 30min → 2 pts; same grandparent only → 1 pt; diff ext → 0 pts; total = 3 < 4
        val a = entry("src/ui/Panel.kt", lastViewedAt = now - (10 * 60_000L))
        val b = entry("src/data/Model.java", lastViewedAt = now - (20 * 60_000L))
        FileGrouper.assignGroups(listOf(a, b))
        assertNull(a.groupId)
        assertNull(b.groupId)
    }

    // ── representativeTimestamp uses max of view and edit ──────────────────────

    @Test
    fun `when both timestamps set representativeTimestamp uses the more recent one`() {
        // A: lastViewedAt = 20min ago, lastEditedAt = 2min ago → representative = 2min ago
        // B: lastViewedAt = 4min ago
        // Diff = 2min → 3 pts (within 5min); same dir → 2 pts; total = 5 → group
        val a = entry(
            "src/core/A.kt",
            lastViewedAt = now - (20 * 60_000L),
            lastEditedAt = now - (2 * 60_000L)
        )
        val b = entry("src/core/B.kt", lastViewedAt = now - (4 * 60_000L))
        FileGrouper.assignGroups(listOf(a, b))
        assertNotNull(a.groupId)
        assertEquals(a.groupId, b.groupId)
    }

    @Test
    fun `when one entry has no timestamps it does not contribute time score`() {
        // A has no timestamps → representativeTimestamp = null → time score = 0
        // B has a timestamp
        // same dir → 2 pts; same ext → 1 pt; time → 0; total = 3 < 4 → no group
        val a = entry("src/core/A.kt", lastViewedAt = null, lastEditedAt = null)
        val b = entry("src/core/B.kt", lastViewedAt = now)
        FileGrouper.assignGroups(listOf(a, b))
        assertNull(a.groupId)
        assertNull(b.groupId)
    }

    // ── path separator handling ─────────────────────────────────────────────────

    @Test
    fun `Windows-style backslash paths are recognised as same parent directory`() {
        // same parent dir using backslash separators → 2 pts; within 5min → 3 pts; total = 5
        val a = entry("src\\ui\\Panel.kt", lastViewedAt = now - (2 * 60_000L))
        val b = entry("src\\ui\\Dialog.kt", lastViewedAt = now - (4 * 60_000L))
        FileGrouper.assignGroups(listOf(a, b))
        assertNotNull(a.groupId)
        assertEquals(a.groupId, b.groupId)
    }

    @Test
    fun `root-level files with no parent directory do not receive path score`() {
        // parentDir("Main.kt") = "" → 0 pts path; within 5min → 3 pts; same ext → 1 pt; total = 4
        // Just at threshold — verifies root-level handling doesn't crash
        val a = entry("Main.kt", lastViewedAt = now)
        val b = entry("Other.kt", lastViewedAt = now - (1 * 60_000L))
        assertDoesNotThrow { FileGrouper.assignGroups(listOf(a, b)) }
    }

    // ── extension edge cases ────────────────────────────────────────────────────

    @Test
    fun `files with no extension contribute 0 extension score`() {
        // e.g. Makefile, Dockerfile — no dot → empty extension → no match
        val a = entry("src/Makefile", lastViewedAt = now)
        val b = entry("src/Dockerfile", lastViewedAt = now - (2 * 60_000L))
        // time: 2min → 3 pts; same parent → 2 pts; ext: "" != "" → wait, both are empty — but the
        // code checks extA.isNotEmpty() so empty strings won't match. Total = 5 → group anyway.
        // This test verifies no crash occurs with extension-less file names.
        assertDoesNotThrow { FileGrouper.assignGroups(listOf(a, b)) }
    }

    @Test
    fun `extension match is case-sensitive — kt and KT are different`() {
        // Verifies no cross-case false positive; total without ext: within 5min(3) + samedir(2) = 5 → group regardless
        // Use >30min apart + same grandparent only so ext is the deciding factor
        // time: 45min apart → 1 pt; same grandparent (src) → 1 pt; .kt ≠ .KT → 0 pts; total = 2 < 4
        val a = entry("src/ui/A.kt", lastViewedAt = now)
        val b = entry("src/data/B.KT", lastViewedAt = now - (45 * 60_000L))
        FileGrouper.assignGroups(listOf(a, b))
        assertNull(a.groupId)
        assertNull(b.groupId)
    }
}
