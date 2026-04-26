package com.psrank.edittrail

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Automated portion of the persistence verification for [EditTrailProjectService].
 *
 * The IDE-restart scenario (task 8.1) cannot be automated without a running IntelliJ
 * instance — see the manual checklist below. This test class covers what can be
 * verified in isolation (bean structure, state container correctness).
 *
 * ──────────────────────────────────────────────────────────────
 * MANUAL VERIFICATION CHECKLIST (task 8.1 / 8.2)
 * ──────────────────────────────────────────────────────────────
 * 8.1 History survives IDE restart:
 *   1. Install the plugin in a local IDE.
 *   2. Open any file → verify it appears in the EditTrail panel.
 *   3. Close and reopen the IDE; reopen the same project.
 *   4. The file must still appear in the EditTrail panel.
 *
 * 8.2 Independent histories per project:
 *   1. Open Project A, view File-A → it appears in EditTrail.
 *   2. Open Project B in the same IDE instance.
 *   3. The EditTrail panel for Project B must NOT show File-A.
 * ──────────────────────────────────────────────────────────────
 */
class PersistenceVerificationTest {

    @Test
    fun `FileHistoryState is a valid serialisable bean`() {
        val state = FileHistoryState()
        val entry = FileHistoryEntry(
            fileUrl = "file:///src/Main.kt",
            fileName = "Main.kt",
            relativePath = "src/Main.kt",
            lastViewedAt = System.currentTimeMillis(),
            lastEditedAt = null,
            viewCount = 1,
            editCount = 0
        )
        state.entries.add(entry)

        assertNotNull(state.entries)
        assertEquals(1, state.entries.size)
        assertEquals("file:///src/Main.kt", state.entries[0].fileUrl)
        assertEquals("Main.kt", state.entries[0].fileName)
        assertEquals(1, state.entries[0].viewCount)
        assertNull(state.entries[0].lastEditedAt)
    }

    @Test
    fun `FileHistoryState entries list is mutable (required for XmlSerializer)`() {
        val state = FileHistoryState()
        // Must be able to add without UnsupportedOperationException
        assertDoesNotThrow {
            state.entries.add(FileHistoryEntry(fileUrl = "file:///a.kt", fileName = "a.kt", relativePath = "a.kt"))
            state.entries.add(FileHistoryEntry(fileUrl = "file:///b.kt", fileName = "b.kt", relativePath = "b.kt"))
        }
        assertEquals(2, state.entries.size)
    }

    @Test
    fun `FileHistoryEntry no-arg constructor creates default instance (required for XmlSerializer)`() {
        val entry = FileHistoryEntry() // no-arg constructor via default parameter values
        assertEquals("", entry.fileUrl)
        assertEquals("", entry.fileName)
        assertEquals(0, entry.viewCount)
        assertEquals(0, entry.editCount)
        assertTrue(entry.exists)
        assertNull(entry.lastViewedAt)
        assertNull(entry.lastEditedAt)
    }

    @Test
    fun `repository round-trips entries through FileHistoryState`() {
        val repo = FileHistoryRepository()
        repo.recordView("file:///src/Main.kt", "Main.kt", "src/Main.kt")
        repo.recordEdit("file:///src/Main.kt", "Main.kt", "src/Main.kt")

        // Simulate getState()
        val state = FileHistoryState()
        state.entries = repo.getAllEntries().toMutableList()

        // Simulate loadState() into a new repository instance
        val repo2 = FileHistoryRepository()
        repo2.loadEntries(state.entries)

        val restored = repo2.getHistory(SortMode.LAST_EDITED)
        assertEquals(1, restored.size)
        assertEquals("file:///src/Main.kt", restored[0].fileUrl)
        assertEquals(1, restored[0].viewCount)
        assertEquals(1, restored[0].editCount)
        assertNotNull(restored[0].lastViewedAt)
        assertNotNull(restored[0].lastEditedAt)
    }
}
