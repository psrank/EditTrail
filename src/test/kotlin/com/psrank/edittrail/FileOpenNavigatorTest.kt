package com.psrank.edittrail

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class FileOpenNavigatorTest {

    private val navigatorSource: String by lazy {
        val path = Path.of("src/main/kotlin/com/psrank/edittrail/FileOpenNavigator.kt")
        check(Files.exists(path)) { "FileOpenNavigator.kt not found at expected path: $path" }
        Files.readString(path)
    }

    @Test
    fun `markdown extensions prefer the text editor`() {
        listOf(
            "README.md",
            "CHANGELOG.markdown",
            "notes.mdown",
            "draft.mkd",
            "guide.mkdn",
            "MixedCase.MD",
        ).forEach { fileName ->
            assertTrue(
                FileOpenNavigator.shouldPreferTextEditor(fileName),
                "Expected `$fileName` to prefer opening in the text editor first"
            )
        }
    }

    @Test
    fun `non markdown extensions keep the default editor path`() {
        listOf(
            "EditTrailPanel.kt",
            "plugin.xml",
            "archive.tar.gz",
            "Makefile",
            "diagram.drawio",
        ).forEach { fileName ->
            assertFalse(
                FileOpenNavigator.shouldPreferTextEditor(fileName),
                "Expected `$fileName` to keep the default editor opening path"
            )
        }
    }

    @Test
    fun `markdown navigation requests editor-only split layout`() {
        assertTrue(
            navigatorSource.contains("getDeclaredField(\"DEFAULT_LAYOUT_FOR_FILE\")") &&
                navigatorSource.contains("TextEditorWithPreview.Layout.SHOW_EDITOR"),
            "Markdown files opened from EditTrail should request the editor-only split layout before opening."
        )
    }

    @Test
    fun `open markdown split editors are forced to editor-only before navigation`() {
        assertTrue(
            navigatorSource.contains("manager.openFiles.forEach") &&
                navigatorSource.contains("manager.getAllEditors(file)") &&
                navigatorSource.contains("filterIsInstance<TextEditorWithPreview>()"),
            "EditTrail should suppress already-open Markdown preview layouts before navigating to another file."
        )
    }
}
