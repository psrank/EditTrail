## Why

EditTrail does not yet exist. This change creates the foundational plugin: a JetBrains tool window that tracks recently viewed and edited files per project, giving developers instant access to their working context without navigating the project tree.

## What Changes

- Introduce the `EditTrail` project-level tool window
- Add file-view tracking via editor selection listener
- Add file-edit tracking via document change listener
- Store one history entry per file, deduplicated and updated in place
- Support sorting by last-edited (default) and last-viewed
- Allow opening a file directly from the history list
- Persist history per project across IDE restarts
- Enforce a configurable maximum history size (default 500)
- Show an empty-state message when no history exists

## Capabilities

### New Capabilities

- `tool-window`: The EditTrail project-level tool window panel that renders the file history list and empty state
- `file-tracking`: Listening to editor selection and document change events to record viewed and edited files
- `history-store`: Persisting and managing a deduplicated, size-limited list of `FileHistoryEntry` records per project
- `history-sorting`: Sorting history entries by last-edited or last-viewed timestamp

### Modified Capabilities

<!-- None — this is the initial implementation. No existing specs exist. -->

## Impact

- New IntelliJ Platform plugin module (Kotlin)
- New project service: `EditTrailProjectService`, `FileHistoryState`, `FileHistoryRepository`
- New listeners: `EditorSelectionListener`, `DocumentEditListener`
- New UI: `EditTrailToolWindowFactory`
- No external API changes; no breaking changes
- Dependencies: IntelliJ Platform SDK (existing)
