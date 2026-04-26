package com.psrank.edittrail

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Runs after project open to resolve persisted file URLs against the file system.
 * Any entry pointing to a file that no longer exists is marked [FileHistoryEntry.exists] = false
 * and filtered from the visible list in [EditTrailPanel].
 *
 * Registered in plugin.xml as a <postStartupActivity>.
 */
class EditTrailStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        project.service<EditTrailProjectService>().resolveExistingFiles()
    }
}
