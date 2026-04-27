package com.psrank.edittrail

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

/**
 * Edge-case tests for FileHistoryRepository.clearAll() and setMaxSize() — Stage 6.
 */
class FileHistoryRepositorySettingsEdgeCaseTest {

    private lateinit var repository: FileHistoryRepository

    @BeforeEach
    fun setUp() {
        repository = FileHistoryRepository()
    }

    // ── clearAll idempotency ──────────────────────────────────────────────────────

    @Test
    fun `calling clearAll twice does not throw`() {
        repository.recordView("file:///a.kt", "a.kt", "a.kt")
        assertDoesNotThrow {
            repository.clearAll()
            repository.clearAll()
        }
    }

    @Test
    fun `clearAll does not affect max size — new entries still evict correctly`() {
        val small = FileHistoryRepository(maxSize = 2)
        small.recordView("file:///a.kt", "a.kt", "a.kt")
        small.recordView("file:///b.kt", "b.kt", "b.kt")
        small.clearAll()

        // After clear, adding 3 entries should evict 1
        small.recordView("file:///x.kt", "x.kt", "x.kt")
        small.recordView("file:///y.kt", "y.kt", "y.kt")
        small.recordView("file:///z.kt", "z.kt", "z.kt")
        assertEquals(2, small.getHistory(SortMode.LAST_VIEWED).size)
    }

    // ── setMaxSize edge cases ─────────────────────────────────────────────────────

    @Test
    fun `setMaxSize to 1 retains only the most recently active entry`() {
        Thread.sleep(2)
        repository.recordView("file:///first.kt", "first.kt", "first.kt")
        Thread.sleep(2)
        repository.recordView("file:///second.kt", "second.kt", "second.kt")

        repository.setMaxSize(1)

        val history = repository.getHistory(SortMode.LAST_VIEWED)
        assertEquals(1, history.size)
        assertEquals("file:///second.kt", history[0].fileUrl)
    }

    @Test
    fun `setMaxSize called multiple times accumulates correctly`() {
        repeat(10) { i -> repository.recordView("file:///f$i.kt", "f$i.kt", "f$i.kt") }
        repository.setMaxSize(8)
        assertEquals(8, repository.getHistory(SortMode.LAST_VIEWED).size)
        repository.setMaxSize(4)
        assertEquals(4, repository.getHistory(SortMode.LAST_VIEWED).size)
    }

    @Test
    fun `after setMaxSize new entries beyond new limit are evicted`() {
        repeat(5) { i -> repository.recordView("file:///f$i.kt", "f$i.kt", "f$i.kt") }
        repository.setMaxSize(5)

        // Adding one more should evict the oldest
        repository.recordView("file:///new.kt", "new.kt", "new.kt")
        assertEquals(5, repository.getHistory(SortMode.LAST_VIEWED).size)
    }

    // ── loadEntries then clearAll ─────────────────────────────────────────────────

    @Test
    fun `clearAll works correctly after loadEntries`() {
        val preloaded = listOf(
            FileHistoryEntry("file:///a.kt", "a.kt", "a.kt"),
            FileHistoryEntry("file:///b.kt", "b.kt", "b.kt")
        )
        repository.loadEntries(preloaded)
        assertEquals(2, repository.getHistory(SortMode.LAST_VIEWED).size)

        repository.clearAll()
        assertEquals(0, repository.getHistory(SortMode.LAST_VIEWED).size)
    }
}
