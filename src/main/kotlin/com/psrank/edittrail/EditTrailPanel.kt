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

    // ── File-type filter state ────────────────────────────────────────────────────
    private val selectedFileTypes: MutableSet<String> = mutableSetOf()
    private val chipBarPanel = JPanel(FlowLayout(FlowLayout.LEFT, 4, 2))

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

        val northPanel = JPanel(GridLayout(4, 1))
        northPanel.add(createSortBar())
        northPanel.add(createSearchBar())
        northPanel.add(createToggleBar())
        northPanel.add(createChipBarScrollPane())

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

    /** Called with the search-filtered list. Computes chips, rebuilds chip bar, then applies file-type filter. */
    private fun applyToModel(searchFiltered: List<FileHistoryEntry>) {
        // 4.1 Compute per-type counts from search-filtered list
        val counts: Map<String, Int> = searchFiltered
            .groupingBy { FileTypeClassifier.classify(it.fileName) }
            .eachCount()

        // 4.2 Build chip list; mark selected if the label is in selectedFileTypes
        val chips: List<FileTypeChip> = counts.entries
            .sortedByDescending { it.value }
            .map { (label, count) -> FileTypeChip(label, count, label in selectedFileTypes) }

        // 4.3 Rebuild chip bar
        rebuildChipBar(chips)

        // 4.4 Apply file-type filter
        val visible = if (selectedFileTypes.isEmpty()) {
            searchFiltered
        } else {
            searchFiltered.filter { FileTypeClassifier.classify(it.fileName) in selectedFileTypes }
        }

        model.clear()
        if (visible.isEmpty()) {
            list.emptyText.text = "No recent files. Open and edit files to see them here."
        } else {
            list.emptyText.text = ""
            visible.forEach { model.addElement(it) }
        }
    }

    /** Rebuilds the chip bar from the supplied chip list. Must be called on the EDT. */
    private fun rebuildChipBar(chips: List<FileTypeChip>) {
        chipBarPanel.removeAll()

        // 3.4 / 3.5  "All" button — appears bold when no type is selected
        val allButton = JButton("All")
        if (selectedFileTypes.isEmpty()) {
            allButton.font = allButton.font.deriveFont(java.awt.Font.BOLD)
        }
        // 3.7 Wire All button to clear selection then refresh
        allButton.addActionListener {
            selectedFileTypes.clear()
            refresh()
        }
        chipBarPanel.add(allButton)

        // 3.4 / 3.6  Per-type toggle buttons
        chips.forEach { chip ->
            val toggle = JToggleButton("${chip.label} (${chip.count})", chip.selected)
            toggle.addActionListener {
                if (toggle.isSelected) selectedFileTypes.add(chip.label)
                else selectedFileTypes.remove(chip.label)
                refresh()
            }
            chipBarPanel.add(toggle)
        }

        chipBarPanel.revalidate()
        chipBarPanel.repaint()
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

    private fun createChipBarScrollPane(): JScrollPane {
        val scroll = JScrollPane(chipBarPanel)
        scroll.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        scroll.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_NEVER
        scroll.border = null
        return scroll
    }
}
