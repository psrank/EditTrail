## Why

EditTrail is now feature-complete for its core workflows, but it behaves identically for every developer and every project. There is no way to adjust how many files are retained, which sort mode starts selected, or what the search toggles default to. Developers who come back to a project also have no mechanism to clear stale history. This iteration adds a settings page, a few key configurable options, and a clear-history action to make the plugin feel production-ready and adaptable.

## What Changes

- **New**: `EditTrailAppSettings` application-level service stores max history size, default sort mode, and search option persistence preference
- **New**: `EditTrailConfigurable` exposes those settings as a form under IDE Settings → Tools → EditTrail
- **Modified**: `EditTrailProjectService` reads max history size from app settings when the repository enforces its limit; exposes `clearHistory()` to wipe all entries
- **Modified**: `EditTrailPanel` initialises sort mode and (optionally) search toggles from settings; adds a "Clear history" action with confirmation dialog
- **Modified**: `plugin.xml` registers the app-level service and the configurable extension

## Capabilities

### New Capabilities
- `settings-page`: IDE settings form for EditTrail, exposing configurable options and accessible under Tools → EditTrail.
- `clear-history`: Tool-window action that prompts for confirmation then wipes all history entries for the current project.

### Modified Capabilities
- `history-store`: Max history size is now read from `EditTrailAppSettings` so it can be changed at runtime without restarting the IDE.
- `tool-window`: Panel reads default sort from settings on startup; persists search-toggle state when the persistence preference is enabled; shows Clear History button.

## Impact

- `EditTrailAppSettings.kt` — new application-level service (serialised to `edittrail-settings.xml`)
- `EditTrailConfigurable.kt` — new `Configurable` implementation for IDE settings
- `EditTrailProjectService.kt` — new `clearHistory()` method; max size read from `EditTrailAppSettings`
- `EditTrailPanel.kt` — reads default sort; conditionally restores/saves search toggles; new Clear History button
- `plugin.xml` — new `applicationService` and `applicationConfigurable` registrations
- `FileHistoryState.kt` — no changes (search option persistence is app-level, not per-project)
