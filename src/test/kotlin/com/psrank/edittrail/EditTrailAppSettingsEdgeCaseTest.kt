package com.psrank.edittrail

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Edge-case and boundary tests for EditTrailAppSettings — created after implementation (Stage 6).
 */
class EditTrailAppSettingsEdgeCaseTest {

    // ── State independence ────────────────────────────────────────────────────────

    @Test
    fun `two separate State instances are independent`() {
        val s1 = EditTrailAppSettings.State().apply { maxHistorySize = 100 }
        val s2 = EditTrailAppSettings.State()
        assertEquals(100, s1.maxHistorySize)
        assertEquals(500, s2.maxHistorySize)
    }

    @Test
    fun `two separate EditTrailAppSettings instances start with independent state`() {
        val a = EditTrailAppSettings()
        val b = EditTrailAppSettings()
        a.state.maxHistorySize = 999
        assertEquals(999, a.state.maxHistorySize)
        // b has its own state — default value unchanged
        assertEquals(500, b.state.maxHistorySize)
    }

    // ── loadState replaces state ──────────────────────────────────────────────────

    @Test
    fun `loadState replaces previous state entirely`() {
        val settings = EditTrailAppSettings()
        settings.state.maxHistorySize = 999
        settings.state.matchPath = true

        val fresh = EditTrailAppSettings.State()   // default: 500, matchPath = false
        settings.loadState(fresh)

        assertEquals(500, settings.state.maxHistorySize)
        assertFalse(settings.state.matchPath)
    }

    // ── All search toggle fields ──────────────────────────────────────────────────

    @Test
    fun `all search toggle fields can be independently set`() {
        val state = EditTrailAppSettings.State().apply {
            matchPath = true
            matchContent = false
            regex = true
            caseSensitive = false
        }
        assertTrue(state.matchPath)
        assertFalse(state.matchContent)
        assertTrue(state.regex)
        assertFalse(state.caseSensitive)
    }

    // ── defaultSortMode string values ─────────────────────────────────────────────

    @Test
    fun `defaultSortMode can be set to LAST_VIEWED`() {
        val state = EditTrailAppSettings.State().apply { defaultSortMode = "LAST_VIEWED" }
        assertEquals("LAST_VIEWED", state.defaultSortMode)
    }

    @Test
    fun `defaultSortMode can be set to LAST_EDITED`() {
        val state = EditTrailAppSettings.State().apply { defaultSortMode = "LAST_EDITED" }
        assertEquals("LAST_EDITED", state.defaultSortMode)
    }

    // ── maxHistorySize boundary values ────────────────────────────────────────────

    @Test
    fun `maxHistorySize can be set to minimum boundary value 50`() {
        val state = EditTrailAppSettings.State().apply { maxHistorySize = 50 }
        assertEquals(50, state.maxHistorySize)
    }

    @Test
    fun `maxHistorySize can be set to maximum boundary value 10000`() {
        val state = EditTrailAppSettings.State().apply { maxHistorySize = 10000 }
        assertEquals(10000, state.maxHistorySize)
    }
}
