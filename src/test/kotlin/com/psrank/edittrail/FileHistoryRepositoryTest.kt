package com.psrank.edittrail

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

/**
 * Tests for FileHistoryRepository — deduplication, LRU eviction, and sorting.
 * These are expected to fail compilation until FileHistoryRepository and SortMode are implemented.
 */
class FileHistoryRepositoryTest {

    private lateinit var repository: FileHistoryRepository

    @BeforeEach
    fun setUp() {
        repository = FileHistoryRepository()
    }

    // ── recordView ──────────────────────────────────────────────────────────────

    @Test
    fun `recordView adds new entry to history`() {
        repository.recordView(
            fileUrl = "file:///src/Main.kt",
            fileName = "Main.kt",
            relativePath = "src/Main.kt"
        )

        val history = repository.getHistory(SortMode.LAST_VIEWED)
        assertEquals(1, history.size)
        assertEquals("file:///src/Main.kt", history[0].fileUrl)
    }

    @Test
    fun `recordView increments viewCount and updates lastViewedAt`() {
        repository.recordView("file:///src/Main.kt", "Main.kt", "src/Main.kt")
        val before = repository.getHistory(SortMode.LAST_VIEWED)[0].lastViewedAt!!

        Thread.sleep(2)
        repository.recordView("file:///src/Main.kt", "Main.kt", "src/Main.kt")

        val entry = repository.getHistory(SortMode.LAST_VIEWED)[0]
        assertEquals(2, entry.viewCount)
        assertTrue(entry.lastViewedAt!! >= before)
    }

    @Test
    fun `recordView does not create duplicate entries`() {
        repository.recordView("file:///src/Main.kt", "Main.kt", "src/Main.kt")
        repository.recordView("file:///src/Main.kt", "Main.kt", "src/Main.kt")
        repository.recordView("file:///src/Main.kt", "Main.kt", "src/Main.kt")

        assertEquals(1, repository.getHistory(SortMode.LAST_VIEWED).size)
    }

    @Test
    fun `recordView ignores non-file selections (null url)`() {
        // Passing null or blank url should not create an entry
        repository.recordView("", "Main.kt", "src/Main.kt")
        assertEquals(0, repository.getHistory(SortMode.LAST_VIEWED).size)
    }

    // ── recordEdit ──────────────────────────────────────────────────────────────

    @Test
    fun `recordEdit adds entry to history`() {
        repository.recordEdit("file:///src/Main.kt", "Main.kt", "src/Main.kt")

        val history = repository.getHistory(SortMode.LAST_EDITED)
        assertEquals(1, history.size)
        assertEquals("file:///src/Main.kt", history[0].fileUrl)
        assertNotNull(history[0].lastEditedAt)
    }

    @Test
    fun `recordEdit increments editCount and updates lastEditedAt`() {
        repository.recordEdit("file:///src/Main.kt", "Main.kt", "src/Main.kt")
        val before = repository.getHistory(SortMode.LAST_EDITED)[0].lastEditedAt!!

        Thread.sleep(2)
        repository.recordEdit("file:///src/Main.kt", "Main.kt", "src/Main.kt")

        val entry = repository.getHistory(SortMode.LAST_EDITED)[0]
        assertEquals(2, entry.editCount)
        assertTrue(entry.lastEditedAt!! >= before)
    }

    @Test
    fun `recordEdit updates existing viewed-only entry in place`() {
        // File is first viewed
        repository.recordView("file:///src/Main.kt", "Main.kt", "src/Main.kt")
        assertEquals(1, repository.getHistory(SortMode.LAST_VIEWED).size)
        assertNull(repository.getHistory(SortMode.LAST_VIEWED)[0].lastEditedAt)

        // File is then edited — should update existing entry, not create new one
        repository.recordEdit("file:///src/Main.kt", "Main.kt", "src/Main.kt")
        assertEquals(1, repository.getHistory(SortMode.LAST_EDITED).size)
        assertNotNull(repository.getHistory(SortMode.LAST_EDITED)[0].lastEditedAt)
        assertEquals(1, repository.getHistory(SortMode.LAST_EDITED)[0].viewCount)
    }

    // ── sorting ─────────────────────────────────────────────────────────────────

    @Test
    fun `getHistory LAST_EDITED sorts by lastEditedAt descending`() {
        val t1 = System.currentTimeMillis()
        repository.recordEdit("file:///src/A.kt", "A.kt", "src/A.kt")
        Thread.sleep(5)
        repository.recordEdit("file:///src/B.kt", "B.kt", "src/B.kt")

        val history = repository.getHistory(SortMode.LAST_EDITED)
        assertEquals("file:///src/B.kt", history[0].fileUrl)
        assertEquals("file:///src/A.kt", history[1].fileUrl)
    }

    @Test
    fun `getHistory LAST_EDITED puts viewed-only entries after edited entries`() {
        repository.recordView("file:///src/ViewOnly.kt", "ViewOnly.kt", "src/ViewOnly.kt")
        repository.recordEdit("file:///src/Edited.kt", "Edited.kt", "src/Edited.kt")

        val history = repository.getHistory(SortMode.LAST_EDITED)
        // Edited file should come first; viewed-only has null lastEditedAt
        assertEquals("file:///src/Edited.kt", history[0].fileUrl)
        assertEquals("file:///src/ViewOnly.kt", history[1].fileUrl)
    }

    @Test
    fun `getHistory LAST_VIEWED sorts by lastViewedAt descending`() {
        repository.recordView("file:///src/A.kt", "A.kt", "src/A.kt")
        Thread.sleep(5)
        repository.recordView("file:///src/B.kt", "B.kt", "src/B.kt")

        val history = repository.getHistory(SortMode.LAST_VIEWED)
        assertEquals("file:///src/B.kt", history[0].fileUrl)
        assertEquals("file:///src/A.kt", history[1].fileUrl)
    }

    @Test
    fun `getHistory returns empty list when no history exists`() {
        assertTrue(repository.getHistory(SortMode.LAST_EDITED).isEmpty())
    }

    // ── LRU eviction ────────────────────────────────────────────────────────────

    @Test
    fun `repository evicts oldest LRU entry when size exceeds max`() {
        val repository = FileHistoryRepository(maxSize = 3)

        repository.recordView("file:///src/A.kt", "A.kt", "src/A.kt")
        Thread.sleep(2)
        repository.recordView("file:///src/B.kt", "B.kt", "src/B.kt")
        Thread.sleep(2)
        repository.recordView("file:///src/C.kt", "C.kt", "src/C.kt")
        // Now at max (3). Adding D should evict A (oldest LRU).
        Thread.sleep(2)
        repository.recordView("file:///src/D.kt", "D.kt", "src/D.kt")

        val urls = repository.getHistory(SortMode.LAST_VIEWED).map { it.fileUrl }
        assertEquals(3, urls.size)
        assertFalse(urls.contains("file:///src/A.kt"), "Oldest entry should have been evicted")
        assertTrue(urls.contains("file:///src/D.kt"))
    }

    @Test
    fun `repository does not evict below max size`() {
        val repository = FileHistoryRepository(maxSize = 500)

        repeat(10) { i ->
            repository.recordView("file:///src/File$i.kt", "File$i.kt", "src/File$i.kt")
        }

        assertEquals(10, repository.getHistory(SortMode.LAST_VIEWED).size)
    }
}
