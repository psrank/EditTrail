package com.psrank.edittrail

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*

/**
 * IDE Settings → Tools → EditTrail settings form.
 * Registered as <applicationConfigurable parentId="tools"> in plugin.xml.
 */
class EditTrailConfigurable : Configurable {

    private var maxSizeSpinner: JSpinner? = null
    private var sortCombo: JComboBox<String>? = null
    private var persistSearchBox: JCheckBox? = null
    private var panel: JPanel? = null

    override fun getDisplayName(): String = "EditTrail"

    override fun createComponent(): JComponent {
        val spinner = JSpinner(SpinnerNumberModel(500, 50, 10000, 50))
        val combo = JComboBox(arrayOf("Last edited", "Last viewed"))
        val checkbox = JCheckBox("Remember search options")

        maxSizeSpinner = spinner
        sortCombo = combo
        persistSearchBox = checkbox

        val p = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            insets = Insets(4, 4, 4, 4)
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
        }

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; p.add(JLabel("Max history entries:"), gbc)
        gbc.gridx = 1; p.add(spinner, gbc)
        gbc.gridx = 0; gbc.gridy = 1; p.add(JLabel("Default sort:"), gbc)
        gbc.gridx = 1; p.add(combo, gbc)
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; p.add(checkbox, gbc)

        panel = p
        reset()
        return p
    }

    override fun isModified(): Boolean {
        val s = EditTrailAppSettings.getInstance().state
        val formSort = if ((sortCombo?.selectedIndex ?: 0) == 0) "LAST_EDITED" else "LAST_VIEWED"
        return (maxSizeSpinner?.value as? Int) != s.maxHistorySize ||
                formSort != s.defaultSortMode ||
                persistSearchBox?.isSelected != s.persistSearchOptions
    }

    override fun apply() {
        val state = EditTrailAppSettings.getInstance().state
        val newMax = (maxSizeSpinner?.value as? Int) ?: state.maxHistorySize
        state.maxHistorySize = newMax
        state.defaultSortMode = if ((sortCombo?.selectedIndex ?: 0) == 0) "LAST_EDITED" else "LAST_VIEWED"
        state.persistSearchOptions = persistSearchBox?.isSelected ?: state.persistSearchOptions

        // Push new max size to all open projects immediately.
        ProjectManager.getInstance().openProjects.forEach { project ->
            if (!project.isDisposed) {
                project.service<EditTrailProjectService>().setMaxSize(newMax)
            }
        }

        // Notify open panels so they can re-read the default sort on next refresh.
        ApplicationManager.getApplication().messageBus
            .syncPublisher(EditTrailTopics.HISTORY_UPDATED)
            .historyUpdated()
    }

    override fun reset() {
        val s = EditTrailAppSettings.getInstance().state
        maxSizeSpinner?.value = s.maxHistorySize
        sortCombo?.selectedIndex = if (s.defaultSortMode == "LAST_EDITED") 0 else 1
        persistSearchBox?.isSelected = s.persistSearchOptions
    }

    override fun disposeUIResources() {
        panel = null
        maxSizeSpinner = null
        sortCombo = null
        persistSearchBox = null
    }
}
