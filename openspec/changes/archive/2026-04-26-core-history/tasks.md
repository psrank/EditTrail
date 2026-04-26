## 1. Project Setup

- [x] 1.1 Create the Gradle/IntelliJ plugin project structure (plugin.xml, build.gradle.kts, src/main/kotlin layout)
- [x] 1.2 Configure `plugin.xml` with plugin ID, name, vendor, and platform compatibility range
- [x] 1.3 Add IntelliJ Platform SDK dependency and verify the project builds cleanly

## 2. Data Model

- [x] 2.1 Create `FileHistoryEntry` data class with fields: `fileUrl`, `fileName`, `relativePath`, `lastViewedAt`, `lastEditedAt`, `viewCount`, `editCount`, `exists`
- [x] 2.2 Create `FileHistoryState` as the serialisable state container (list of `FileHistoryEntry`)

## 3. History Repository

- [x] 3.1 Create `FileHistoryRepository` with `recordView(fileUrl, fileName, relativePath)` and `recordEdit(…)` methods
- [x] 3.2 Implement deduplication: look up entry by `fileUrl`, update in place rather than inserting a duplicate
- [x] 3.3 Implement LRU eviction: when size exceeds 500, remove the entry with the oldest `max(lastViewedAt, lastEditedAt)`
- [x] 3.4 Implement `getHistory(sortMode: SortMode): List<FileHistoryEntry>` returning entries sorted by `lastEditedAt` desc (default) or `lastViewedAt` desc

## 4. Project Service

- [x] 4.1 Create `EditTrailProjectService` as an `@Service(Service.Level.PROJECT)` class
- [x] 4.2 Annotate with `@State` and implement `PersistentStateComponent<FileHistoryState>` for per-project persistence
- [x] 4.3 Inject `FileHistoryRepository` and expose `recordView(file: VirtualFile)` and `recordEdit(file: VirtualFile)` delegating to the repository
- [x] 4.4 Expose `getHistory(sortMode)` delegating to the repository

## 5. Editor Listeners

- [x] 5.1 Create `EditorSelectionListener` implementing `FileEditorManagerListener` to detect file open / tab-switch events; reject non-file editors
- [x] 5.2 Register `EditorSelectionListener` in `plugin.xml` as a project-level message bus listener for `FileEditorManagerListener.FILE_EDITOR_MANAGER`
- [x] 5.3 Create `DocumentEditListener` implementing `DocumentListener`; attach it to each document on first open via `VirtualFileListener` or `FileOpenedSyncListener`
- [x] 5.4 Implement 2-second debounce in `DocumentEditListener` using a `ScheduledExecutorService` keyed by file URL; cancel prior task on each new event
- [x] 5.5 Ensure both listeners call `EditTrailProjectService.recordView` / `recordEdit` and are disposed properly when the project closes

## 6. Tool Window

- [x] 6.1 Create `EditTrailToolWindowFactory` implementing `ToolWindowFactory`; register in `plugin.xml` as a project-level tool window named `EditTrail`
- [x] 6.2 Create `EditTrailPanel` (a `JPanel`) containing a `JBList<FileHistoryEntry>` backed by a `DefaultListModel`
- [x] 6.3 Implement a cell renderer that shows file icon (from file type), file name, and relative path
- [x] 6.4 Implement empty-state: when history is empty, show a placeholder label explaining what will appear there
- [x] 6.5 Wire double-click / Enter on a list item to open the file in the editor via `FileEditorManager.openFile`; handle missing files gracefully (mark unavailable, no crash)
- [x] 6.6 Add a sort toggle (toolbar button or combo) that switches between `LAST_EDITED` and `LAST_VIEWED` modes and refreshes the list

## 7. List Refresh

- [x] 7.1 After each `recordView` or `recordEdit` call, post a UI refresh to the `EditTrailPanel` on the EDT using `ApplicationManager.getApplication().invokeLater`
- [x] 7.2 Verify the list updates in real time as the user switches tabs and edits files

## 8. Persistence Verification

- [x] 8.1 Manually verify that history entries survive an IDE restart (write an integration smoke test or manual test checklist)
- [x] 8.2 Verify that two projects opened simultaneously have independent histories

## 9. Edge Cases and Cleanup

- [x] 9.1 On project open, resolve each persisted `fileUrl` via `VirtualFileManager.findFileByUrl`; set `exists = false` for entries that no longer resolve
- [x] 9.2 Filter out `exists = false` entries from the visible list (or display them with a distinct style — choose one and implement consistently)
- [x] 9.3 Ensure all listeners and timers are disposed on project close to prevent memory leaks
