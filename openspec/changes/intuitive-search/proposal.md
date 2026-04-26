## Why

The EditTrail history list currently shows all recent files with no way to filter them. As the history grows, finding a specific file requires scrolling through the full list. A search interface makes the tool usable in large projects with deep history.

## What Changes

- A search text field is added above the history list in `EditTrailPanel`
- A row of toggle checkboxes (`Match path`, `Match content`, `Regex`, `Case sensitive`) is added below the search field
- The list is filtered dynamically as the user types (debounced)
- Fuzzy matching is applied to file names by default
- Path matching, content matching, regex mode, and case-sensitive mode are opt-in
- Invalid regex input is surfaced as a non-blocking UI state (no crash)
- Content search runs off the EDT to avoid blocking the IDE

## Capabilities

### New Capabilities

- `search-filter`: Provides the search input, toggle options, filter logic, and debounce mechanism for the EditTrail history list

### Modified Capabilities

- `tool-window`: The panel layout changes to include the search bar — the existing `EditTrailPanel` gains a search header component

## Impact

- `EditTrailPanel.kt` — gains search field, toggles, and list filtering logic
- New `SearchOptions` data class (query, matchPath, matchContent, regex, caseSensitive)
- New `SearchFilter` or inline filter function used by `EditTrailPanel` to produce the visible list subset
- Content search requires read access to `VirtualFile` contents — off-EDT coroutine or background thread
- No persistence changes; search state is ephemeral (not saved across IDE restarts)
- No changes to `FileHistoryRepository`, `EditTrailProjectService`, or listeners
