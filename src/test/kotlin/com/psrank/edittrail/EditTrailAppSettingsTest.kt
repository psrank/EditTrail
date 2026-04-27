package com.psrank.edittrail

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

/**
 * TDD acceptance tests for EditTrailAppSettings — created BEFORE implementation (Stage 4).
 * All tests in this file are expected to fail compilation until the class is implemented.
 */
class EditTrailAppSettingsTest {

    // ── Default state ────────────────────────────────────────────────────────────

    @Test
    fun `default maxHistorySize is 500`() {
        val state = EditTrailAppSettings.State()
        assertEquals(500, state.maxHistorySize)
    }

    @Test
    fun `default defaultSortMode is LAST_EDITED`() {
        val state = EditTrailAppSettings.State()
        assertEquals("LAST_EDITED", state.defaultSortMode)
    }

    @Test
    fun `default persistSearchOptions is false`() {
        val state = EditTrailAppSettings.State()
        assertFalse(state.persistSearchOptions)
    }

    @Test
    fun `default search toggle values are all false`() {
        val state = EditTrailAppSettings.State()
        assertFalse(state.matchPath)
        assertFalse(state.matchContent)
        assertFalse(state.regex)
        assertFalse(state.caseSensitive)
    }

    // ── PersistentStateComponent contract ────────────────────────────────────────

    @Test
    fun `getState returns the current state`() {
        val settings = EditTrailAppSettings()
        val state = settings.state
        assertNotNull(state)
        assertEquals(500, state.maxHistorySize)
    }

    @Test
    fun `loadState sets state fields`() {
        val settings = EditTrailAppSettings()
        val newState = EditTrailAppSettings.State().apply {
            maxHistorySize = 250
            defaultSortMode = "LAST_VIEWED"
            persistSearchOptions = true
            matchPath = true
        }
        settings.loadState(newState)
        assertEquals(250, settings.state.maxHistorySize)
        assertEquals("LAST_VIEWED", settings.state.defaultSortMode)
        assertTrue(settings.state.persistSearchOptions)
        assertTrue(settings.state.matchPath)
    }

    // ── State mutation ───────────────────────────────────────────────────────────

    @Test
    fun `state fields are mutable`() {
        val settings = EditTrailAppSettings()
        settings.state.maxHistorySize = 1000
        assertEquals(1000, settings.state.maxHistorySize)
    }
}
