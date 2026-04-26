# EditTrail — Iteration 3: File Type Filters

## Goal

Add clickable file-type filters at the top of the EditTrail panel.

The plugin should scan known history entries and show file types as toggleable chips, allowing the user to quickly narrow history without typing.

## User Value

As a developer, I want to filter recent files by type, such as C#, JSON, XAML, Razor, Markdown, SQL, or XML, so that I can quickly focus on the part of the project I am working on.

## Scope

### Included

- File-type detection from history entries
- File-type chip bar
- Counts per file type
- Multi-select file-type filtering
- Toggle behaviour
- `All` chip to reset file-type filters
- Integration with search results

### Excluded

- Full project scanning
- User-defined file-type mappings
- Global project file-type counts
- Clustering by file type

## Requirements

### Requirement: Detect file types

The plugin SHALL classify history entries into user-friendly file types.

#### Scenario: C# file

- GIVEN a history entry has extension `.cs`
- WHEN file types are calculated
- THEN the file SHALL be classified as `C#`

#### Scenario: Razor file

- GIVEN a history entry has extension `.razor` or `.cshtml`
- WHEN file types are calculated
- THEN the file SHALL be classified as `Razor`

#### Scenario: Unknown extension

- GIVEN a history entry has an unrecognised extension
- WHEN file types are calculated
- THEN the file SHALL be classified as `Other`

---

### Requirement: Show file-type chips

The EditTrail panel SHALL show file-type chips above the history list.

#### Scenario: History has multiple file types

- GIVEN history contains C#, JSON, and Markdown files
- WHEN the panel is displayed
- THEN chips SHALL be shown for `All`, `C#`, `JSON`, and `Markdown`

#### Scenario: File type has no entries

- GIVEN history contains no SQL files
- WHEN chips are displayed
- THEN the `SQL` chip SHALL NOT be shown unless configured otherwise

---

### Requirement: Show counts

Each file-type chip SHALL show the number of matching entries.

#### Scenario: Count displayed

- GIVEN history contains 8 C# files
- WHEN the file-type chip bar is rendered
- THEN the C# chip SHALL display `C# 8`

#### Scenario: Counts update after history changes

- GIVEN history contains 8 C# files
- WHEN a new C# file is added to history
- THEN the C# chip count SHALL update to `C# 9`

---

### Requirement: Toggle file-type filters

File-type chips SHALL be toggleable.

#### Scenario: Select one file type

- GIVEN the user clicks the `C#` chip
- WHEN the filter is applied
- THEN only C# files SHALL be shown

#### Scenario: Toggle selected file type off

- GIVEN the `C#` chip is selected
- WHEN the user clicks `C#` again
- THEN the C# filter SHALL be removed

#### Scenario: Select multiple file types

- GIVEN the user selects `C#` and `JSON`
- WHEN the list is filtered
- THEN only C# and JSON files SHALL be shown

---

### Requirement: Provide All reset chip

The chip bar SHALL include an `All` chip.

#### Scenario: All selected by default

- GIVEN no file-type filters are active
- WHEN the chip bar is displayed
- THEN `All` SHALL appear selected

#### Scenario: User clicks All

- GIVEN one or more file-type filters are active
- WHEN the user clicks `All`
- THEN all file-type filters SHALL be cleared
- AND the full history list SHALL be shown subject to search and sort options

---

### Requirement: Combine with search

File-type filters SHALL combine with active search options.

#### Scenario: Search and file-type filter

- GIVEN the user searches `user`
- AND selects the `C#` file-type chip
- WHEN results are displayed
- THEN only C# files matching `user` SHALL be shown

## Suggested UI

```text
[ Search files...                         ]

File types:
[ All ] [ C# 18 ] [ JSON 7 ] [ Razor 3 ] [ SQL 2 ] [ Markdown 4 ] [ Other 5 ]

[ ] Match path   [ ] Match content   [ ] Regex   [ ] Case sensitive
```

## Suggested Data Model

```kotlin
data class FileTypeChip(
    val id: String,
    val label: String,
    val extensions: Set<String>,
    val count: Int,
    val selected: Boolean
)
```

## Suggested Default Mappings

```text
C#        .cs
XAML      .xaml
Razor     .razor, .cshtml
JSON      .json
XML       .xml
SQL       .sql
YAML      .yml, .yaml
Markdown  .md, .markdown
Kotlin    .kt, .kts
Java      .java
JavaScript .js, .jsx
TypeScript .ts, .tsx
HTML      .html, .htm
CSS       .css, .scss, .sass, .less
Other     fallback
```

## Completion Criteria

- File-type chips appear when history contains files
- Counts are correct
- Clicking chips filters the history
- Multiple file types can be selected
- All chip resets file-type filters
- File-type filters combine correctly with text search
