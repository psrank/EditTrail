package com.psrank.edittrail

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.fileEditor.TextEditorWithPreview
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile

/**
 * Centralises how EditTrail opens files from the tool window.
 *
 * Markdown files are routed through the text editor with the split-preview layout
 * forced to editor-only. Otherwise the Markdown plugin can start or cancel its
 * JCEF preview page while EditTrail is navigating between files, surfacing noisy
 * `ERR_ABORTED` load failures from the preview coroutine.
 */
object FileOpenNavigator {

    private val TEXT_EDITOR_FIRST_EXTENSIONS = setOf(
        "md",
        "markdown",
        "mdown",
        "mkd",
        "mkdn",
    )

    private val defaultLayoutForFileKey: Key<TextEditorWithPreview.Layout>? by lazy {
        runCatching {
            @Suppress("UNCHECKED_CAST")
            TextEditorWithPreview::class.java
                .getDeclaredField("DEFAULT_LAYOUT_FOR_FILE")
                .get(null) as Key<TextEditorWithPreview.Layout>
        }.getOrNull()
    }

    fun open(project: Project, file: VirtualFile, requestFocus: Boolean = true) {
        val manager = FileEditorManager.getInstance(project)
        forceEditorOnlyForOpenMarkdownEditors(manager)

        if (shouldPreferTextEditor(file.name)) {
            forceEditorOnlyMarkdownLayout(manager, file)
            val descriptor = OpenFileDescriptor(project, file)
            val editor = manager.openTextEditor(descriptor, requestFocus)
            forceEditorOnlyMarkdownLayout(manager, file)
            if (editor != null) {
                return
            }
        }

        manager.openFile(file, requestFocus)
        forceEditorOnlyMarkdownLayout(manager, file)
    }

    internal fun shouldPreferTextEditor(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        return extension in TEXT_EDITOR_FIRST_EXTENSIONS
    }

    private fun forceEditorOnlyForOpenMarkdownEditors(manager: FileEditorManager) {
        manager.openFiles.forEach { openFile ->
            forceEditorOnlyMarkdownLayout(manager, openFile)
        }
    }

    private fun forceEditorOnlyMarkdownLayout(manager: FileEditorManager, file: VirtualFile) {
        if (!shouldPreferTextEditor(file.name)) return

        defaultLayoutForFileKey?.let { layoutKey ->
            file.putUserData(layoutKey, TextEditorWithPreview.Layout.SHOW_EDITOR)
        }
        manager.getAllEditors(file)
            .filterIsInstance<TextEditorWithPreview>()
            .forEach { editorWithPreview ->
                editorWithPreview.setLayout(TextEditorWithPreview.Layout.SHOW_EDITOR)
            }
    }
}
