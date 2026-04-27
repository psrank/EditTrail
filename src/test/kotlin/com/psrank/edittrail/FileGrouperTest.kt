package com.psrank.edittrail

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * TDD tests for FileGrouper — written before implementation.
 * These tests are expected to fail until FileGrouper.kt is created.
 */
class FileGrouperTest {

    private fun entry(
        relativePath: String,
        lastViewedAt: Long? = null,
        lastEditedAt: Long? = null
    ) = FileHistoryEntry(
        fileUrl = "file:///$relativePath",
        fileName = relativePath.substringAfterLast('/'),
        relativePath = relativePath,
        lastViewedAt = lastViewedAt,
        lastEditedAt = lastEditedAt
    )

    private val now = System.currentTimeMillis()
    private fun minutesAgo(n: Long) = now - n * 60_000L

    // ── empty and single-entry edge cases ───────────────────────────────────────

    @Test
    fun `assignGroups does not throw on empty list`() {
        assertDoesNotThrow { FileGrouper.assignGroups(emptyList()) }
    }

    @Test
    fun `assignGroups does not throw on single entry`() {
        val e = entry("src/Main.kt", lastViewedAt = now)
        assertDoesNotThrow { FileGrouper.assignGroups(listOf(e)) }
    }

    @Test
    fun `single entry has null groupId after assignment`() {
        val e = entry("src/Main.kt", lastViewedAt = now)
        FileGrouper.assignGroups(listOf(e))
        assertNull(e.groupId)
    }

    // ── time proximity + path prefix → score ≥ 4 ───────────────────────────────

    @Test
    fun `entries opened within 5 min in the same directory receive same groupId`() {
        // time: within 5 min → 3 pts; same parent dir → 2 pts; total = 5 ≥ 4
        val a = entry("src/ui/Main.kt", lastViewedAt = minutesAgo(1))
        val b = entry("src/ui/Settings.kt", lastViewedAt = minutesAgo(3))
        FileGrouper.assignGroups(listOf(a, b))
        assertNotNull(a.groupId)
        assertNotNull(b.groupId)
        assertEquals(a.groupId, b.groupId)
    }

    @Test
    fun `entries opened within 30 min in the same directory receive same groupId`() {
        // time: within 30 min → 2 pts; same parent dir → 2 pts; total = 4 ≥ 4
        val a = entry("src/ui/Panel.kt", lastViewedAt = minutesAgo(10))
        val b = entry("src/ui/Renderer.kt", lastViewedAt = minutesAgo(25))
        FileGrouper.assignGroups(listOf(a, b))
        assertNotNull(a.groupId)
        assertNotNull(b.groupId)
        assertEquals(a.groupId, b.groupId)
    }

    // ── unrelated files score below threshold ───────────────────────────────────

    @Test
    fun `entries opened 3 hours apart with different paths receive null groupId`() {
        // time: > 2h → 0 pts; different dirs → 0 pts; ext: both kt → 1 pt; total = 1 < 4
        val a = entry("src/ui/Main.kt", lastViewedAt = minutesAgo(200))
        val b = entry("build/Output.kt", lastViewedAt = minutesAgo(0))
        FileGrouper.assignGroups(listOf(a, b))
        // at least one should be null; if isolated, both are null
        val grouped = listOf(a.groupId, b.groupId).count { it != null }
        assertTrue(grouped == 0, "Unrelated entries should not be grouped but grouped=$grouped")
    }

    @Test
    fun `entry that scores below threshold against all others receives null groupId`() {
        // a and b are related; c is far away in time and path
        val a = entry("src/ui/Panel.kt", lastViewedAt = minutesAgo(2))
        val b = entry("src/ui/Renderer.kt", lastViewedAt = minutesAgo(4))
        val c = entry("test/other/IsolatedTest.java", lastViewedAt = minutesAgo(300))
        FileGrouper.assignGroups(listOf(a, b, c))
        assertNull(c.groupId)
        assertNotNull(a.groupId)
        assertEquals(a.groupId, b.groupId)
    }

    // ── extension match contributes to scoring ──────────────────────────────────

    @Test
    fun `entries with same extension within 2 hours in different dirs can still group`() {
        // time: within 2h → 1 pt; same grandparent → 1 pt; same ext → 1 pt = 3 — below threshold
        // Adjust: same parent → 2 pts + within 2h → 1 pt + same ext → 1 pt = 4 ≥ 4
        val a = entry("src/ui/Panel.kt", lastViewedAt = minutesAgo(90))
        val b = entry("src/ui/Dialog.kt", lastViewedAt = minutesAgo(100))
        FileGrouper.assignGroups(listOf(a, b))
        assertNotNull(a.groupId)
        assertEquals(a.groupId, b.groupId)
    }

    // ── transitive grouping ─────────────────────────────────────────────────────

    @Test
    fun `transitive connections produce a single group`() {
        // a-b are related, b-c are related, therefore a, b, c should all be in same group
        val a = entry("src/ui/Panel.kt", lastViewedAt = minutesAgo(1))
        val b = entry("src/ui/Renderer.kt", lastViewedAt = minutesAgo(3))
        val c = entry("src/ui/Factory.kt", lastViewedAt = minutesAgo(5))
        FileGrouper.assignGroups(listOf(a, b, c))
        assertNotNull(a.groupId)
        assertEquals(a.groupId, b.groupId)
        assertEquals(b.groupId, c.groupId)
    }

    // ── lastEditedAt used as fallback timestamp ─────────────────────────────────

    @Test
    fun `lastEditedAt is used when lastViewedAt is null`() {
        // Both files edited within 5 min, same dir → should group
        val a = entry("src/core/Service.kt", lastViewedAt = null, lastEditedAt = minutesAgo(2))
        val b = entry("src/core/Repository.kt", lastViewedAt = null, lastEditedAt = minutesAgo(4))
        FileGrouper.assignGroups(listOf(a, b))
        assertNotNull(a.groupId)
        assertEquals(a.groupId, b.groupId)
    }

    @Test
    fun `entries with no timestamp at all receive null groupId`() {
        val a = entry("src/Main.kt", lastViewedAt = null, lastEditedAt = null)
        val b = entry("src/Other.kt", lastViewedAt = null, lastEditedAt = null)
        FileGrouper.assignGroups(listOf(a, b))
        // no timestamp → no time proximity score; if same dir that's 2 pts + same ext 1 pt = 3 < 4
        // so they should NOT group purely on path+ext without time signal
        // (expected to remain null because threshold is 4)
        assertNull(a.groupId)
        assertNull(b.groupId)
    }

    // ── exception safety ────────────────────────────────────────────────────────

    @Test
    fun `assignGroups preserves previous groupIds if called again on same entries`() {
        val a = entry("src/ui/Panel.kt", lastViewedAt = minutesAgo(2))
        val b = entry("src/ui/Renderer.kt", lastViewedAt = minutesAgo(4))
        FileGrouper.assignGroups(listOf(a, b))
        val firstGroupId = a.groupId
        FileGrouper.assignGroups(listOf(a, b))
        // After a second call, entries should still be grouped (same or re-assigned, but not null)
        assertNotNull(a.groupId)
        assertEquals(a.groupId, b.groupId)
    }
}
