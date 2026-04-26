## Context

`EditTrailPanel` currently shows all history entries filtered only by `exists = true` and the active `SortMode`. There is no search input, so the list cannot be narrowed. Iteration 2 adds a search bar with optional toggles above the list.

The existing panel layout is: sort combo (NORTH) + scrollable `JBList` (CENTER). The search bar will sit between those, expanding the NORTH region into a stacked `JPanel`.

The plugin has no external dependencies beyond IntelliJ Platform. All filtering is implemented in pure Kotlin.

## Goals / Non-Goals

**Goals:**

- Add a search text field above the history list
- Filter the visible list by `fileName` (default) or `relativePath` (opt-in toggle)
- Support fuzzy matching on file names
- Support regex mode with safe error handling
- Support case-sensitive toggle
- Support optional content matching (off-EDT)
- Debounce the search input to avoid recalculating on every keystroke
- Introduce a `SearchOptions` data class to hold filter state

**Non-Goals:**

- Persisting search state across IDE restarts
- "Saved searches" or named filters
- Highlighting matched characters in the cell renderer (future polish)
- Search over files not in history
- Global project file search (separate iteration)

## Decisions

### Decision 1: `SearchOptions` data class holds all filter state

**Chosen**: Introduce `data class SearchOptions(val query: String, val matchPath: Boolean, val matchContent: Boolean, val regex: Boolean, val caseSensitive: Boolean)` as a plain data class in `SearchOptions.kt`.

**Rationale**: Groups the five filter parameters into one object, making it easy to pass to a filter function and enabling equality comparison for change detection.

---

### Decision 2: `SearchFilter` object implements the matching logic

**Chosen**: Create a top-level `object SearchFilter` with a `fun matches(entry: FileHistoryEntry, options: SearchOptions): Boolean` method. Regex compilation errors are caught and treated as no-match (returns `false`) for the entry; a separate `fun isValidRegex(pattern: String): Boolean` helper drives the UI validation state.

**Rationale**: Keeps filter logic independently testable without constructing a panel. Separating regex validation from matching lets the panel show an error indicator without crashing.

---

### Decision 3: Search bar is a separate `JPanel` stacked above the sort bar

**Chosen**: Add a `createSearchBar()` method to `EditTrailPanel` that returns a `JPanel` containing the `JTextField` and toggle checkboxes. Stack sort bar and search bar in a `JPanel(GridLayout(2, 1))` placed in the NORTH position.

**Rationale**: Minimal changes to the existing layout. The NORTH region currently holds one row; replacing it with a two-row panel keeps the CENTER (`JBScrollPane`) untouched.

---

### Decision 4: Debounce search input with a 300 ms `Timer`

**Chosen**: Use a single `javax.swing.Timer` (EDT-safe) with a 300 ms delay. On each `DocumentEvent`, restart the timer. On fire, call `refresh()`.

**Rationale**: `javax.swing.Timer` fires on the EDT so no manual `invokeLater` is needed. 300 ms is snappy but avoids recalculating on every keystroke. Simpler than a `ScheduledExecutorService` for a UI-bound timer.

---

### Decision 5: Content search runs in a background coroutine via `ApplicationManager.getApplication().executeOnPooledThread`

**Chosen**: When `matchContent = true`, spawn a pooled thread to read file contents, collect matching URLs, then `invokeLater` to apply results to the list model.

**Rationale**: Reading `VirtualFile` content can block on I/O. The plugin already uses `invokeLater` for list updates; extending the pattern to a pooled thread keeps the EDT free. If a newer search starts before the old one finishes, the old result is discarded (checked via a generation counter).

---

### Decision 6: Invalid regex shows a red border on the search field, no crash

**Chosen**: On each search change, call `SearchFilter.isValidRegex(query)` when regex mode is on. If invalid, set the field's border to `BorderFactory.createLineBorder(JBColor.RED)` and skip filtering (retain the last valid result set). If valid, restore the default border.

**Rationale**: Non-blocking validation matches the spec requirement. Retaining the last valid results avoids a flash of an empty list mid-typing.

## Risks / Trade-offs

- **Content search latency**: Reading many large files may be slow. Acceptable for MVP; a cache or index can be added later.
- **Generation counter race**: A slow content search completing after a new query starts will be discarded, but the generation counter approach is simple and correct.
- **Fuzzy matching complexity**: A full fuzzy algorithm (e.g. Smith-Waterman) is overkill. Using "all space-separated tokens are substrings of the file name" covers common cases ("user service" → "UserApplicationService") without a dependency.
