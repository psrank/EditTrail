# Design — Settings and Polish

## App Settings Service

`EditTrailAppSettings` is a `@Service(Service.Level.APP)` that implements `PersistentStateComponent<EditTrailAppSettings.State>`. Its state class holds:

| Field | Type | Default | Notes |
|---|---|---|---|
| `maxHistorySize` | `Int` | `500` | Upper limit for `FileHistoryRepository` |
| `defaultSortMode` | `String` | `"LAST_EDITED"` | Serialised as String to survive enum renames |
| `persistSearchOptions` | `Boolean` | `false` | Gate for saving/restoring toggle state |
| `matchPath` | `Boolean` | `false` | Saved toggle state |
| `matchContent` | `Boolean` | `false` | Saved toggle state |
| `regex` | `Boolean` | `false` | Saved toggle state |
| `caseSensitive` | `Boolean` | `false` | Saved toggle state |

State is serialised to `APP_CONFIG/edittrail-settings.xml` (standard IntelliJ app-level storage path).

Accessed as a singleton: `ApplicationManager.getApplication().service<EditTrailAppSettings>()`.

---

## Configurable

`EditTrailConfigurable` implements `com.intellij.openapi.options.Configurable`. It is registered as `applicationConfigurable` in `plugin.xml` under the parent `tools` group so it appears at **Settings → Tools → EditTrail**.

The form contains:
- `Max history entries` — `JSpinner` (range 50–10 000, step 50)
- `Default sort` — `JComboBox<String>` with items `["Last edited", "Last viewed"]`
- `Remember search options` — `JCheckBox`
- A note label explaining that search toggle state is saved per-app when the box is checked

`isModified()` compares current form values against `EditTrailAppSettings.state`.
`apply()` writes new values to `EditTrailAppSettings.state` and publishes `EditTrailTopics.HISTORY_UPDATED` so any open panels repaint with the new sort.
`reset()` reads values back from `EditTrailAppSettings.state`.

---

## Max History Size Wiring

`FileHistoryRepository` already accepts `maxSize` as a constructor parameter. `EditTrailProjectService` currently hard-codes `FileHistoryRepository()` (default 500).

Change: pass `EditTrailAppSettings.getInstance().state.maxHistorySize` at repository construction. When settings are applied and the new size is smaller than the current size, call `repository.evictToSize(newMaxSize)` (a new method that runs `evictIfNeeded` until entries ≤ limit).

Add `fun setMaxSize(newMax: Int)` to `FileHistoryRepository` so the configurable can resize at runtime without recreating the service.

---

## Default Sort Wiring

`EditTrailPanel` initialises `sortMode` from `EditTrailAppSettings`. When the configurable calls `apply()`, publish `HISTORY_UPDATED`; the panel re-reads the setting on its next `refresh()` call (or reset `sortMode` in the refresh path).

Since the user can also change sort in the combo box directly, the combo is still the source of truth during a session. The app setting only sets the initial value when the panel is first constructed.

---

## Search Option Persistence

When `EditTrailAppSettings.state.persistSearchOptions == true`:
- On panel init: restore `matchPath`, `matchContent`, `regex`, `caseSensitive` from app settings
- On each toggle change: save those four booleans back to app settings immediately

When `persistSearchOptions == false`: toggles always start unchecked (current behaviour).

`globalSearchEnabled` is already persisted per-project in `FileHistoryState` — no change.

---

## Clear History

A `JButton("Clear history")` is added to the sort bar row. On click:

1. Show `JOptionPane.showConfirmDialog` asking "Clear all EditTrail history for this project? This cannot be undone."
2. On YES: call `EditTrailProjectService.clearHistory()`, which clears the repository entries and publishes `HISTORY_UPDATED`.
3. On NO/CANCEL: do nothing.

`clearHistory()` is a one-liner in `EditTrailProjectService`:
```kotlin
fun clearHistory() {
    repository.clearAll()
    notifyHistoryChanged()
}
```

`FileHistoryRepository.clearAll()` simply calls `entries.clear()`.

---

## plugin.xml Changes

```xml
<!-- App-level settings service -->
<applicationService serviceImplementation="com.psrank.edittrail.EditTrailAppSettings"/>

<!-- Settings page under Tools → EditTrail -->
<applicationConfigurable
    parentId="tools"
    id="com.psrank.edittrail.EditTrailConfigurable"
    displayName="EditTrail"
    instance="com.psrank.edittrail.EditTrailConfigurable"/>
```
