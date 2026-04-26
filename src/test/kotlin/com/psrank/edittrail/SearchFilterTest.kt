package com.psrank.edittrail

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SearchFilterTest {

    private fun entry(
        fileName: String = "Main.kt",
        relativePath: String = "src/Main.kt"
    ) = FileHistoryEntry(
        fileUrl = "file:///project/$relativePath",
        fileName = fileName,
        relativePath = relativePath,
        lastViewedAt = null,
        lastEditedAt = null,
        exists = true
    )

    // ── Blank query ──────────────────────────────────────────────────────────────

    @Test
    fun `blank query matches every entry`() {
        val opts = SearchOptions(query = "")
        assertTrue(SearchFilter.matches(entry(), opts))
    }

    // ── Default name matching ────────────────────────────────────────────────────

    @Test
    fun `single token matches file name case-insensitively`() {
        val opts = SearchOptions(query = "main")
        assertTrue(SearchFilter.matches(entry(fileName = "Main.kt"), opts))
    }

    @Test
    fun `query that does not appear in file name is rejected`() {
        val opts = SearchOptions(query = "test")
        assertFalse(SearchFilter.matches(entry(fileName = "Main.kt"), opts))
    }

    @Test
    fun `all tokens must match for multi-token query`() {
        val opts = SearchOptions(query = "main kt")
        assertTrue(SearchFilter.matches(entry(fileName = "Main.kt"), opts))
    }

    @Test
    fun `partial token match fails when one token is absent`() {
        val opts = SearchOptions(query = "main foo")
        assertFalse(SearchFilter.matches(entry(fileName = "Main.kt"), opts))
    }

    // ── Case-sensitive flag ───────────────────────────────────────────────────────

    @Test
    fun `case-insensitive by default`() {
        val opts = SearchOptions(query = "MAIN")
        assertTrue(SearchFilter.matches(entry(fileName = "Main.kt"), opts))
    }

    @Test
    fun `case-sensitive flag requires exact case`() {
        val opts = SearchOptions(query = "MAIN", caseSensitive = true)
        assertFalse(SearchFilter.matches(entry(fileName = "Main.kt"), opts))
    }

    @Test
    fun `case-sensitive flag matches when case is exact`() {
        val opts = SearchOptions(query = "Main", caseSensitive = true)
        assertTrue(SearchFilter.matches(entry(fileName = "Main.kt"), opts))
    }

    // ── Path matching ────────────────────────────────────────────────────────────

    @Test
    fun `matchPath=false does not search relativePath`() {
        val opts = SearchOptions(query = "utils", matchPath = false)
        assertFalse(SearchFilter.matches(entry(fileName = "Main.kt", relativePath = "utils/Main.kt"), opts))
    }

    @Test
    fun `matchPath=true finds query in relativePath`() {
        val opts = SearchOptions(query = "utils", matchPath = true)
        assertTrue(SearchFilter.matches(entry(fileName = "Main.kt", relativePath = "utils/Main.kt"), opts))
    }

    @Test
    fun `matchPath=true also falls through to fileName match`() {
        val opts = SearchOptions(query = "main", matchPath = true)
        assertTrue(SearchFilter.matches(entry(fileName = "Main.kt", relativePath = "src/Main.kt"), opts))
    }

    // ── Regex mode ───────────────────────────────────────────────────────────────

    @Test
    fun `regex mode matches file name`() {
        val opts = SearchOptions(query = "^Main\\.kt$", regex = true)
        assertTrue(SearchFilter.matches(entry(fileName = "Main.kt"), opts))
    }

    @Test
    fun `regex mode rejects non-matching file name`() {
        val opts = SearchOptions(query = "^Foo", regex = true)
        assertFalse(SearchFilter.matches(entry(fileName = "Main.kt"), opts))
    }

    @Test
    fun `invalid regex returns false without throwing`() {
        val opts = SearchOptions(query = "[invalid(", regex = true)
        assertFalse(SearchFilter.matches(entry(), opts))
    }

    @Test
    fun `regex mode with matchPath searches relativePath`() {
        val opts = SearchOptions(query = "src/.*\\.kt", regex = true, matchPath = true)
        assertTrue(SearchFilter.matches(entry(relativePath = "src/Main.kt"), opts))
    }

    @Test
    fun `regex is case-insensitive by default`() {
        val opts = SearchOptions(query = "main\\.kt", regex = true)
        assertTrue(SearchFilter.matches(entry(fileName = "Main.kt"), opts))
    }

    @Test
    fun `regex honours case-sensitive flag`() {
        val opts = SearchOptions(query = "main\\.kt", regex = true, caseSensitive = true)
        assertFalse(SearchFilter.matches(entry(fileName = "Main.kt"), opts))
    }

    // ── isValidRegex ─────────────────────────────────────────────────────────────

    @Test
    fun `isValidRegex returns true for valid pattern`() {
        assertTrue(SearchFilter.isValidRegex("\\d+"))
    }

    @Test
    fun `isValidRegex returns false for invalid pattern`() {
        assertFalse(SearchFilter.isValidRegex("[unclosed"))
    }

    @Test
    fun `isValidRegex returns true for empty string`() {
        assertTrue(SearchFilter.isValidRegex(""))
    }
}
