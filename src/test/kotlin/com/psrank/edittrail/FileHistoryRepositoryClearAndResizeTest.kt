package com.psrank.edittrail

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

/**
 * TDD acceptance tests for the new FileHistoryRepository methods added in iteration 006:
 * - clearAll(): removes all entries
 * - setMaxSize(n): updates the limit and trims entries if needed
 *
 * Created BEFORE implementation (Stage 4) — expected to fail until methods exist.
 */
class FileHistoryRepositoryClearAndResizeTest {

    private lateinit var repository: FileHistoryRepository

    @BeforeEach
    fun setUp() {
        repository = FileHistoryRepository()
    }

    // ── clearAll ─────────────────────────────────────────────────────────────────

    @Test
    fun `clearAll removes all entries`() {
        repository.recordView("file:///a.kt", "a.kt", "a.kt")
        repository.recordView("file:///b.kt", "b.kt", "b.kt")
        repository.recordView("file:///c.kt", "c.kt", "c.kt")

        repository.clearAll()

        assertEquals(0, repository.getHistory(SortMode.LAST_VIEWED).size)
    }

    @Test
    fun `clearAll on empty repository does not throw`() {
        assertDoesNotThrow { repository.clearAll() }
        assertEquals(0, repository.getHistory(SortMode.LAST_VIEWED).size)
    }

    @Test
    fun `after clearAll new entries can be recorded normally`() {
        repository.recordView("file:///old.kt", "old.kt", "old.kt")
        repository.clearAll()

        repository.recordView("file:///new.kt", "new.kt", "new.kt")
        val history = repository.getHistory(SortMode.LAST_VIEWED)
        assertEquals(1, history.size)
        assertEquals("file:///new.kt", history[0].fileUrl)
    }

    // ── setMaxSize ───────────────────────────────────────────────────────────────

    @Test
    fun `setMaxSize to higher value retains all existing entries`() {
        repeat(10) { i -> repository.recordView("file:///f$i.kt", "f$i.kt", "f$i.kt") }
        repository.setMaxSize(20)
        assertEquals(10, repository.getHistory(SortMode.LAST_VIEWED).size)
    }

    @Test
    fun `setMaxSize to lower value trims oldest entries immediately`() {
        // Add 10 entries with distinct timestamps
        repeat(10) { i ->
            Thread.sleep(2)
            repository.recordView("file:///f$i.kt", "f$i.kt", "f$i.kt")
        }
        assertEquals(10, repository.getHistory(SortMode.LAST_VIEWED).size)

        repository.setMaxSize(5)

        assertEquals(5, repository.getHistory(SortMode.LAST_VIEWED).size)
    }

    @Test
    fun `setMaxSize to equal current size keeps all entries`() {
        repeat(5) { i -> repository.recordView("file:///f$i.kt", "f$i.kt", "f$i.kt") }
        repository.setMaxSize(5)
        assertEquals(5, repository.getHistory(SortMode.LAST_VIEWED).size)
    }

    @Test
    fun `new repository respects maxSize passed to constructor`() {
        val small = FileHistoryRepository(maxSize = 3)
        repeat(5) { i -> small.recordView("file:///f$i.kt", "f$i.kt", "f$i.kt") }
        assertEquals(3, small.getHistory(SortMode.LAST_VIEWED).size)
    }
}
