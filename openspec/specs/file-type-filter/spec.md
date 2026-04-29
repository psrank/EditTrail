### Requirement: Classify history entries by file type
The plugin SHALL classify each history entry into a user-friendly file-type label based on its file extension.

#### Scenario: Known extension maps to label
- **WHEN** a history entry has a file extension that matches a known mapping (e.g., `.cs` → `C#`, `.kt` → `Kotlin`)
- **THEN** the entry SHALL be classified with that label

#### Scenario: Unknown extension falls back to Other
- **WHEN** a history entry has a file extension that does not match any known mapping
- **THEN** the entry SHALL be classified as `Other`

#### Scenario: Extension matching is case-insensitive
- **WHEN** a history entry has extension `.CS` or `.Cs`
- **THEN** it SHALL be classified as `C#`, the same as `.cs`

#### Scenario: File with no extension
- **WHEN** a history entry has no file extension
- **THEN** the entry SHALL be classified as `Other`

---

### Requirement: Show file-type chip bar
The EditTrail panel SHALL show a chip bar containing one chip per file type present in the current history, placed between the search controls and the history list.

#### Scenario: Chips reflect present types only
- **WHEN** history contains C# and JSON files
- **THEN** chips SHALL appear for `C#` and `JSON` but NOT for types with zero entries

#### Scenario: All chip appears first
- **WHEN** the chip bar is displayed
- **THEN** an `All` chip SHALL appear as the first chip, before any type chips

#### Scenario: No history
- **WHEN** history is empty
- **THEN** the chip bar SHALL show only the `All` chip (no type chips)

---

### Requirement: Show per-type counts on chips
Each file-type chip SHALL display the count of currently visible entries of that type.

#### Scenario: Count shown on chip
- **WHEN** history contains 8 C# entries that match the current search
- **THEN** the C# chip SHALL display `C# (8)` or equivalent label with count

#### Scenario: Counts update with search
- **WHEN** the user types a search query that reduces C# matches from 8 to 3
- **THEN** the C# chip count SHALL update to 3

---

### Requirement: Filter history by selected file types
The chip bar SHALL allow the user to select one or more file-type chips to filter the history list.

#### Scenario: Select one file type
- **WHEN** the user clicks the `C#` chip
- **THEN** only history entries classified as `C#` SHALL appear in the list

#### Scenario: Select multiple file types
- **WHEN** the user selects both `C#` and `JSON` chips
- **THEN** history entries classified as `C#` OR `JSON` SHALL appear in the list

#### Scenario: Toggle chip off
- **WHEN** the user clicks an already-selected chip
- **THEN** that chip SHALL be deselected and the list SHALL update accordingly

#### Scenario: All chip resets selection
- **WHEN** one or more type chips are selected
- **AND** the user clicks the `All` chip
- **THEN** all type-chip selections SHALL be cleared
- **AND** the full history (subject to search and sort) SHALL be shown

#### Scenario: All chip is active by default
- **WHEN** no file-type chips are selected
- **THEN** the `All` chip SHALL appear in the active/selected visual state

---

### Requirement: Combine file-type filter with search
File-type chip selections SHALL compose with the active text search filter.

#### Scenario: Both search and chip filter active
- **WHEN** the search query is `user` and the `C#` chip is selected
- **THEN** the list SHALL show only entries whose file name matches `user` AND whose type is `C#`

#### Scenario: Chip filter with no search
- **WHEN** the search field is empty and the `JSON` chip is selected
- **THEN** all JSON entries SHALL be shown regardless of name
