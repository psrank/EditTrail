package com.psrank.edittrail

/**
 * Holds all filter parameters for the EditTrail search bar.
 *
 * @param query        The raw text the user typed. Empty string means no filter.
 * @param matchPath    When true, matching also checks [FileHistoryEntry.relativePath].
 * @param matchContent When true, matching also checks file contents (off-EDT).
 * @param regex        When true, [query] is interpreted as a regular expression.
 * @param caseSensitive When true, string comparisons are case-sensitive.
 */
data class SearchOptions(
    val query: String = "",
    val matchPath: Boolean = false,
    val matchContent: Boolean = false,
    val regex: Boolean = false,
    val caseSensitive: Boolean = false
)
