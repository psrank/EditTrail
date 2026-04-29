package com.psrank.edittrail.actions

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.psrank.edittrail.EditTrailAppSettings
import com.psrank.edittrail.EditTrailProjectService

/**
 * Shared state holder for the EditTrail icon toolbar.
 *
 * Each [com.intellij.openapi.actionSystem.ToggleAction] / [com.intellij.openapi.actionSystem.AnAction]
 * on the toolbar is constructed with a reference to a single [EditTrailToolbarState],
 * so the panel and every toolbar action read and write the same source of truth.
 *
 * The four search-option booleans are seeded from [EditTrailAppSettings] when
 * the persistence opt-in is on, matching the behaviour the panel had with its
 * old `JCheckBox` controls.
 */
class EditTrailToolbarState(
    val project: Project,
    val onSearchChange: () -> Unit,
    val onClearHistory: () -> Unit,
    val onRecalculateGroups: () -> Unit,
) {
    var matchPath: Boolean = false
    var matchContent: Boolean = false
    var regex: Boolean = false
    var caseSensitive: Boolean = false

    init {
        val settings = try { EditTrailAppSettings.getInstance().state } catch (_: Throwable) { null }
        if (settings?.persistSearchOptions == true) {
            matchPath = settings.matchPath
            matchContent = settings.matchContent
            regex = settings.regex
            caseSensitive = settings.caseSensitive
        }
    }

    fun isGlobalSearchEnabled(): Boolean =
        try { project.service<EditTrailProjectService>().isGlobalSearchEnabled() }
        catch (_: Throwable) { false }

    fun setGlobalSearchEnabled(value: Boolean) {
        try { project.service<EditTrailProjectService>().setGlobalSearchEnabled(value) }
        catch (_: Throwable) { /* outside IntelliJ context */ }
    }

    fun persistSearchOptionsIfEnabled() {
        try {
            val s = EditTrailAppSettings.getInstance().state
            if (s.persistSearchOptions) {
                s.matchPath = matchPath
                s.matchContent = matchContent
                s.regex = regex
                s.caseSensitive = caseSensitive
            }
        } catch (_: Throwable) { /* outside IntelliJ context */ }
    }
}
