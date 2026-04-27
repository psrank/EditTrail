package com.psrank.edittrail

/**
 * Assigns session-scoped group IDs to [FileHistoryEntry] objects based on co-occurrence signals.
 *
 * Scoring (pairwise):
 * - Time proximity  (0–3 pts): representative timestamp within 5 min → 3, 30 min → 2, 2 h → 1, else 0
 * - Path prefix     (0–2 pts): same parent directory → 2, same grandparent → 1
 * - Extension match (0–1 pt):  identical file extension → 1
 *
 * Pairs whose combined score ≥ 4 are considered connected. Group IDs are assigned via
 * union-find (connected components). Isolated entries (component size 1) receive `groupId = null`.
 *
 * Must be called off the EDT; contains no IntelliJ platform dependencies.
 */
object FileGrouper {

    private const val THRESHOLD = 4

    fun assignGroups(entries: List<FileHistoryEntry>) {
        if (entries.size < 2) {
            entries.forEach { it.groupId = null }
            return
        }

        try {
            val parent = IntArray(entries.size) { it }

            fun find(x: Int): Int {
                var r = x
                while (parent[r] != r) r = parent[r]
                var i = x
                while (i != r) { val next = parent[i]; parent[i] = r; i = next }
                return r
            }

            fun union(a: Int, b: Int) { parent[find(a)] = find(b) }

            for (i in entries.indices) {
                for (j in i + 1 until entries.size) {
                    if (score(entries[i], entries[j]) >= THRESHOLD) union(i, j)
                }
            }

            // Count component sizes
            val sizes = IntArray(entries.size)
            for (i in entries.indices) sizes[find(i)]++

            // Assign stable group IDs (root index) or null for isolated entries
            for (i in entries.indices) {
                val root = find(i)
                entries[i].groupId = if (sizes[root] > 1) root else null
            }
        } catch (_: Exception) {
            // Silently leave groupId values unchanged on any unexpected failure
        }
    }

    private fun score(a: FileHistoryEntry, b: FileHistoryEntry): Int {
        var total = 0
        total += timeProximityScore(a, b)
        total += pathPrefixScore(a, b)
        total += extensionScore(a, b)
        return total
    }

    /** Returns the best available timestamp for an entry (max of view/edit). */
    private fun representativeTimestamp(e: FileHistoryEntry): Long? {
        val v = e.lastViewedAt
        val ed = e.lastEditedAt
        return when {
            v != null && ed != null -> maxOf(v, ed)
            v != null -> v
            ed != null -> ed
            else -> null
        }
    }

    private fun timeProximityScore(a: FileHistoryEntry, b: FileHistoryEntry): Int {
        val ta = representativeTimestamp(a) ?: return 0
        val tb = representativeTimestamp(b) ?: return 0
        val diffMs = Math.abs(ta - tb)
        return when {
            diffMs <= 5 * 60_000L  -> 3
            diffMs <= 30 * 60_000L -> 2
            diffMs <= 2 * 3_600_000L -> 1
            else -> 0
        }
    }

    /** Returns the directory portion of [path], handling both / and \ separators. */
    private fun parentDir(path: String): String {
        val lastSep = maxOf(path.lastIndexOf('/'), path.lastIndexOf('\\'))
        return if (lastSep > 0) path.substring(0, lastSep) else ""
    }

    private fun pathPrefixScore(a: FileHistoryEntry, b: FileHistoryEntry): Int {
        val parentA = parentDir(a.relativePath)
        val parentB = parentDir(b.relativePath)
        if (parentA.isBlank() || parentB.isBlank()) return 0
        if (parentA == parentB) return 2
        val grandA = parentDir(parentA)
        val grandB = parentDir(parentB)
        if (grandA.isNotBlank() && grandA == grandB) return 1
        return 0
    }

    private fun extensionScore(a: FileHistoryEntry, b: FileHistoryEntry): Int {
        val extA = a.fileName.substringAfterLast('.', "")
        val extB = b.fileName.substringAfterLast('.', "")
        return if (extA.isNotEmpty() && extA == extB) 1 else 0
    }
}
