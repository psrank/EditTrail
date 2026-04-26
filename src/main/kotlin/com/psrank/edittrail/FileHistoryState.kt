package com.psrank.edittrail

/**
 * Serialisable state container for EditTrail's per-project file history.
 * Used by [EditTrailProjectService] as the type parameter of PersistentStateComponent.
 *
 * Mutable list is required so that IntelliJ's XmlSerializer can populate it during
 * deserialization.
 */
class FileHistoryState {
    var entries: MutableList<FileHistoryEntry> = mutableListOf()
}
