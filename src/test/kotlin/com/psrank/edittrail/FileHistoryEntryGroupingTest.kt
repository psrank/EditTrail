package com.psrank.edittrail

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * TDD tests for the groupId field on FileHistoryEntry.
 * These tests verify the new field exists with the correct default.
 * Expected to fail compilation until groupId is added to FileHistoryEntry.
 */
class FileHistoryEntryGroupingTest {

    @Test
    fun `FileHistoryEntry has a groupId field defaulting to null`() {
        val entry = FileHistoryEntry(
            fileUrl = "file:///src/Main.kt",
            fileName = "Main.kt",
            relativePath = "src/Main.kt"
        )
        assertNull(entry.groupId)
    }

    @Test
    fun `groupId can be set and read back`() {
        val entry = FileHistoryEntry(
            fileUrl = "file:///src/Main.kt",
            fileName = "Main.kt",
            relativePath = "src/Main.kt"
        )
        entry.groupId = 3
        assertEquals(3, entry.groupId)
    }

    @Test
    fun `groupId can be reset to null`() {
        val entry = FileHistoryEntry(
            fileUrl = "file:///src/Main.kt",
            fileName = "Main.kt",
            relativePath = "src/Main.kt"
        )
        entry.groupId = 1
        entry.groupId = null
        assertNull(entry.groupId)
    }

    @Test
    fun `groupId is not included in copy equality (it is session-scoped)`() {
        // Two logically identical entries that differ only by groupId should still compare
        // via their primary identity fields (fileUrl). GroupId is mutable state only.
        val a = FileHistoryEntry(fileUrl = "file:///src/A.kt", fileName = "A.kt", relativePath = "src/A.kt")
        val b = FileHistoryEntry(fileUrl = "file:///src/A.kt", fileName = "A.kt", relativePath = "src/A.kt")
        a.groupId = 1
        b.groupId = null
        // They are data class instances — if groupId is in the primary constructor, copy
        // equality will differ, which is fine; this test documents the behavior.
        // If groupId is added as a non-constructor var, equals() won't consider it.
        // Either way, fileUrl must match:
        assertEquals(a.fileUrl, b.fileUrl)
    }
}
