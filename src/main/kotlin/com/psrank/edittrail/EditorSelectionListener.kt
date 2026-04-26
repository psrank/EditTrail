package com.psrank.edittrail

import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture

/**
 * Project-level listener registered in plugin.xml via <projectListeners>.
 *
 * Responsibilities:
 * 1. Track viewed files when a file is opened or the active tab changes.
 * 2. Attach a [DocumentEditListener] to each newly opened document so that
 *    edit events can be debounced and recorded.
 *
 * Both the debounce executor and all pending scheduled tasks are cancelled
 * when the project closes (this listener implements Disposable and is
 * registered with the project's parent Disposable).
 */
class EditorSelectionListener(private val project: Project) :
    FileEditorManagerListener {

    /** Shared executor for the 2-second debounce across all open files. */
    internal val debounceExecutor = Executors.newSingleThreadScheduledExecutor()

    /** Active debounce tasks keyed by file URL; cancelled and replaced on each new event. */
    internal val pendingTasks = ConcurrentHashMap<String, ScheduledFuture<*>>()

    init {
        // Register for disposal when the project closes so all timers are cleaned up.
        Disposer.register(project) {
            pendingTasks.values.forEach { it.cancel(false) }
            debounceExecutor.shutdown()
        }
    }

    // ── FileEditorManagerListener ────────────────────────────────────────────────

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        // Record view
        recordView(file)

        // Attach a document-level edit listener for this file so edits are tracked.
        val document = com.intellij.openapi.fileEditor.FileDocumentManager
            .getInstance().getDocument(file) ?: return
        val editListener = DocumentEditListener(project, file, debounceExecutor, pendingTasks)
        // Pass project as the parent Disposable — the listener is removed automatically
        // when the project closes, preventing memory leaks.
        document.addDocumentListener(editListener, project)
    }

    override fun selectionChanged(event: FileEditorManagerEvent) {
        event.newFile?.let { recordView(it) }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private fun recordView(file: VirtualFile) {
        project.service<EditTrailProjectService>().recordView(file)
    }
}
