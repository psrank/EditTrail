package com.psrank.edittrail

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * Application-level service that stores EditTrail user preferences.
 * Persisted to APP_CONFIG/edittrail-settings.xml.
 */
@State(name = "EditTrailSettings", storages = [Storage("edittrail-settings.xml")])
@Service(Service.Level.APP)
class EditTrailAppSettings : PersistentStateComponent<EditTrailAppSettings.State> {

    /**
     * Serialisable state container for EditTrail application settings.
     * All fields must have default values for XmlSerializer.
     */
    class State {
        var maxHistorySize: Int = 500
        var defaultSortMode: String = "LAST_EDITED"
        var persistSearchOptions: Boolean = false
        var matchPath: Boolean = false
        var matchContent: Boolean = false
        var regex: Boolean = false
        var caseSensitive: Boolean = false
    }

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        fun getInstance(): EditTrailAppSettings =
            ApplicationManager.getApplication().getService(EditTrailAppSettings::class.java)
    }
}
