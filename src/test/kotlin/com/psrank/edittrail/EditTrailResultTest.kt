package com.psrank.edittrail

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * TDD tests for [EditTrailResult] sealed class.
 * Expected to fail compilation until EditTrailResult is implemented.
 *
 * Covers: specs/global-search, specs/result-renderer
 */
class EditTrailResultTest {

    // ── HistoryResult ─────────────────────────────────────────────────────────────

    @Test
    fun `HistoryResult wraps a FileHistoryEntry`() {
        val entry = makeEntry("UserService.cs")
        val result = EditTrailResult.HistoryResult(entry)
        assertEquals(entry, result.entry)
    }

    @Test
    fun `HistoryResult is an EditTrailResult`() {
        val result: EditTrailResult = EditTrailResult.HistoryResult(makeEntry("Main.kt"))
        assertNotNull(result)
    }

    @Test
    fun `two HistoryResults with same entry are equal`() {
        val entry = makeEntry("UserService.cs")
        val a = EditTrailResult.HistoryResult(entry)
        val b = EditTrailResult.HistoryResult(entry)
        assertEquals(a, b)
    }

    @Test
    fun `two HistoryResults with different entries are not equal`() {
        val a = EditTrailResult.HistoryResult(makeEntry("UserService.cs"))
        val b = EditTrailResult.HistoryResult(makeEntry("InvoiceService.cs"))
        assertNotEquals(a, b)
    }

    // ── ProjectFileResult ─────────────────────────────────────────────────────────

    @Test
    fun `ProjectFileResult stores fileName`() {
        val result = makeProjectResult("InvoiceService.cs", "src/InvoiceService.cs", "C#")
        assertEquals("InvoiceService.cs", result.fileName)
    }

    @Test
    fun `ProjectFileResult stores relativePath`() {
        val result = makeProjectResult("InvoiceService.cs", "src/billing/InvoiceService.cs", "C#")
        assertEquals("src/billing/InvoiceService.cs", result.relativePath)
    }

    @Test
    fun `ProjectFileResult stores fileType`() {
        val result = makeProjectResult("appsettings.json", "src/appsettings.json", "JSON")
        assertEquals("JSON", result.fileType)
    }

    @Test
    fun `ProjectFileResult is an EditTrailResult`() {
        val result: EditTrailResult = makeProjectResult("Foo.kt", "src/Foo.kt", "Kotlin")
        assertNotNull(result)
    }

    @Test
    fun `two ProjectFileResults with same fields are equal`() {
        val a = makeProjectResult("Foo.kt", "src/Foo.kt", "Kotlin")
        val b = makeProjectResult("Foo.kt", "src/Foo.kt", "Kotlin")
        assertEquals(a, b)
    }

    @Test
    fun `two ProjectFileResults with different fileNames are not equal`() {
        val a = makeProjectResult("Foo.kt", "src/Foo.kt", "Kotlin")
        val b = makeProjectResult("Bar.kt", "src/Bar.kt", "Kotlin")
        assertNotEquals(a, b)
    }

    // ── Sealed exhaustiveness ─────────────────────────────────────────────────────

    @Test
    fun `exhaustive when over EditTrailResult compiles and runs for HistoryResult`() {
        val result: EditTrailResult = EditTrailResult.HistoryResult(makeEntry("Main.kt"))
        val label = when (result) {
            is EditTrailResult.HistoryResult -> "history"
            is EditTrailResult.ProjectFileResult -> "project"
        }
        assertEquals("history", label)
    }

    @Test
    fun `exhaustive when over EditTrailResult compiles and runs for ProjectFileResult`() {
        val result: EditTrailResult = makeProjectResult("Foo.cs", "src/Foo.cs", "C#")
        val label = when (result) {
            is EditTrailResult.HistoryResult -> "history"
            is EditTrailResult.ProjectFileResult -> "project"
        }
        assertEquals("project", label)
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

    private fun makeProjectResult(
        fileName: String,
        relativePath: String,
        fileType: String
    ): EditTrailResult.ProjectFileResult =
        EditTrailResult.ProjectFileResult(
            fileName = fileName,
            relativePath = relativePath,
            fileType = fileType
        )
}
