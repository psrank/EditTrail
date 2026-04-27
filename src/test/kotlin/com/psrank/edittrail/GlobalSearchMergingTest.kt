package com.psrank.edittrail

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * TDD tests for the global project search result merging logic.
 * Tests the ordering rule: history entries first, project file results second.
 * Also tests file-type chip filter composition across both result types.
 *
 * Covers: specs/global-search — priority ordering, file-type composition
 */
class GlobalSearchMergingTest {

    // ── History-first ordering ────────────────────────────────────────────────────

    @Test
    fun `history results appear before project file results in merged list`() {
        val historyResult = EditTrailResult.HistoryResult(makeEntry("UserService.cs", "C#"))
        val projectResult = makeProjectResult("UserSettingsService.cs", "C#")

        val merged = mergeResults(
            history = listOf(historyResult.entry),
            project = listOf(projectResult)
        )

        assertEquals(2, merged.size)
        assertTrue(merged[0] is EditTrailResult.HistoryResult)
        assertTrue(merged[1] is EditTrailResult.ProjectFileResult)
    }

    @Test
    fun `when only history results exist merged list contains only those`() {
        val entry = makeEntry("Main.kt", "Kotlin")
        val merged = mergeResults(history = listOf(entry), project = emptyList())
        assertEquals(1, merged.size)
        assertTrue(merged[0] is EditTrailResult.HistoryResult)
    }

    @Test
    fun `when only project results exist merged list contains only those`() {
        val projectResult = makeProjectResult("InvoiceService.cs", "C#")
        val merged = mergeResults(history = emptyList(), project = listOf(projectResult))
        assertEquals(1, merged.size)
        assertTrue(merged[0] is EditTrailResult.ProjectFileResult)
    }

    @Test
    fun `empty history and empty project yields empty merged list`() {
        val merged = mergeResults(history = emptyList(), project = emptyList())
        assertTrue(merged.isEmpty())
    }

    @Test
    fun `multiple history results all appear before any project result`() {
        val h1 = makeEntry("A.cs", "C#")
        val h2 = makeEntry("B.cs", "C#")
        val p1 = makeProjectResult("X.cs", "C#")
        val p2 = makeProjectResult("Y.cs", "C#")

        val merged = mergeResults(history = listOf(h1, h2), project = listOf(p1, p2))
        assertEquals(4, merged.size)
        assertTrue(merged[0] is EditTrailResult.HistoryResult)
        assertTrue(merged[1] is EditTrailResult.HistoryResult)
        assertTrue(merged[2] is EditTrailResult.ProjectFileResult)
        assertTrue(merged[3] is EditTrailResult.ProjectFileResult)
    }

    // ── File-type filter on HistoryResult ─────────────────────────────────────────

    @Test
    fun `file-type filter retains matching history results`() {
        val entry = makeEntry("UserService.cs", "C#")
        val merged = mergeResults(history = listOf(entry), project = emptyList())
        val filtered = filterByTypes(merged, setOf("C#"))
        assertEquals(1, filtered.size)
    }

    @Test
    fun `file-type filter removes non-matching history results`() {
        val entry = makeEntry("appsettings.json", "JSON")
        val merged = mergeResults(history = listOf(entry), project = emptyList())
        val filtered = filterByTypes(merged, setOf("C#"))
        assertTrue(filtered.isEmpty())
    }

    // ── File-type filter on ProjectFileResult ─────────────────────────────────────

    @Test
    fun `file-type filter retains matching project results`() {
        val project = makeProjectResult("InvoiceService.cs", "C#")
        val merged = mergeResults(history = emptyList(), project = listOf(project))
        val filtered = filterByTypes(merged, setOf("C#"))
        assertEquals(1, filtered.size)
    }

    @Test
    fun `file-type filter removes non-matching project results`() {
        val project = makeProjectResult("appsettings.json", "JSON")
        val merged = mergeResults(history = emptyList(), project = listOf(project))
        val filtered = filterByTypes(merged, setOf("C#"))
        assertTrue(filtered.isEmpty())
    }

    // ── File-type filter across both ──────────────────────────────────────────────

    @Test
    fun `file-type filter with multiple types retains matches of any type`() {
        val h1 = makeEntry("UserService.cs", "C#")
        val h2 = makeEntry("appsettings.json", "JSON")
        val p1 = makeProjectResult("Config.yaml", "YAML")
        val p2 = makeProjectResult("InvoiceService.cs", "C#")

        val merged = mergeResults(history = listOf(h1, h2), project = listOf(p1, p2))
        val filtered = filterByTypes(merged, setOf("C#", "JSON"))

        assertEquals(3, filtered.size)
        assertTrue(filtered.none { it is EditTrailResult.ProjectFileResult && it.fileType == "YAML" })
    }

    @Test
    fun `empty type filter set retains all results`() {
        val h1 = makeEntry("UserService.cs", "C#")
        val p1 = makeProjectResult("Config.yaml", "YAML")
        val merged = mergeResults(history = listOf(h1), project = listOf(p1))
        val filtered = filterByTypes(merged, emptySet())
        assertEquals(2, filtered.size)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    /**
     * Merges history entries and project results: history first, then project.
     * This is the pure logic that will live in EditTrailPanel.applyToModel.
     */
    private fun mergeResults(
        history: List<FileHistoryEntry>,
        project: List<EditTrailResult.ProjectFileResult>
    ): List<EditTrailResult> =
        history.map { EditTrailResult.HistoryResult(it) } + project

    /**
     * Applies file-type chip filter to a merged result list.
     * Empty set = show all.
     */
    private fun filterByTypes(
        results: List<EditTrailResult>,
        selectedTypes: Set<String>
    ): List<EditTrailResult> {
        if (selectedTypes.isEmpty()) return results
        return results.filter { result ->
            when (result) {
                is EditTrailResult.HistoryResult ->
                    FileTypeClassifier.classify(result.entry.fileName) in selectedTypes
                is EditTrailResult.ProjectFileResult ->
                    result.fileType in selectedTypes
            }
        }
    }

    private fun makeEntry(name: String, @Suppress("UNUSED_PARAMETER") type: String) =
        FileHistoryEntry(
            fileUrl = "file:///src/$name",
            fileName = name,
            relativePath = "src/$name",
            lastViewedAt = System.currentTimeMillis(),
            lastEditedAt = null,
            viewCount = 1,
            editCount = 0,
            exists = true
        )

    private fun makeProjectResult(fileName: String, fileType: String) =
        EditTrailResult.ProjectFileResult(
            fileName = fileName,
            relativePath = "src/$fileName",
            fileType = fileType
        )
}
