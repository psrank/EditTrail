package com.psrank.edittrail

import com.intellij.openapi.vfs.VirtualFile

/**
 * Unified result type for the EditTrail list.
 *
 * - [HistoryResult] wraps a file that is already in the edit/view history.
 * - [ProjectFileResult] represents a project file that matched a global search
 *   query but has not yet been visited.
 */
sealed class EditTrailResult {

    data class HistoryResult(val entry: FileHistoryEntry) : EditTrailResult()

    data class ProjectFileResult(
        val virtualFile: VirtualFile? = null,
        val fileName: String,
        val relativePath: String,
        val fileType: String
    ) : EditTrailResult()
}
