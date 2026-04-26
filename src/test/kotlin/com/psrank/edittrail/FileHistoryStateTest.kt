package com.psrank.edittrail

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

/**
 * Tests for the FileHistoryState persistence container.
 * Expected to fail compilation until FileHistoryState is implemented.
 */
class FileHistoryStateTest {

    @Test
    fun `FileHistoryState starts with empty entry list`() {
        val state = FileHistoryState()
        assertTrue(state.entries.isEmpty())
    }

    @Test
    fun `FileHistoryState can hold a list of entries`() {
        val entry = FileHistoryEntry(
            fileUrl = "file:///src/Main.kt",
            fileName = "Main.kt",
            relativePath = "src/Main.kt",
            lastViewedAt = System.currentTimeMillis(),
            lastEditedAt = null,
            viewCount = 1,
            editCount = 0,
            exists = true
        )
        val state = FileHistoryState()
        state.entries.add(entry)

        assertEquals(1, state.entries.size)
        assertEquals("file:///src/Main.kt", state.entries[0].fileUrl)
    }
}
