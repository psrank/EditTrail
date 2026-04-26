package com.psrank.edittrail

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Registered in plugin.xml as the factory for the `EditTrail` tool window.
 * Creates an [EditTrailPanel] and wraps it in a tool window content tab.
 */
class EditTrailToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = EditTrailPanel(project, toolWindow)
        val content = ContentFactory.getInstance()
            .createContent(panel, /* displayName = */ "", /* isLockable = */ false)
        toolWindow.contentManager.addContent(content)
    }
}
