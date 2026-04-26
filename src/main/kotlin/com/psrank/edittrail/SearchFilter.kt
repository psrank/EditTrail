package com.psrank.edittrail

import java.util.regex.PatternSyntaxException

/**
 * Pure filter logic for the EditTrail search bar.
 *
 * All methods are stateless and thread-safe.
 */
object SearchFilter {

    /**
     * Returns `true` if [entry] matches [options].
     *
     * - When [SearchOptions.query] is blank, every entry matches.
     * - When [SearchOptions.regex] is true, the query is compiled as a regex.
     *   A [PatternSyntaxException] causes the entry to be treated as non-matching.
     * - Otherwise, fuzzy token matching is applied: every space-separated token
     *   in the query must appear in the target string(s).
     *
     * [SearchOptions.matchContent] is intentionally **not** handled here —
     * content matching requires I/O and is managed asynchronously by the caller.
     */
    fun matches(entry: FileHistoryEntry, options: SearchOptions): Boolean {
        val query = options.query
        if (query.isBlank()) return true

        return if (options.regex) {
            matchesRegex(entry, query, options)
        } else {
            matchesTokens(entry, query, options)
        }
    }

    /**
     * Returns `true` if [pattern] is a syntactically valid regular expression.
     */
    fun isValidRegex(pattern: String): Boolean {
        if (pattern.isBlank()) return true
        return try {
            pattern.toRegex()
            true
        } catch (_: PatternSyntaxException) {
            false
        }
    }

    // ── Internal helpers ─────────────────────────────────────────────────────────

    private fun matchesRegex(entry: FileHistoryEntry, pattern: String, options: SearchOptions): Boolean {
        val flags = if (options.caseSensitive) emptySet() else setOf(RegexOption.IGNORE_CASE)
        val regex = try {
            Regex(pattern, flags)
        } catch (_: PatternSyntaxException) {
            return false
        }
        if (regex.containsMatchIn(entry.fileName)) return true
        if (options.matchPath && regex.containsMatchIn(entry.relativePath)) return true
        return false
    }

    private fun matchesTokens(entry: FileHistoryEntry, query: String, options: SearchOptions): Boolean {
        val tokens = query.trim().split(Regex("\\s+"))
        return tokens.all { token -> tokenMatchesEntry(token, entry, options) }
    }

    private fun tokenMatchesEntry(token: String, entry: FileHistoryEntry, options: SearchOptions): Boolean {
        val t = if (options.caseSensitive) token else token.lowercase()
        val name = if (options.caseSensitive) entry.fileName else entry.fileName.lowercase()
        if (name.contains(t)) return true
        if (options.matchPath) {
            val path = if (options.caseSensitive) entry.relativePath else entry.relativePath.lowercase()
            if (path.contains(t)) return true
        }
        return false
    }
}
