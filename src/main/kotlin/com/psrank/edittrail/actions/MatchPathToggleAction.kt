package com.psrank.edittrail.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction

class MatchPathToggleAction(
    private val state: EditTrailToolbarState,
) : ToggleAction("Match path", DESCRIPTION, AllIcons.Nodes.Folder) {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun isSelected(e: AnActionEvent): Boolean = state.matchPath

    override fun setSelected(e: AnActionEvent, selected: Boolean) {
        state.matchPath = selected
        state.persistSearchOptionsIfEnabled()
        state.onSearchChange()
    }

    companion object {
        const val DESCRIPTION = "Match path — search across the relative file path"
    }
}
