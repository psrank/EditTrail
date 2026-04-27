## Why

The EditTrail history list currently shows all files in a flat, unsorted-by-relationship view. Developers who work on a feature often open the same cluster of files together, but there is no visual signal to help them recognise these clusters. Introducing usage-based visual grouping — a coloured left-border indicator — lets developers instantly see which files belong to the same working context without requiring manual tagging.

## What Changes

- **New**: `FileGrouper` service assigns group IDs to `FileHistoryEntry` objects based on co-occurrence heuristics (time proximity, directory proximity, extension similarity)
- **New**: Group colour palette assigns a stable per-session colour to each group ID
- **Modified**: `FileHistoryCellRenderer` renders a coloured 4 px vertical bar on the left edge for entries that have a group ID
- **Modified**: `EditTrailPanel` triggers background group recalculation on history refresh and exposes a manual "Recalculate groups" action
- **Modified**: `FileHistoryEntry` gains an optional `groupId: Int?` field (not persisted; recalculated each session)
- **Safe fallback**: if grouping fails, history renders normally with no indicators

## Capabilities

### New Capabilities
- `file-grouper`: Usage-based heuristic grouping engine that assigns group IDs to history entries. Covers scoring, thresholding, and connected-component group assignment.
- `group-renderer`: Visual rendering of group indicators (coloured vertical bar) in the cell renderer and stable session-scoped colour palette.

### Modified Capabilities
- `tool-window`: Panel gains a background grouping trigger on refresh and a "Recalculate groups" menu/button action.

## Impact

- `FileHistoryEntry.kt` — add nullable `groupId: Int?` (transient, not serialised)
- `FileGrouper.kt` — new file: scoring + grouping algorithm
- `FileHistoryCellRenderer.kt` — paint coloured left bar when `groupId != null`
- `EditTrailPanel.kt` — trigger `FileGrouper` off EDT after history load; expose recalculate action
- No changes to persistence schema (`FileHistoryState`) — `groupId` is session-only
- No changes to public API surface
