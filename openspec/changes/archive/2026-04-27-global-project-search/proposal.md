## Why

The EditTrail history panel is limited to files the user has already visited. Developers often want to navigate to files that are not yet in their recent history — today they must leave the panel and use the IDE's own file switcher. Bringing project-wide file search into EditTrail makes it the single go-to navigation surface.

## What Changes

- Add an `Include all project files` toggle button to the EditTrail panel toolbar
- When enabled and a non-empty query is entered, search all project virtual files by name in addition to history entries
- History entries appear first in results; non-history project files appear below them
- Non-history results are rendered in grey italic with a `Project result` label
- Clicking a non-history result opens the file and inserts it into EditTrail history
- Global search results compose with the existing text search filter and file-type chip bar
- Project file scanning runs off the EDT to keep the UI responsive

## Capabilities

### New Capabilities
- `global-search`: Toggle, project-file scanning, result merging, priority ordering, and async refresh
- `result-renderer`: Visual distinction for non-history results (grey italic, `Project result` label)

### Modified Capabilities
- `tool-window`: Panel gains a new `Include all project files` toggle control
- `search-filter`: Search filter now operates over the merged result set when global search is enabled

## Impact

- `EditTrailPanel.kt` — new toggle button, async worker, merged model rebuild
- `FileHistoryCellRenderer.kt` — render `ProjectFileResult` rows with grey italic style
- New sealed class `EditTrailResult` (or file) for the unified result model
- Depends on IntelliJ Platform `FilenameIndex` / `ProjectRootManager` for project-wide file enumeration
- No persistence changes — project file results are ephemeral (not stored in `FileHistoryState`)
