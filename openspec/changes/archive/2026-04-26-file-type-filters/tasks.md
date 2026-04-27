## 1. Data Model

- [x] 1.1 Create `FileTypeClassifier` object with a `classify(fileName: String): String` function and a built-in extension-to-label map (C#, XAML, Razor, JSON, XML, SQL, YAML, Markdown, Kotlin, Java, JavaScript, TypeScript, HTML, CSS, Other)
- [x] 1.2 Create `FileTypeChip` data class with fields: `label: String`, `count: Int`, `selected: Boolean`

## 2. Classification Logic

- [x] 2.1 Implement extension extraction in `FileTypeClassifier.classify`: strip the extension, lowercase it, look up in the map; return `Other` for unrecognised or missing extensions
- [x] 2.2 Ensure extension matching is case-insensitive (normalise to lowercase before lookup)

## 3. Chip Bar UI Component

- [x] 3.1 Add `selectedFileTypes: MutableSet<String>` to `EditTrailPanel` (empty = All selected)
- [x] 3.2 Add a `chipBarPanel` (`JPanel(FlowLayout(FlowLayout.LEFT, 4, 2))`) wrapped in a horizontal `JScrollPane` to `EditTrailPanel`
- [x] 3.3 Place the chip bar panel between the search/toggle controls and the history list (insert into the top compound panel)
- [x] 3.4 Implement `rebuildChipBar(chips: List<FileTypeChip>)` that clears and repopulates `chipBarPanel`: one `JButton` for `All`, one `JToggleButton` per chip type, each showing `"${chip.label} (${chip.count})"`
- [x] 3.5 Style the `All` button to appear selected (bold or highlighted border) when `selectedFileTypes` is empty
- [x] 3.6 Wire each type `JToggleButton`'s action listener to toggle its label in `selectedFileTypes` then call `refresh()`
- [x] 3.7 Wire the `All` button's action listener to clear `selectedFileTypes` then call `refresh()`

## 4. Filtering in `refresh()`

- [x] 4.1 After computing the search-filtered entry list, compute per-type counts: group visible entries by `FileTypeClassifier.classify(entry.fileName)` and count each group
- [x] 4.2 Build the `List<FileTypeChip>` from the counts map, marking each chip selected if its label is in `selectedFileTypes`
- [x] 4.3 Call `rebuildChipBar(chips)` with the computed list on every `refresh()` invocation
- [x] 4.4 Apply file-type filter: if `selectedFileTypes` is non-empty, retain only entries whose classified type is in `selectedFileTypes`; if empty, show all search-filtered entries

## 5. Tests

- [x] 5.1 Unit-test `FileTypeClassifier.classify` for: known extensions (`.cs`, `.kt`, `.json`, `.razor`, `.cshtml`, `.xaml`, `.xml`, `.sql`, `.yml`, `.yaml`, `.md`, `.markdown`, `.java`, `.js`, `.jsx`, `.ts`, `.tsx`, `.html`, `.htm`, `.css`, `.scss`), case-insensitive extension (`.CS`), unknown extension (`Other`), no extension (`Other`)
- [x] 5.2 Unit-test `FileTypeChip` data class construction and equality
