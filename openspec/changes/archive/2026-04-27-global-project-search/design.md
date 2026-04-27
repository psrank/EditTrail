## Context

EditTrail currently shows only files that have been opened/edited during the current and past sessions (stored in `FileHistoryState` via `FileHistoryRepository`). Navigation is restricted to that history. The panel already has a text search filter (`SearchFilter`) and a file-type chip bar; a global project search toggle extends the same surface to cover all project files without replacing the existing history-first experience.

IntelliJ Platform provides `FilenameIndex.getVirtualFilesByName` and `ProjectFileIndex.iterateContent` for enumerating project files. These APIs must be called from a read action, but not on the EDT for large projects.

## Goals / Non-Goals

**Goals:**
- Single `Include all project files` toggle in the panel toolbar
- Non-empty query triggers a background project-file scan (read action off EDT)
- Merged result list: history entries first, then non-history project files
- Non-history rows rendered with grey italic + `Project result` label
- Opening a non-history row opens the file and inserts a `FileHistoryEntry` into the repository
- Compose correctly with text search filter and file-type chip bar (both apply to merged results)
- Empty query hides all project-file results (show only history)

**Non-Goals:**
- Full-text / content search inside files
- Custom indexing or search ranking beyond history priority
- External search engines
- Persisting project-file results in `FileHistoryState`

## Decisions

### D1 — Sealed result model (`EditTrailResult`)
**Decision:** Introduce `sealed class EditTrailResult` with two subtypes: `HistoryResult(entry: FileHistoryEntry)` and `ProjectFileResult(virtualFile: VirtualFile, fileType: String)`. The list model holds `List<EditTrailResult>` instead of `List<FileHistoryEntry>`.

**Rationale:** A sealed type keeps renderer and click handler exhaustive. Attempting to bolt non-history items onto `FileHistoryEntry` would require nullable fields and boolean flags, which is fragile.

**Alternative considered:** Wrapping both in a single class with a `isHistoryResult: Boolean` flag — rejected because it breaks exhaustiveness checking and leaks implementation detail.

### D2 — Background scan via `ApplicationManager.getApplication().executeOnPooledThread`
**Decision:** When the toggle is on and the query changes, dispatch a pooled-thread read action (`ReadAction.compute`) to enumerate project files via `FilenameIndex.processAllFileNames` filtered by query. Post results back on the EDT via `SwingUtilities.invokeLater`.

**Rationale:** `FilenameIndex` access requires a read lock but must not block the EDT. Pooled threads are the lightest IntelliJ-idiomatic mechanism that avoids full `Task.Backgroundable` overhead for a simple enumeration.

**Alternative considered:** `Task.Backgroundable` with progress indicator — overkill for fast in-memory index lookups; adds modal progress UI the user does not need.

### D3 — Debounce global scan (300 ms)
**Decision:** Use a `javax.swing.Timer` (or a `ScheduledExecutorService`) to debounce the scan by 300 ms after the query changes, cancelling any pending scan before starting a new one.

**Rationale:** Prevents flooding the thread pool on every keystroke. History search remains synchronous (fast); only project-file scan is debounced.

### D4 — Cell renderer handles both result types
**Decision:** Extend `FileHistoryCellRenderer` to accept `EditTrailResult`. `HistoryResult` renders as before; `ProjectFileResult` renders filename in grey italic and appends a small `Project result` tag.

**Rationale:** Reusing the same renderer avoids duplication of layout logic. The renderer is already stateless and purely presentational.

### D5 — Open non-history file adds to history
**Decision:** In the mouse/keyboard handler, if the selected item is `ProjectFileResult`, open the `VirtualFile` via `FileEditorManager.openFile`, then call `repository.recordEdit(virtualFile)` (or equivalent) to insert it into history.

**Rationale:** Matches the completion criteria and makes the experience consistent — after opening a project file, it immediately becomes part of the user's history trail.

## Risks / Trade-offs

- [Risk] Large projects with thousands of files could make `FilenameIndex` enumeration slow → Mitigation: debounce + pooled thread; consider capping results at 50 non-history entries in the first implementation.
- [Risk] `FilenameIndex.processAllFileNames` may not be available in all IDE versions → Mitigation: use `FilenameIndex.getAllFilesByExt` with a fallback or iterate `ProjectRootManager.getInstance(project).contentRoots` recursively.
- [Risk] EDT assertion violations if read action is not properly wrapped → Mitigation: always wrap FilenameIndex calls in `ReadAction.compute`.
- [Risk] The list model type change (`EditTrailResult` vs `FileHistoryEntry`) is a breaking refactor of `EditTrailPanel` → Mitigation: all callers are internal to the plugin; no public API surface.
