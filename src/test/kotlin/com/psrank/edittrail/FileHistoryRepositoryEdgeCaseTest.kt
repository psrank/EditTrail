package com.psrank.edittrail

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

/**
 * Edge-case tests added after implementation to cover branches not exercised by
 * the acceptance tests in FileHistoryRepositoryTest.
 */
class FileHistoryRepositoryEdgeCaseTest {

    private lateinit var repository: FileHistoryRepository

    @BeforeEach
    fun setUp() {
        repository = FileHistoryRepository()
    }

    // ── recordEdit blank URL ─────────────────────────────────────────────────────

    @Test
    fun `recordEdit ignores blank URL`() {
        repository.recordEdit("", "Main.kt", "src/Main.kt")
        assertEquals(0, repository.getHistory(SortMode.LAST_EDITED).size)
    }

    @Test
    fun `recordEdit ignores whitespace-only URL`() {
        repository.recordEdit("   ", "Main.kt", "src/Main.kt")
        assertEquals(0, repository.getHistory(SortMode.LAST_EDITED).size)
    }

    @Test
    fun `recordView ignores whitespace-only URL`() {
        repository.recordView("   ", "Main.kt", "src/Main.kt")
        assertEquals(0, repository.getHistory(SortMode.LAST_VIEWED).size)
    }

    // ── recordEdit on brand-new file ─────────────────────────────────────────────

    @Test
    fun `recordEdit on new file sets lastEditedAt but leaves lastViewedAt null`() {
        repository.recordEdit("file:///src/New.kt", "New.kt", "src/New.kt")

        val entry = repository.getHistory(SortMode.LAST_EDITED)[0]
        assertNotNull(entry.lastEditedAt)
        assertNull(entry.lastViewedAt)
        assertEquals(1, entry.editCount)
        assertEquals(0, entry.viewCount)
    }

    // ── LAST_VIEWED fallback to lastEditedAt ─────────────────────────────────────

    @Test
    fun `getHistory LAST_VIEWED falls back to lastEditedAt when lastViewedAt is null`() {
        // Edit-only entry (never viewed)
        repository.recordEdit("file:///src/EditOnly.kt", "EditOnly.kt", "src/EditOnly.kt")
        Thread.sleep(2)
        // Viewed entry — should appear first because lastViewedAt is more recent
        repository.recordView("file:///src/Viewed.kt", "Viewed.kt", "src/Viewed.kt")

        val history = repository.getHistory(SortMode.LAST_VIEWED)
        assertEquals("file:///src/Viewed.kt", history[0].fileUrl,
            "Viewed entry should be first (most recent lastViewedAt)")
        assertEquals("file:///src/EditOnly.kt", history[1].fileUrl,
            "Edit-only entry uses lastEditedAt as fallback for sort")
    }

    // ── LAST_EDITED: multiple view-only entries sub-sorted by lastViewedAt ───────

    @Test
    fun `getHistory LAST_EDITED sorts multiple view-only entries by lastViewedAt descending`() {
        repository.recordView("file:///src/ViewA.kt", "ViewA.kt", "src/ViewA.kt")
        Thread.sleep(5)
        repository.recordView("file:///src/ViewB.kt", "ViewB.kt", "src/ViewB.kt")
        Thread.sleep(5)
        repository.recordEdit("file:///src/Edited.kt", "Edited.kt", "src/Edited.kt")

        val history = repository.getHistory(SortMode.LAST_EDITED)
        // Edited entry must come first
        assertEquals("file:///src/Edited.kt", history[0].fileUrl)
        // Among view-only: B was viewed more recently than A
        assertEquals("file:///src/ViewB.kt", history[1].fileUrl)
        assertEquals("file:///src/ViewA.kt", history[2].fileUrl)
    }

    // ── getAllEntries returns a snapshot ─────────────────────────────────────────

    @Test
    fun `getAllEntries returns independent snapshot — mutations do not affect repository`() {
        repository.recordView("file:///src/Snap.kt", "Snap.kt", "src/Snap.kt")

        val snapshot = repository.getAllEntries().toMutableList()
        snapshot.clear()

        // Repository should be unaffected
        assertEquals(1, repository.getAllEntries().size)
    }

    // ── loadEntries deduplicates by fileUrl ──────────────────────────────────────

    @Test
    fun `loadEntries with duplicate fileUrls keeps the last occurrence`() {
        val first = FileHistoryEntry(
            fileUrl = "file:///src/Dup.kt",
            fileName = "Dup.kt",
            relativePath = "src/Dup.kt",
            viewCount = 1,
            editCount = 0
        )
        val second = FileHistoryEntry(
            fileUrl = "file:///src/Dup.kt",
            fileName = "Dup.kt",
            relativePath = "src/Dup.kt",
            viewCount = 5,
            editCount = 3
        )

        repository.loadEntries(listOf(first, second))

        val entries = repository.getAllEntries()
        assertEquals(1, entries.size, "Duplicate URL should be collapsed to one entry")
        assertEquals(5, entries[0].viewCount, "Last entry in list should win")
        assertEquals(3, entries[0].editCount)
    }

    // ── loadEntries replaces existing entries ────────────────────────────────────

    @Test
    fun `loadEntries replaces all existing entries`() {
        repository.recordView("file:///src/Old.kt", "Old.kt", "src/Old.kt")
        assertEquals(1, repository.getAllEntries().size)

        val fresh = FileHistoryEntry(
            fileUrl = "file:///src/New.kt",
            fileName = "New.kt",
            relativePath = "src/New.kt"
        )
        repository.loadEntries(listOf(fresh))

        val entries = repository.getAllEntries()
        assertEquals(1, entries.size)
        assertEquals("file:///src/New.kt", entries[0].fileUrl)
    }

    // ── SortMode enum coverage ───────────────────────────────────────────────────

    @Test
    fun `SortMode enum has exactly two values`() {
        val values = SortMode.entries
        assertEquals(2, values.size)
        assertTrue(values.contains(SortMode.LAST_EDITED))
        assertTrue(values.contains(SortMode.LAST_VIEWED))
    }
}
