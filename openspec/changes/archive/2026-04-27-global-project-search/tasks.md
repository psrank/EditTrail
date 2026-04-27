## 1. Result model

- [x] 1.1 Create `EditTrailResult.kt` with a sealed class containing `HistoryResult(entry: FileHistoryEntry)` and `ProjectFileResult(virtualFile: VirtualFile, fileName: String, relativePath: String, fileType: String)` subtypes

## 2. Cell renderer

- [x] 2.1 Refactor `FileHistoryCellRenderer` to accept `EditTrailResult` instead of `FileHistoryEntry` in `getListCellRendererComponent`
- [x] 2.2 For `HistoryResult`, render exactly as before (no visual change)
- [x] 2.3 For `ProjectFileResult`, render file name in grey italic and display a `Project result` secondary label

## 3. Panel model refactor

- [x] 3.1 Change `DefaultListModel<FileHistoryEntry>` to `DefaultListModel<EditTrailResult>` in `EditTrailPanel`
- [x] 3.2 Update list type parameter and all references in `EditTrailPanel` accordingly
- [x] 3.3 Update `applyToModel` signature to `applyToModel(historyResults: List<FileHistoryEntry>, projectResults: List<ProjectFileResult> = emptyList())`; wrap history entries as `HistoryResult` before populating the model

## 4. Global search toggle

- [x] 4.1 Add `globalSearchEnabled: Boolean` field to `FileHistoryState` (defaults to `false`) for persistence
- [x] 4.2 Add `globalSearchBox = JCheckBox("Include all project files")` to the toggle bar in `EditTrailPanel`
- [x] 4.3 On panel init, load toggle state from `FileHistoryState`; on toggle change, save state and call `onSearchChange()`

## 5. Project file scanner

- [x] 5.1 Add a dedicated debounce `Timer` (300 ms) and a `searchGeneration`-style counter for the global scan in `EditTrailPanel`
- [x] 5.2 Implement a private `fetchProjectFiles(query: String, historyUrls: Set<String>): List<ProjectFileResult>` function that runs inside a `ReadAction.compute` block: enumerate project virtual files matching the query (by file name, case-insensitive), exclude files already in `historyUrls`, cap at 50 results, classify each file type via `FileTypeClassifier`
- [x] 5.3 In `refresh()`, when global search is enabled and query is non-blank, dispatch `fetchProjectFiles` on a pooled thread and post merged results back via `invokeLater`
- [x] 5.4 When global search is disabled or query is empty, show only history results (no pooled dispatch)

## 6. Result merging and file-type composition

- [x] 6.1 In `applyToModel`, build the merged result list: history entries first (as `HistoryResult`), then project file results (as `ProjectFileResult`)
- [x] 6.2 Apply file-type chip filter to both result types: for `HistoryResult` use `FileTypeClassifier.classify(entry.fileName)`; for `ProjectFileResult` use its `fileType` field
- [x] 6.3 Compute per-type counts across the full merged and search-filtered set before applying chip filter

## 7. Open action

- [x] 7.1 Update `openSelected()` to handle `EditTrailResult` via exhaustive `when` expression
- [x] 7.2 For `HistoryResult`, open the file as before
- [x] 7.3 For `ProjectFileResult`, open the `VirtualFile` via `FileEditorManager.openFile`, then insert a new `FileHistoryEntry` into history via `EditTrailProjectService`, then call `refresh()`
