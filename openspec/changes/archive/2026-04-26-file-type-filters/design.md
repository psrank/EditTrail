## Context

EditTrail's history panel currently shows all recorded file entries, with text-based search (from iteration 2) as the only filtering mechanism. Developers working in mixed-technology projects (e.g., Kotlin + XML + SQL + Markdown) often want to quickly narrow the list to a specific file type without typing a search query. A chip bar — a row of toggleable filter buttons, one per detected extension family — is the standard IDE pattern for this kind of categorical filtering.

The existing panel layout (`EditTrailPanel.kt`) stacks: sort bar → search field + toggles → list. The chip bar will be inserted between the search toggles and the list.

## Goals / Non-Goals

**Goals:**
- Classify history entries into named file-type categories using extension-to-label mappings
- Render a dynamic chip bar showing only the file types present in the current history
- Show per-type counts on each chip
- Support multi-select: selecting multiple chips shows the union of matching entries
- Provide an `All` chip that resets all file-type selections
- Compose file-type filtering with the existing search filter and sort order

**Non-Goals:**
- Persisting chip selection across sessions (ephemeral UI state)
- User-configurable extension mappings
- Full project file scan (history-backed only)
- Clustering or grouping the list by file type (just filtering)

## Decisions

### Decision 1: Introduce a `FileTypeClassifier` singleton object

A pure `FileTypeClassifier` object holds the extension-to-label map and exposes a `classify(fileName: String): String` function. This keeps the mapping logic isolated and testable independently of any UI class.

**Alternatives considered:**
- Inline the logic in `EditTrailPanel`: harder to unit-test; mixing UI and business logic.
- Enum-based type system: more rigid; would require code changes to add a new mapping.

### Decision 2: Represent chip state as `Set<String>` of selected type labels

The panel holds a `selectedFileTypes: MutableSet<String>` (initially empty = All). Empty set = no filter (show all). Non-empty set = show entries whose classified type is in the set. This avoids a separate boolean flag for the "All" state.

**Alternatives considered:**
- Nullable selected type string: restricts to single-select.
- Separate `allSelected: Boolean` flag: redundant; the empty-set convention is simpler.

### Decision 3: Chip bar is a `JPanel` with `FlowLayout`

Each chip is a `JToggleButton`. The `All` button is a plain `JButton` that clears `selectedFileTypes`. The chip bar is placed in a `JScrollPane` (horizontal only) to handle scenarios where many file types are present and the panel is narrow.

**Alternatives considered:**
- Custom painted chip widget: over-engineering for an IntelliJ plugin.
- `JCheckBox` row: less visually distinct; doesn't communicate "chip" semantics.

### Decision 4: Counts are computed inside `refresh()`

On every `refresh()` call, after the current list of entries is determined (post-search), the chip counts are recomputed. The chip bar is then rebuilt or updated in-place. This keeps counts in sync with search without an additional observer.

**Alternatives considered:**
- Observer on `FileHistoryRepository`: more decoupled but more complex; counts after search filter are more useful than total counts.

## Risks / Trade-offs

- [Chip bar rerender on each keystroke] → Mitigation: chip bar rebuild is O(N) over history size; for typical history sizes (<1000 entries) this is imperceptible. If profiling reveals a bottleneck, debounce the chip rebuild.
- [Large number of distinct file types] → Mitigation: the `JScrollPane` wrapper handles overflow gracefully.
- [Counts may be confusing when search is also active] → Mitigation: counts reflect post-search matches per type, so they always sum to the visible entry count.
