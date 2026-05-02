package com.psrank.edittrail.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction

class MatchPatternToggleAction(
    private val state: EditTrailToolbarState,
) : ToggleAction("Match pattern", DESCRIPTION, AllIcons.Actions.Regex) {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun isSelected(e: AnActionEvent): Boolean = state.regex

    override fun setSelected(e: AnActionEvent, selected: Boolean) {
        state.regex = selected
        state.persistSearchOptionsIfEnabled()
        state.onSearchChange()
    }

    companion object {
        const val DESCRIPTION = "Match pattern — interpret query as a regular expression"
    }
}
