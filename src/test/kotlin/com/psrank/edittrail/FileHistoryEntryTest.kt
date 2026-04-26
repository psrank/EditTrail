package com.psrank.edittrail

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

/**
 * Tests for FileHistoryEntry data model.
 * These are expected to fail compilation until FileHistoryEntry is implemented.
 */
class FileHistoryEntryTest {

    @Test
    fun `entry has all required fields`() {
        val entry = FileHistoryEntry(
            fileUrl = "file:///project/src/Main.kt",
            fileName = "Main.kt",
            relativePath = "src/Main.kt",
            lastViewedAt = null,
            lastEditedAt = null,
            viewCount = 0,
            editCount = 0,
            exists = true
        )

        assertEquals("file:///project/src/Main.kt", entry.fileUrl)
        assertEquals("Main.kt", entry.fileName)
        assertEquals("src/Main.kt", entry.relativePath)
        assertNull(entry.lastViewedAt)
        assertNull(entry.lastEditedAt)
        assertEquals(0, entry.viewCount)
        assertEquals(0, entry.editCount)
        assertTrue(entry.exists)
    }

    @Test
    fun `entry exists defaults to true`() {
        val entry = FileHistoryEntry(
            fileUrl = "file:///project/src/Main.kt",
            fileName = "Main.kt",
            relativePath = "src/Main.kt",
            lastViewedAt = null,
            lastEditedAt = null,
            viewCount = 0,
            editCount = 0
        )
        assertTrue(entry.exists)
    }
}
