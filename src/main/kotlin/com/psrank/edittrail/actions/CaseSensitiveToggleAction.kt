package com.psrank.edittrail.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction

class CaseSensitiveToggleAction(
    private val state: EditTrailToolbarState,
) : ToggleAction("Case sensitive", DESCRIPTION, AllIcons.Actions.MatchCase) {

    override fun isSelected(e: AnActionEvent): Boolean = state.caseSensitive

    override fun setSelected(e: AnActionEvent, selected: Boolean) {
        state.caseSensitive = selected
        state.persistSearchOptionsIfEnabled()
        state.onSearchChange()
    }

    companion object {
        const val DESCRIPTION = "Case sensitive — match query letter case exactly"
    }
}
