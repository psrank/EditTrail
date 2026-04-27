package com.psrank.edittrail

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * TDD tests for the global-search toggle persistence in [FileHistoryState].
 * Expected to fail compilation until FileHistoryState.globalSearchEnabled is added.
 *
 * Covers: specs/global-search — toggle disabled by default, toggle state remembered
 */
class GlobalSearchStateTest {

    private lateinit var state: FileHistoryState

    @BeforeEach
    fun setUp() {
        state = FileHistoryState()
    }

    @Test
    fun `globalSearchEnabled is false by default`() {
        assertFalse(state.globalSearchEnabled)
    }

    @Test
    fun `globalSearchEnabled can be set to true`() {
        state.globalSearchEnabled = true
        assertTrue(state.globalSearchEnabled)
    }

    @Test
    fun `globalSearchEnabled can be toggled back to false`() {
        state.globalSearchEnabled = true
        state.globalSearchEnabled = false
        assertFalse(state.globalSearchEnabled)
    }

    @Test
    fun `globalSearchEnabled is independent of entries list`() {
        state.globalSearchEnabled = true
        state.entries.add(
            FileHistoryEntry(
                fileUrl = "file:///src/Main.kt",
                fileName = "Main.kt",
                relativePath = "src/Main.kt",
                lastViewedAt = System.currentTimeMillis(),
                lastEditedAt = null,
                viewCount = 1,
                editCount = 0,
                exists = true
            )
        )
        assertTrue(state.globalSearchEnabled)
        assertEquals(1, state.entries.size)
    }
}
