package com.psrank.edittrail.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction

class MatchContentToggleAction(
    private val state: EditTrailToolbarState,
) : ToggleAction("Match content", DESCRIPTION, AllIcons.Actions.Find) {

    override fun isSelected(e: AnActionEvent): Boolean = state.matchContent

    override fun setSelected(e: AnActionEvent, selected: Boolean) {
        state.matchContent = selected
        state.persistSearchOptionsIfEnabled()
        state.onSearchChange()
    }

    companion object {
        const val DESCRIPTION = "Match content — search inside file contents"
    }
}
