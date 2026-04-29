package com.psrank.edittrail.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction

class GlobalSearchToggleAction(
    private val state: EditTrailToolbarState,
) : ToggleAction("Include all project files", DESCRIPTION, AllIcons.Nodes.Project) {

    override fun isSelected(e: AnActionEvent): Boolean = state.isGlobalSearchEnabled()

    override fun setSelected(e: AnActionEvent, selected: Boolean) {
        state.setGlobalSearchEnabled(selected)
        state.onSearchChange()
    }

    companion object {
        const val DESCRIPTION = "Include all project files in search results"
    }
}
