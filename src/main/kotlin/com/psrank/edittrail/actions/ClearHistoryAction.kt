package com.psrank.edittrail.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ActionUpdateThread

class ClearHistoryAction(
    private val state: EditTrailToolbarState,
) : AnAction("Clear history", DESCRIPTION, AllIcons.General.Remove) {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        state.onClearHistory()
    }

    companion object {
        const val DESCRIPTION = "Clear history — remove all EditTrail entries for this project"
    }
}
