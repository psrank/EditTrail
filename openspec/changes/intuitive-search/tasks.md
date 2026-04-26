## 1. Data Model

- [x] 1.1 Create `SearchOptions` data class with fields: `query`, `matchPath`, `matchContent`, `regex`, `caseSensitive`

## 2. Filter Logic

- [x] 2.1 Create `SearchFilter` object with `matches(entry: FileHistoryEntry, options: SearchOptions): Boolean`
- [x] 2.2 Implement default file-name matching: all space-separated tokens of `query` must appear in `fileName` (case-insensitive by default)
- [x] 2.3 Implement path matching: when `matchPath = true`, also check `relativePath` in addition to `fileName`
- [x] 2.4 Implement regex mode: compile query as regex, apply to `fileName` (and `relativePath` when `matchPath = true`); catch `PatternSyntaxException` and return `false`
- [x] 2.5 Implement case-sensitive toggle: honour `caseSensitive` flag in all string comparisons
- [x] 2.6 Add `isValidRegex(pattern: String): Boolean` helper on `SearchFilter`

## 3. Search UI Component

- [x] 3.1 Add a `JTextField` (placeholder text "Search files…") to `EditTrailPanel` as the search input field
- [x] 3.2 Add four `JCheckBox` controls (`Match path`, `Match content`, `Regex`, `Case sensitive`) below the search field
- [x] 3.3 Stack the search row and toggle row in a `JPanel(GridLayout(2, 1))` and place it between the sort bar and the list
- [x] 3.4 Show a red border on the search field when `Regex` is enabled and the query is an invalid regex; restore the default border otherwise

## 4. Debounce

- [x] 4.1 Wire a `javax.swing.Timer` (300 ms delay) to the search field's `DocumentListener`; restart the timer on each change and call `refresh()` on fire

## 5. Filtering in `refresh()`

- [x] 5.1 Read current `SearchOptions` from the UI state in `refresh()`
- [x] 5.2 When query is blank, show all entries (existing behaviour)
- [x] 5.3 When query is non-blank and `matchContent = false`, apply `SearchFilter.matches` to each entry and show only matching entries
- [x] 5.4 When `matchContent = true`, run content matching on a pooled thread; use a generation counter to discard stale results; apply combined results on the EDT

## 6. Content Search

- [x] 6.1 Implement content matching: read each history file's text via `VirtualFile.contentsToByteArray()` on a pooled thread; skip binary or unreadable files without crashing
- [x] 6.2 Combine content match results with name/path filter result (union) before updating the list model

## 7. Tests

- [x] 7.1 Unit-test `SearchFilter` for: default name match, case-insensitive, case-sensitive flag, token fuzzy match, path match, regex match, invalid regex returns false
- [x] 7.2 Unit-test `SearchOptions` fields and defaults
