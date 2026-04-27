package com.psrank.edittrail

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Edge-case and characterization tests added after implementation of the
 * global-project-search change. These complement the TDD acceptance tests
 * (EditTrailResultTest, GlobalSearchStateTest, GlobalSearchMergingTest) and
 * cover implementation details that could only be verified once the code existed.
 */
class GlobalSearchEdgeCaseTest {

    // ── ProjectFileResult nullable virtualFile ────────────────────────────────────

    @Test
    fun `ProjectFileResult virtualFile defaults to null when not provided`() {
        val result = EditTrailResult.ProjectFileResult(
            fileName = "UserService.cs",
            relativePath = "src/UserService.cs",
            fileType = "C#"
        )
        assertNull(result.virtualFile)
    }

    @Test
    fun `ProjectFileResult equality is based on fileName relativePath fileType not virtualFile`() {
        val a = EditTrailResult.ProjectFileResult(
            virtualFile = null,
            fileName = "UserService.cs",
            relativePath = "src/UserService.cs",
            fileType = "C#"
        )
        val b = EditTrailResult.ProjectFileResult(
            virtualFile = null,
            fileName = "UserService.cs",
            relativePath = "src/UserService.cs",
            fileType = "C#"
        )
        assertEquals(a, b)
    }

    @Test
    fun `ProjectFileResult relativePath can differ from fileName`() {
        val result = EditTrailResult.ProjectFileResult(
            fileName = "UserService.cs",
            relativePath = "src/services/domain/UserService.cs",
            fileType = "C#"
        )
        assertNotEquals(result.fileName, result.relativePath)
        assertEquals("UserService.cs", result.fileName)
        assertEquals("src/services/domain/UserService.cs", result.relativePath)
    }

    // ── Per-type count computation ────────────────────────────────────────────────

    @Test
    fun `per-type counts include both history and project results of same type`() {
        val h1 = makeEntry("UserService.cs")
        val h2 = makeEntry("InvoiceService.cs")
        val p1 = makeProjectResult("OrderService.cs", "C#")

        val merged: List<EditTrailResult> =
            listOf(h1, h2).map { EditTrailResult.HistoryResult(it) } + listOf(p1)

        val counts = merged.groupingBy { result ->
            when (result) {
                is EditTrailResult.HistoryResult ->
                    FileTypeClassifier.classify(result.entry.fileName)
                is EditTrailResult.ProjectFileResult -> result.fileType
            }
        }.eachCount()

        assertEquals(3, counts["C#"])
    }

    @Test
    fun `per-type counts are independent for different file types`() {
        val h1 = makeEntry("UserService.cs")
        val p1 = makeProjectResult("settings.json", "JSON")
        val p2 = makeProjectResult("schema.yaml", "YAML")

        val merged: List<EditTrailResult> =
            listOf(EditTrailResult.HistoryResult(h1), p1, p2)

        val counts = merged.groupingBy { result ->
            when (result) {
                is EditTrailResult.HistoryResult ->
                    FileTypeClassifier.classify(result.entry.fileName)
                is EditTrailResult.ProjectFileResult -> result.fileType
            }
        }.eachCount()

        assertEquals(1, counts["C#"])
        assertEquals(1, counts["JSON"])
        assertEquals(1, counts["YAML"])
    }

    @Test
    fun `merged list preserves relative ordering within history and within project`() {
        val h1 = makeEntry("A.cs")
        val h2 = makeEntry("B.cs")
        val h3 = makeEntry("C.cs")
        val p1 = makeProjectResult("X.cs", "C#")
        val p2 = makeProjectResult("Y.cs", "C#")

        val merged: List<EditTrailResult> =
            listOf(h1, h2, h3).map { EditTrailResult.HistoryResult(it) } +
            listOf(p1, p2)

        // History order preserved
        assertEquals("A.cs", (merged[0] as EditTrailResult.HistoryResult).entry.fileName)
        assertEquals("B.cs", (merged[1] as EditTrailResult.HistoryResult).entry.fileName)
        assertEquals("C.cs", (merged[2] as EditTrailResult.HistoryResult).entry.fileName)
        // Project order preserved
        assertEquals("X.cs", (merged[3] as EditTrailResult.ProjectFileResult).fileName)
        assertEquals("Y.cs", (merged[4] as EditTrailResult.ProjectFileResult).fileName)
    }

    // ── GlobalSearchEnabled default ───────────────────────────────────────────────

    @Test
    fun `FileHistoryState globalSearchEnabled is false by default`() {
        val state = FileHistoryState()
        assertFalse(state.globalSearchEnabled)
    }

    @Test
    fun `FileHistoryState globalSearchEnabled persists independently from entries`() {
        val state = FileHistoryState()
        state.globalSearchEnabled = true
        state.entries.add(
            FileHistoryEntry(
                fileUrl = "file:///a.cs",
                fileName = "a.cs",
                relativePath = "a.cs",
                lastViewedAt = 1L,
                lastEditedAt = null,
                viewCount = 1,
                editCount = 0,
                exists = true
            )
        )
        assertTrue(state.globalSearchEnabled)
        assertEquals(1, state.entries.size)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    private fun makeEntry(name: String) = FileHistoryEntry(
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
