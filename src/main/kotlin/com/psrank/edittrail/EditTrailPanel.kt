package com.psrank.edittrail

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.psrank.edittrail.actions.CaseSensitiveToggleAction
import com.psrank.edittrail.actions.ClearHistoryAction
import com.psrank.edittrail.actions.EditTrailToolbarState
import com.psrank.edittrail.actions.GlobalSearchToggleAction
import com.psrank.edittrail.actions.MatchContentToggleAction
import com.psrank.edittrail.actions.MatchPathToggleAction
import com.psrank.edittrail.actions.MatchPatternToggleAction
import com.psrank.edittrail.actions.RecalculateGroupsAction
import java.awt.BorderLayout
import java.awt.FlowLayout
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
 * 3. Icon toolbar (path | content | regex | case sensitive | global | recalc | clear)
 * 4. File-type chip bar
 * 5. Scrollable [JBList] of [EditTrailResult] items
 *
 * Subscribes to [EditTrailTopics.HISTORY_UPDATED] so the list refreshes automatically.
 */
class EditTrailPanel(
    private val project: Project,
    toolWindow: ToolWindow
) : JPanel(BorderLayout()) {

    private val model = DefaultListModel<EditTrailResult>()
    private val list = JBList(model)
    private var sortMode: SortMode = run {
        try {
            val s = EditTrailAppSettings.getInstance().state.defaultSortMode
            if (s == "LAST_VIEWED") SortMode.LAST_VIEWED else SortMode.LAST_EDITED
        } catch (_: Exception) {
            SortMode.LAST_EDITED
        }
    }

    // ── Search state ─────────────────────────────────────────────────────────────
    private val searchField = JTextField()

    private val toolbarState: EditTrailToolbarState = EditTrailToolbarState(
        project = project,
        onSearchChange = { onSearchChange() },
        onClearHistory = { promptAndClearHistory() },
        onRecalculateGroups = { dispatchRecalculateGroups() },
    )

    // ── File-type filter state ────────────────────────────────────────────────────
    private val selectedFileTypes: MutableSet<String> = mutableSetOf()
    private val chipBarPanel = JPanel(FlowLayout(FlowLayout.CENTER, 1, 6))

    /** Tracks the total visible result count for the `All` chip label. */
    private var lastVisibleCount: Int = 0

    /** Incremented on every search change to discard stale content-search results. */
    private val searchGeneration = AtomicInteger(0)

    /** Incremented on every global scan dispatch to discard stale project-file results. */
    private val globalScanGeneration = AtomicInteger(0)

    /** Debounce timer — restarted on every search-field change. */
    private val debounceTimer = Timer(300) { refresh() }.apply { isRepeats = false }

    private val defaultSearchBorder: javax.swing.border.Border = searchField.border

    init {
        border = JBUI.Borders.empty(6)

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

        val northPanel = JPanel()
        northPanel.layout = BoxLayout(northPanel, BoxLayout.Y_AXIS)
        // Each row sizes to its preferred height (no equal-share padding).
        listOf(
            createSortBar(),
            createSearchBar(),
            createIconToolbar(),
            createChipBarComponent(),
        ).forEach { row ->
            row.alignmentX = LEFT_ALIGNMENT
            northPanel.add(row)
        }

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
        if (toolbarState.regex && !SearchFilter.isValidRegex(searchField.text)) {
            searchField.border = LineBorder(JBColor.RED, 1)
        } else {
            searchField.border = defaultSearchBorder
        }
    }

    private fun currentSearchOptions() = SearchOptions(
        query = searchField.text,
        matchPath = toolbarState.matchPath,
        matchContent = toolbarState.matchContent,
        regex = toolbarState.regex,
        caseSensitive = toolbarState.caseSensitive
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

            val historyFiltered = if (options.query.isBlank()) {
                allEntries
            } else if (options.regex && !SearchFilter.isValidRegex(options.query)) {
                // Invalid regex — retain current list unchanged
                return@invokeLater
            } else {
                allEntries.filter { SearchFilter.matches(it, options) }
            }

            // Dispatch group assignment off-EDT; repaint when done.
            ApplicationManager.getApplication().executeOnPooledThread {
                FileGrouper.assignGroups(allEntries)
                ApplicationManager.getApplication().invokeLater {
                    list.repaint()
                }
            }

            // When global search is enabled and query is non-blank, fetch project files.
            if (toolbarState.isGlobalSearchEnabled() && options.query.isNotBlank()) {
                val historyUrls = allEntries.map { it.fileUrl }.toSet()
                val generation = globalScanGeneration.incrementAndGet()

                ApplicationManager.getApplication().executeOnPooledThread {
                    val projectResults = fetchProjectFiles(options.query, historyUrls)
                    if (generation != globalScanGeneration.get()) return@executeOnPooledThread

                    ApplicationManager.getApplication().invokeLater {
                        if (generation == globalScanGeneration.get()) {
                            applyToModel(historyFiltered, projectResults)
                        }
                    }
                }
            } else {
                // No global search — show only history.
                applyToModel(historyFiltered)
            }
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

    // ── Project file scan ─────────────────────────────────────────────────────────

    /**
     * Fetches project files whose names contain [query] (case-insensitive),
     * excluding files already in [historyUrls]. Capped at 50 results.
     * Must be called on a pooled thread; runs inside a [ReadAction].
     */
    private fun fetchProjectFiles(
        query: String,
        historyUrls: Set<String>
    ): List<EditTrailResult.ProjectFileResult> {
        return ReadAction.compute<List<EditTrailResult.ProjectFileResult>, Throwable> {
            val results = mutableListOf<EditTrailResult.ProjectFileResult>()
            val queryLower = query.lowercase()
            val basePath = project.basePath ?: ""

            ProjectRootManager.getInstance(project).fileIndex.iterateContent { vf ->
                if (results.size >= 50) return@iterateContent false
                if (vf.isDirectory) return@iterateContent true
                if (vf.url in historyUrls) return@iterateContent true
                if (vf.name.lowercase().contains(queryLower)) {
                    val rel = if (basePath.isNotEmpty() && vf.path.startsWith(basePath)) {
                        vf.path.substring(basePath.length).trimStart('/', '\\')
                    } else {
                        vf.path
                    }
                    results.add(
                        EditTrailResult.ProjectFileResult(
                            virtualFile = vf,
                            fileName = vf.name,
                            relativePath = rel,
                            fileType = FileTypeClassifier.classify(vf.name)
                        )
                    )
                }
                true
            }
            results
        }
    }

    // ── Model update ─────────────────────────────────────────────────────────────

    /**
     * Merges history and project-file results, rebuilds the chip bar, applies the
     * file-type chip filter, and populates the list model.
     */
    private fun applyToModel(
        historyResults: List<FileHistoryEntry>,
        projectResults: List<EditTrailResult.ProjectFileResult> = emptyList()
    ) {
        val mergedResults: List<EditTrailResult> =
            historyResults.map { EditTrailResult.HistoryResult(it) } + projectResults

        val counts: Map<String, Int> = mergedResults.groupingBy { result ->
            when (result) {
                is EditTrailResult.HistoryResult ->
                    FileTypeClassifier.classify(result.entry.fileName)
                is EditTrailResult.ProjectFileResult -> result.fileType
            }
        }.eachCount()

        val chips: List<FileTypeChip> = counts.entries
            .sortedByDescending { it.value }
            .map { (label, count) -> FileTypeChip(label, count, label in selectedFileTypes) }

        // The All-chip count tracks the total across all types in the merged set.
        lastVisibleCount = mergedResults.size

        rebuildChipBar(chips)

        val visible = if (selectedFileTypes.isEmpty()) {
            mergedResults
        } else {
            mergedResults.filter { result ->
                val fileType = when (result) {
                    is EditTrailResult.HistoryResult ->
                        FileTypeClassifier.classify(result.entry.fileName)
                    is EditTrailResult.ProjectFileResult -> result.fileType
                }
                fileType in selectedFileTypes
            }
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

        // The "All" chip has no natural file-type glyph, so it keeps full text.
        // It's a ChipButton with selected = (no type filters active).
        val allChip = ChipButton(
            text = FileTypeChipLabel.formatAll(lastVisibleCount),
            selected = selectedFileTypes.isEmpty(),
        )
        allChip.toolTipText = "All file types"
        allChip.addActionListener {
            selectedFileTypes.clear()
            refresh()
        }
        chipBarPanel.add(allChip)

        chips.forEach { chip ->
            val toggle = ChipButton(
                text = chip.count.toString(),
                icon = FileTypeChipIcon.iconFor(chip.label),
                selected = chip.selected,
            )
            toggle.toolTipText = FileTypeChipLabel.format(chip)
            toggle.addActionListener {
                if (toggle.isSelected) selectedFileTypes.add(chip.label)
                else selectedFileTypes.remove(chip.label)
                refresh()
            }
            chipBarPanel.add(toggle)
        }

        chipBarPanel.revalidate()
        chipBarPanel.repaint()
        // Bubble revalidation up so the north panel re-allocates the chip
        // bar's grown height after chips are added.
        chipBarPanel.parent?.revalidate()
    }

    // ── Actions ──────────────────────────────────────────────────────────────────

    private fun openSelected() {
        val result = list.selectedValue ?: return
        when (result) {
            is EditTrailResult.HistoryResult -> {
                val entry = result.entry
                val vf = VirtualFileManager.getInstance().findFileByUrl(entry.fileUrl)
                if (vf != null && vf.exists()) {
                    FileOpenNavigator.open(project, vf)
                } else {
                    entry.exists = false
                    refresh()
                }
            }
            is EditTrailResult.ProjectFileResult -> {
                val vf = result.virtualFile ?: return
                if (vf.exists()) {
                    FileOpenNavigator.open(project, vf)
                    project.service<EditTrailProjectService>().recordEdit(vf)
                }
            }
        }
    }

    private fun dispatchRecalculateGroups() {
        val entries = project.service<EditTrailProjectService>().getHistory(sortMode)
        ApplicationManager.getApplication().executeOnPooledThread {
            FileGrouper.assignGroups(entries)
            ApplicationManager.getApplication().invokeLater {
                list.repaint()
            }
        }
    }

    private fun promptAndClearHistory() {
        val choice = JOptionPane.showConfirmDialog(
            this,
            "Clear all EditTrail history for this project? This cannot be undone.",
            "Clear History",
            JOptionPane.YES_NO_OPTION
        )
        if (choice == JOptionPane.YES_OPTION) {
            project.service<EditTrailProjectService>().clearHistory()
        }
    }

    // ── Toolbar builders ─────────────────────────────────────────────────────────

    private fun createSortBar(): JPanel {
        val sortOptions = arrayOf("Last Edited", "Last Viewed")
        val combo = JComboBox(sortOptions)
        combo.selectedIndex = if (sortMode == SortMode.LAST_EDITED) 0 else 1
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

    /**
     * Builds the icon-only `ActionToolbar` row containing the four search-option
     * toggles, the global-search toggle, the recalculate-groups action, and the
     * clear-history action.
     */
    private fun createIconToolbar(): JComponent {
        val group = object : DefaultActionGroup() {
            override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
        }.apply {
            add(MatchPathToggleAction(toolbarState))
            add(MatchContentToggleAction(toolbarState))
            add(MatchPatternToggleAction(toolbarState))
            add(CaseSensitiveToggleAction(toolbarState))
            addSeparator()
            add(GlobalSearchToggleAction(toolbarState))
            addSeparator()
            add(RecalculateGroupsAction(toolbarState))
            add(ClearHistoryAction(toolbarState))
        }
        val toolbar = ActionManager.getInstance()
            .createActionToolbar(TOOLBAR_PLACE, group, /* horizontal = */ true)
        toolbar.targetComponent = this
        return toolbar.component
    }

    private fun createChipBarComponent(): JComponent {
        // Slightly stronger top divider visually separates the chip bar from
        // the search-toolbar actions above. Theme-aware so it survives both
        // Light and Darcula.
        val dividerColor = JBColor(
            java.awt.Color(0xC2C8D1),
            java.awt.Color(0x4A5058),
        )
        chipBarPanel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, dividerColor),
            BorderFactory.createEmptyBorder(3, 0, 0, 0),
        )
        return chipBarPanel
    }

    companion object {
        const val TOOLBAR_PLACE = "EditTrail.IconToolbar"
    }
}
