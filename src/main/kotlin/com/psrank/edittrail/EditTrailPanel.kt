package com.psrank.edittrail

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JComboBox
import javax.swing.JPanel

/**
 * The main UI panel for the EditTrail tool window.
 *
 * Contains:
 * - A sort-mode toolbar (Last Edited / Last Viewed)
 * - A [JBList] of [FileHistoryEntry] items backed by a [DefaultListModel]
 * - An empty-state placeholder when no history exists
 *
 * Subscribes to [EditTrailTopics.HISTORY_UPDATED] on the project message bus so
 * the list refreshes automatically whenever the history changes. The message bus
 * connection is tied to the [toolWindow]'s disposable lifetime.
 */
class EditTrailPanel(
    private val project: Project,
    toolWindow: ToolWindow
) : JPanel(BorderLayout()) {

    private val model = DefaultListModel<FileHistoryEntry>()
    private val list = JBList(model)
    private var sortMode: SortMode = SortMode.LAST_EDITED

    init {
        list.cellRenderer = FileHistoryCellRenderer()

        // Double-click opens the selected file.
        list.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) openSelected()
            }
        })

        // Enter key opens the selected file.
        list.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER) openSelected()
            }
        })

        add(createSortBar(), BorderLayout.NORTH)
        add(JBScrollPane(list), BorderLayout.CENTER)

        // Subscribe to history changes — connection disposed when toolWindow closes.
        project.messageBus
            .connect(toolWindow.disposable)
            .subscribe(EditTrailTopics.HISTORY_UPDATED, EditTrailListener { refresh() })

        // Initial population.
        refresh()
    }

    // ── Refresh ──────────────────────────────────────────────────────────────────

    /** Reloads the list from the project service on the EDT. */
    fun refresh() {
        ApplicationManager.getApplication().invokeLater {
            val entries = project.service<EditTrailProjectService>()
                .getHistory(sortMode)
                .filter { it.exists }

            model.clear()

            if (entries.isEmpty()) {
                list.emptyText.text =
                    "No recent files. Open and edit files to see them here."
            } else {
                list.emptyText.text = ""
                entries.forEach { model.addElement(it) }
            }
        }
    }

    // ── Actions ──────────────────────────────────────────────────────────────────

    private fun openSelected() {
        val entry = list.selectedValue ?: return
        val vf = VirtualFileManager.getInstance().findFileByUrl(entry.fileUrl)
        if (vf != null && vf.exists()) {
            FileEditorManager.getInstance(project).openFile(vf, /* focusEditor = */ true)
        } else {
            // File no longer exists — mark it and refresh to remove from list.
            entry.exists = false
            refresh()
        }
    }

    // ── Toolbar ──────────────────────────────────────────────────────────────────

    private fun createSortBar(): JPanel {
        val sortOptions = arrayOf("Last Edited", "Last Viewed")
        val combo = JComboBox(sortOptions)
        combo.selectedIndex = 0
        combo.addActionListener {
            sortMode = if (combo.selectedIndex == 0) SortMode.LAST_EDITED else SortMode.LAST_VIEWED
            refresh()
        }

        val bar = JPanel(FlowLayout(FlowLayout.LEFT, 4, 2))
        bar.add(JBLabel("Sort:"))
        bar.add(combo)
        return bar
    }
}
