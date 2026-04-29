package com.psrank.edittrail.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class RecalculateGroupsAction(
    private val state: EditTrailToolbarState,
) : AnAction("Recalculate groups", DESCRIPTION, AllIcons.Actions.Refresh) {

    override fun actionPerformed(e: AnActionEvent) {
        state.onRecalculateGroups()
    }

    companion object {
        const val DESCRIPTION = "Recalculate file groups based on co-occurrence"
    }
}
