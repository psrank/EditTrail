package com.psrank.edittrail

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.*
import javax.swing.border.LineBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * The main UI panel for the EditTrail tool window.
 *
 * Layout (top to bottom):
 * 1. Sort combo (Last Edited / Last Viewed)
 * 2. Search field ("Search files…")
 * 3. Toggle row (Match path | Match content | Regex | Case sensitive)
 * 4. Scrollable [JBList] of [FileHistoryEntry] items
 *
 * Subscribes to [EditTrailTopics.HISTORY_UPDATED] so the list refreshes automatically.
 */
class EditTrailPanel(
    private val project: Project,
    toolWindow: ToolWindow
) : JPanel(BorderLayout()) {

    private val model = DefaultListModel<FileHistoryEntry>()
    private val list = JBList(model)
    private var sortMode: SortMode = SortMode.LAST_EDITED

    // ── Search state ─────────────────────────────────────────────────────────────
    private val searchField = JTextField()
    private val matchPathBox = JCheckBox("Match path")
    private val matchContentBox = JCheckBox("Match content")
    private val regexBox = JCheckBox("Regex")
    private val caseSensitiveBox = JCheckBox("Case sensitive")

    /** Incremented on every search change to discard stale content-search results. */
    private val searchGeneration = AtomicInteger(0)

    /** Debounce timer — restarted on every search-field change. */
    private val debounceTimer = Timer(300) { refresh() }.apply { isRepeats = false }

    private val defaultSearchBorder: javax.swing.border.Border = searchField.border

    init {
        list.cellRenderer = FileHistoryCellRenderer()

        list.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) openSelected()
            }
        })
        list.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER) openSelected()
            }
        })

        // Search field — debounce + regex validation
        searchField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = onSearchChange()
            override fun removeUpdate(e: DocumentEvent) = onSearchChange()
            override fun changedUpdate(e: DocumentEvent) = onSearchChange()
        })

        // Toggle checkboxes all trigger a refresh
        listOf(matchPathBox, matchContentBox, regexBox, caseSensitiveBox).forEach { cb ->
            cb.addActionListener { onSearchChange() }
        }

        val northPanel = JPanel(GridLayout(3, 1))
        northPanel.add(createSortBar())
        northPanel.add(createSearchBar())
        northPanel.add(createToggleBar())

        add(northPanel, BorderLayout.NORTH)
        add(JBScrollPane(list), BorderLayout.CENTER)

        project.messageBus
            .connect(toolWindow.disposable)
            .subscribe(EditTrailTopics.HISTORY_UPDATED, EditTrailListener { refresh() })

        refresh()
    }

    // ── Search change ────────────────────────────────────────────────────────────

    private fun onSearchChange() {
        updateRegexBorder()
        debounceTimer.restart()
    }

    private fun updateRegexBorder() {
        if (regexBox.isSelected && !SearchFilter.isValidRegex(searchField.text)) {
            searchField.border = LineBorder(JBColor.RED, 1)
        } else {
            searchField.border = defaultSearchBorder
        }
    }

    private fun currentSearchOptions() = SearchOptions(
        query = searchField.text,
        matchPath = matchPathBox.isSelected,
        matchContent = matchContentBox.isSelected,
        regex = regexBox.isSelected,
        caseSensitive = caseSensitiveBox.isSelected
    )

    // ── Refresh ──────────────────────────────────────────────────────────────────

    fun refresh() {
        val options = currentSearchOptions()

        if (options.matchContent && options.query.isNotBlank()) {
            refreshWithContentSearch(options)
        } else {
            refreshNamePathOnly(options)
        }
    }

    private fun refreshNamePathOnly(options: SearchOptions) {
        ApplicationManager.getApplication().invokeLater {
            val allEntries = project.service<EditTrailProjectService>()
                .getHistory(sortMode)
                .filter { it.exists }

            val visible = if (options.query.isBlank()) {
                allEntries
            } else if (options.regex && !SearchFilter.isValidRegex(options.query)) {
                // Invalid regex — retain current list unchanged
                return@invokeLater
            } else {
                allEntries.filter { SearchFilter.matches(it, options) }
            }

            applyToModel(visible)
        }
    }

    private fun refreshWithContentSearch(options: SearchOptions) {
        val generation = searchGeneration.incrementAndGet()

        val allEntries = project.service<EditTrailProjectService>()
            .getHistory(sortMode)
            .filter { it.exists }

        val baseCandidates = if (options.query.isBlank()) {
            allEntries
        } else if (options.regex && !SearchFilter.isValidRegex(options.query)) {
            return // invalid regex — do nothing
        } else {
            allEntries.filter { SearchFilter.matches(it, options.copy(matchContent = false)) }
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            val contentMatches = allEntries.filter { entry ->
                if (generation != searchGeneration.get()) return@filter false
                try {
                    val vf = VirtualFileManager.getInstance().findFileByUrl(entry.fileUrl)
                        ?: return@filter false
                    if (vf.fileType.isBinary) return@filter false
                    val text = String(vf.contentsToByteArray(), Charsets.UTF_8)
                    val q = if (options.caseSensitive) options.query else options.query.lowercase()
                    val t = if (options.caseSensitive) text else text.lowercase()
                    t.contains(q)
                } catch (_: Exception) {
                    false
                }
            }

            if (generation != searchGeneration.get()) return@executeOnPooledThread

            val combined = (baseCandidates + contentMatches).distinctBy { it.fileUrl }

            ApplicationManager.getApplication().invokeLater {
                if (generation == searchGeneration.get()) {
                    applyToModel(combined)
                }
            }
        }
    }

    private fun applyToModel(entries: List<FileHistoryEntry>) {
        model.clear()
        if (entries.isEmpty()) {
            list.emptyText.text = "No recent files. Open and edit files to see them here."
        } else {
            list.emptyText.text = ""
            entries.forEach { model.addElement(it) }
        }
    }

    // ── Actions ──────────────────────────────────────────────────────────────────

    private fun openSelected() {
        val entry = list.selectedValue ?: return
        val vf = VirtualFileManager.getInstance().findFileByUrl(entry.fileUrl)
        if (vf != null && vf.exists()) {
            FileEditorManager.getInstance(project).openFile(vf, true)
        } else {
            entry.exists = false
            refresh()
        }
    }

    // ── Toolbar builders ─────────────────────────────────────────────────────────

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

    private fun createSearchBar(): JPanel {
        searchField.toolTipText = "Search files…"
        val bar = JPanel(BorderLayout())
        bar.add(searchField, BorderLayout.CENTER)
        return bar
    }

    private fun createToggleBar(): JPanel {
        val bar = JPanel(FlowLayout(FlowLayout.LEFT, 6, 2))
        bar.add(matchPathBox)
        bar.add(matchContentBox)
        bar.add(regexBox)
        bar.add(caseSensitiveBox)
        return bar
    }
}
