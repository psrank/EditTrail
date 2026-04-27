package com.psrank.edittrail

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.vcsUtil.VcsUtil

/**
 * Project-level service that owns the EditTrail history repository and persists it
 * across IDE restarts using IntelliJ's PersistentStateComponent mechanism.
 *
 * Persisted to `.idea/edittrail.xml` (per-project, excluded from VCS by default).
 */
@State(
    name = "EditTrailHistory",
    storages = [Storage("edittrail.xml")]
)
@Service(Service.Level.PROJECT)
class EditTrailProjectService(private val project: Project) :
    PersistentStateComponent<FileHistoryState> {

    private val repository = FileHistoryRepository()
    private var myState = FileHistoryState()

    // ── PersistentStateComponent ────────────────────────────────────────────────

    override fun getState(): FileHistoryState {
        myState.entries = repository.getAllEntries().toMutableList()
        return myState
    }

    override fun loadState(state: FileHistoryState) {
        myState = state
        repository.loadEntries(state.entries)
        // Validate which files still exist on disk after loading persisted state.
        resolveExistingFiles()
    }

    // ── Public API ──────────────────────────────────────────────────────────────

    /**
     * Records that [file] was viewed (opened or selected in the editor).
     * Ignored for files whose URL is blank.
     */
    fun recordView(file: VirtualFile) {
        val relativePath = relativePath(file)
        repository.recordView(file.url, file.name, relativePath)
        notifyHistoryChanged()
    }

    /**
     * Records that [file] was edited.
     * Ignored for files whose URL is blank.
     */
    fun recordEdit(file: VirtualFile) {
        val relativePath = relativePath(file)
        repository.recordEdit(file.url, file.name, relativePath)
        notifyHistoryChanged()
    }

    /**
     * Returns history entries sorted by [sortMode].
     */
    fun getHistory(sortMode: SortMode): List<FileHistoryEntry> =
        repository.getHistory(sortMode)

    /** Returns the persisted global-search-enabled flag. */
    fun isGlobalSearchEnabled(): Boolean = myState.globalSearchEnabled

    /** Persists the global-search-enabled flag. */
    fun setGlobalSearchEnabled(enabled: Boolean) {
        myState.globalSearchEnabled = enabled
    }

    /**
     * Resolves each persisted entry against the file system.
     * Sets [FileHistoryEntry.exists] = false for entries that no longer resolve.
     * Called automatically during [loadState] and can be called on project open.
     */
    fun resolveExistingFiles() {
        repository.getAllEntries().forEach { entry ->
            val vf = VirtualFileManager.getInstance().findFileByUrl(entry.fileUrl)
            entry.exists = vf != null && vf.exists()
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private fun relativePath(file: VirtualFile): String {
        val basePath = project.basePath ?: return file.path
        return try {
            file.path.removePrefix(basePath).trimStart('/', '\\')
        } catch (_: Exception) {
            file.path
        }
    }

    private fun notifyHistoryChanged() {
        project.messageBus.syncPublisher(EditTrailTopics.HISTORY_UPDATED).historyUpdated()
    }
}
