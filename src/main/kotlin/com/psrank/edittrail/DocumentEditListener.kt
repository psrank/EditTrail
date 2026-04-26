package com.psrank.edittrail

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Per-file [DocumentListener] that records edit events with a 2-second debounce.
 *
 * Multiple rapid keystrokes are coalesced into a single `recordEdit` call so that
 * the project service is not hammered on every keystroke.
 *
 * Instantiated by [EditorSelectionListener] when a file is opened. Removed
 * automatically when the project closes because it is added with the project as
 * its parent Disposable.
 *
 * @param project     The project this listener belongs to.
 * @param file        The virtual file whose document is being observed.
 * @param executor    Shared single-threaded executor (owned by [EditorSelectionListener]).
 * @param pendingTasks Map of per-URL debounce tasks; each new event cancels the previous task.
 */
class DocumentEditListener(
    private val project: Project,
    private val file: VirtualFile,
    private val executor: ScheduledExecutorService,
    private val pendingTasks: ConcurrentHashMap<String, ScheduledFuture<*>>
) : DocumentListener {

    override fun documentChanged(event: DocumentEvent) {
        val url = file.url
        // Cancel any pending debounce task for this file URL and schedule a fresh one.
        pendingTasks[url]?.cancel(false)
        val task = executor.schedule(
            {
                ApplicationManager.getApplication().invokeLater {
                    // Guard: service may not exist if project was disposed between
                    // the schedule and the execution.
                    project.getServiceIfCreated(EditTrailProjectService::class.java)
                        ?.recordEdit(file)
                }
            },
            DEBOUNCE_DELAY_SECONDS,
            TimeUnit.SECONDS
        )
        pendingTasks[url] = task
    }

    companion object {
        private const val DEBOUNCE_DELAY_SECONDS = 2L
    }
}
