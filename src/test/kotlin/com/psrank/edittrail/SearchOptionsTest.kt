package com.psrank.edittrail

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SearchOptionsTest {

    @Test
    fun `default SearchOptions has empty query`() {
        assertEquals("", SearchOptions().query)
    }

    @Test
    fun `default matchPath is false`() {
        assertFalse(SearchOptions().matchPath)
    }

    @Test
    fun `default matchContent is false`() {
        assertFalse(SearchOptions().matchContent)
    }

    @Test
    fun `default regex is false`() {
        assertFalse(SearchOptions().regex)
    }

    @Test
    fun `default caseSensitive is false`() {
        assertFalse(SearchOptions().caseSensitive)
    }

    @Test
    fun `copy preserves changed fields`() {
        val base = SearchOptions(query = "foo", regex = true)
        val updated = base.copy(query = "bar")
        assertEquals("bar", updated.query)
        assertTrue(updated.regex)
    }
}
