package com.psrank.edittrail

import java.util.concurrent.ConcurrentHashMap

/**
 * Manages the in-memory file history with deduplication, LRU eviction, and sorting.
 *
 * @param maxSize Maximum number of entries before LRU eviction kicks in (default 500).
 */
class FileHistoryRepository(private val maxSize: Int = 500) {

    // LinkedHashMap preserves insertion order; values are mutated in place for deduplication.
    private val entries = LinkedHashMap<String, FileHistoryEntry>()

    // ── recordView ───────────────────────────────────────────────────────────────

    /**
     * Records a file view event. Ignores blank URLs.
     * Deduplicates by [fileUrl] — existing entries are updated in place.
     */
    fun recordView(fileUrl: String, fileName: String, relativePath: String) {
        if (fileUrl.isBlank()) return
        val isNew = !entries.containsKey(fileUrl)
        val entry = entries.getOrPut(fileUrl) {
            FileHistoryEntry(fileUrl = fileUrl, fileName = fileName, relativePath = relativePath)
        }
        entry.lastViewedAt = System.currentTimeMillis()
        entry.viewCount += 1
        if (isNew) evictIfNeeded()
    }

    // ── recordEdit ───────────────────────────────────────────────────────────────

    /**
     * Records a file edit event. Ignores blank URLs.
     * Deduplicates by [fileUrl] — existing entries are updated in place.
     */
    fun recordEdit(fileUrl: String, fileName: String, relativePath: String) {
        if (fileUrl.isBlank()) return
        val isNew = !entries.containsKey(fileUrl)
        val entry = entries.getOrPut(fileUrl) {
            FileHistoryEntry(fileUrl = fileUrl, fileName = fileName, relativePath = relativePath)
        }
        entry.lastEditedAt = System.currentTimeMillis()
        entry.editCount += 1
        if (isNew) evictIfNeeded()
    }

    // ── getHistory ───────────────────────────────────────────────────────────────

    /**
     * Returns all history entries sorted by [sortMode].
     *
     * For [SortMode.LAST_EDITED]:
     * - Entries with a `lastEditedAt` value appear first, sorted newest-first.
     * - View-only entries (no `lastEditedAt`) appear after, sorted by `lastViewedAt` newest-first.
     *
     * For [SortMode.LAST_VIEWED]:
     * - All entries sorted by `lastViewedAt` (falling back to `lastEditedAt`) newest-first.
     */
    fun getHistory(sortMode: SortMode): List<FileHistoryEntry> {
        return when (sortMode) {
            SortMode.LAST_EDITED -> entries.values.sortedWith(Comparator { a, b ->
                val aEdit = a.lastEditedAt
                val bEdit = b.lastEditedAt
                when {
                    aEdit != null && bEdit != null -> bEdit.compareTo(aEdit)
                    aEdit != null -> -1   // a has edit, b does not → a first
                    bEdit != null -> 1    // b has edit, a does not → b first
                    else -> {             // both view-only → sort by lastViewedAt
                        (b.lastViewedAt ?: 0L).compareTo(a.lastViewedAt ?: 0L)
                    }
                }
            })

            SortMode.LAST_VIEWED -> entries.values.sortedByDescending {
                it.lastViewedAt ?: it.lastEditedAt ?: 0L
            }
        }
    }

    // ── Persistence helpers ──────────────────────────────────────────────────────

    /** Replaces all entries with [entriesToLoad] (used during state restore). */
    fun loadEntries(entriesToLoad: List<FileHistoryEntry>) {
        entries.clear()
        entriesToLoad.forEach { entries[it.fileUrl] = it }
    }

    /** Returns a snapshot of all current entries (used when persisting state). */
    fun getAllEntries(): List<FileHistoryEntry> = entries.values.toList()

    // ── LRU eviction ────────────────────────────────────────────────────────────

    private fun evictIfNeeded() {
        while (entries.size > maxSize) {
            val oldest = entries.values.minByOrNull { entry ->
                maxOf(entry.lastViewedAt ?: 0L, entry.lastEditedAt ?: 0L)
            }
            oldest?.let { entries.remove(it.fileUrl) }
        }
    }
}
