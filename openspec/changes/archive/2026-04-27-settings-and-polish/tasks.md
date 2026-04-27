## 1. App settings service

- [ ] 1.1 Create `EditTrailAppSettings.kt` — `@Service(Service.Level.APP)` implementing `PersistentStateComponent<EditTrailAppSettings.State>`; state holds `maxHistorySize: Int = 500`, `defaultSortMode: String = "LAST_EDITED"`, `persistSearchOptions: Boolean = false`, `matchPath: Boolean = false`, `matchContent: Boolean = false`, `regex: Boolean = false`, `caseSensitive: Boolean = false`; storage path `edittrail-settings.xml`; expose `companion object { fun getInstance(): EditTrailAppSettings }`
- [ ] 1.2 Register `EditTrailAppSettings` as `<applicationService>` in `plugin.xml`

## 2. Configurable (settings UI)

- [ ] 2.1 Create `EditTrailConfigurable.kt` implementing `com.intellij.openapi.options.Configurable` with display name `"EditTrail"`
- [ ] 2.2 Build the settings form: `JSpinner` for max history size (range 50–10 000, step 50), `JComboBox<String>` for default sort (`"Last edited"` / `"Last viewed"`), `JCheckBox` for "Remember search options"
- [ ] 2.3 Implement `isModified()` comparing form values against `EditTrailAppSettings.getInstance().state`
- [ ] 2.4 Implement `apply()` — write form values to `EditTrailAppSettings.getInstance().state` and call `ApplicationManager.getApplication().messageBus.syncPublisher(EditTrailTopics.HISTORY_UPDATED).historyUpdated()` so open panels repaint
- [ ] 2.5 Implement `reset()` — reload form from current `EditTrailAppSettings.getInstance().state`
- [ ] 2.6 Register `EditTrailConfigurable` as `<applicationConfigurable parentId="tools">` in `plugin.xml`

## 3. Max history size wiring

- [ ] 3.1 Add `fun setMaxSize(newMax: Int)` to `FileHistoryRepository` — updates the `maxSize` field and immediately calls `evictIfNeeded()` to trim entries if the new limit is smaller
- [ ] 3.2 Change `EditTrailProjectService` to initialise `FileHistoryRepository` with `EditTrailAppSettings.getInstance().state.maxHistorySize` instead of the hard-coded default
- [ ] 3.3 In `EditTrailConfigurable.apply()`, after saving state, retrieve each open project's `EditTrailProjectService` via `ProjectManager.getInstance().openProjects` and call `service.setMaxSize(newMax)` on each

## 4. Default sort wiring

- [ ] 4.1 In `EditTrailPanel`, initialise `sortMode` from `EditTrailAppSettings.getInstance().state.defaultSortMode` (parse to `SortMode` enum, fallback to `LAST_EDITED`)
- [ ] 4.2 Set the sort combo box to reflect the initial sort mode from settings

## 5. Search option persistence

- [ ] 5.1 In `EditTrailPanel.init`, after wiring the global-search toggle, check `EditTrailAppSettings.getInstance().state.persistSearchOptions`; if true, set `matchPathBox`, `matchContentBox`, `regexBox`, `caseSensitiveBox` from stored state
- [ ] 5.2 In `EditTrailPanel.onSearchChange()`, if `persistSearchOptions` is true, write the four toggle values back to `EditTrailAppSettings.getInstance().state`

## 6. Clear history

- [ ] 6.1 Add `fun clearAll()` to `FileHistoryRepository` — calls `entries.clear()`
- [ ] 6.2 Add `fun clearHistory()` to `EditTrailProjectService` — calls `repository.clearAll()` then `notifyHistoryChanged()`
- [ ] 6.3 Add a `JButton("Clear history")` to `createSortBar()` in `EditTrailPanel`; on click, show `JOptionPane.showConfirmDialog` with message "Clear all EditTrail history for this project? This cannot be undone."; on YES call `project.service<EditTrailProjectService>().clearHistory()`
