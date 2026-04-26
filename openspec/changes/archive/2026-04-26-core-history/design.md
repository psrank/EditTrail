## Context

EditTrail is a new JetBrains IDE plugin (IntelliJ IDEA, Rider) that does not yet exist. This design covers the initial implementation: the data model, project services, platform listener wiring, UI component tree, and persistence layer needed to track recently viewed and edited files and display them in a tool window.

The plugin targets IntelliJ Platform SDK (Kotlin). No existing codebase or state to migrate from.

## Goals / Non-Goals

**Goals:**

- Define the data model (`FileHistoryEntry`) and storage strategy
- Wire IntelliJ Platform listeners for editor selection and document changes
- Implement a project service that owns history state and enforces the size limit
- Render the history list in a tool window with sort support
- Persist history per project using `PersistentStateComponent`
- Handle missing files gracefully in the list

**Non-Goals:**

- Search / filtering (future iteration)
- File-type filter chips (future iteration)
- Global project file search (future iteration)
- Visual grouping or clustering (future iteration)
- Plugin settings UI (future iteration)
- Multi-module project distinction (all files in a project share one history for now)

## Decisions

### Decision 1: Use `PersistentStateComponent` for persistence

**Chosen**: Store `FileHistoryState` as an `@State`-annotated project service using IntelliJ's built-in XML serialization via `PersistentStateComponent<FileHistoryState>`.

**Rationale**: This is the idiomatic IntelliJ Platform approach. It gives per-project isolation automatically (state stored under `.idea/`), survives IDE restarts, and requires no external database or file I/O code.

**Alternative considered**: Write JSON to a custom `.idea/edittrail.json` file manually. Rejected — more boilerplate, must handle file I/O errors, no benefit over the platform pattern.

---

### Decision 2: Single `EditTrailProjectService` owns all state

**Chosen**: One project-level service (`EditTrailProjectService`) holds the `FileHistoryRepository` and exposes `recordView(file)` / `recordEdit(file)` methods. Listeners call only this service.

**Rationale**: Centralising state avoids race conditions between the two listeners. The repository performs deduplication and LRU eviction. The service can be retrieved via `project.service<EditTrailProjectService>()` from any listener.

---

### Decision 3: `VirtualFile.url` as the stable key

**Chosen**: Persist each history entry by `VirtualFile.url` (e.g. `file:///path/to/File.kt`). Use `VirtualFileManager.findFileByUrl()` at render time to resolve back to a live `VirtualFile`.

**Rationale**: Paths can change on rename/move; URLs are stable within a session. At load time if the URL no longer resolves, the entry is marked `exists = false` and can be hidden or shown with a stale indicator.

**Alternative considered**: Persist by `relativePath` from project root. Rejected — breaks if files are outside the project root or on different drives.

---

### Decision 4: LRU eviction with a 500-entry cap

**Chosen**: When a new entry pushes total count above the configured max (default 500), remove the entry with the oldest `max(lastViewedAt, lastEditedAt)`.

**Rationale**: Simple and predictable. The cap prevents unbounded growth without requiring user action.

---

### Decision 5: Debounce document change events

**Chosen**: Coalesce `DocumentListener.documentChanged` events within a 2-second window per file before recording an edit.

**Rationale**: Typing a single word fires many events per second. Debouncing keeps tracking lightweight and avoids hammering the project service on every keystroke.

**Alternative considered**: Record on every `documentChanged`. Rejected — too noisy; degrades IDE performance during fast typing.

---

### Decision 6: Render using a Swing `JBList` with a custom cell renderer

**Chosen**: Use a `JBList<FileHistoryEntry>` with a `DefaultListModel` updated on the EDT. A cell renderer shows the file name, relative path, and an icon derived from the file type.

**Rationale**: Standard IntelliJ UI for simple lists. Avoids complexity of a full `TableView` for a MVP list.

## Risks / Trade-offs

- **File rename/move during a session** → The `VirtualFile.url` may change. Mitigation: listen for `VirtualFileListener.fileMoved` / `filePropertyChanged` (name) events to update persisted URLs. Low priority for iteration 1; stale entries will be marked unavailable at next load.
- **Large projects with many open files** → History grows toward the 500 cap quickly for power users. Mitigation: cap is configurable in a later settings iteration.
- **EDT contention** → List updates must run on the EDT; listener callbacks may arrive on background threads. Mitigation: use `ApplicationManager.getApplication().invokeLater` for all UI refreshes.
- **Debounce timer leak** → A `Timer` per file could accumulate if many files are edited simultaneously. Mitigation: use a single shared `ScheduledExecutorService` keyed by file URL, cancelling the prior task on each new event.
