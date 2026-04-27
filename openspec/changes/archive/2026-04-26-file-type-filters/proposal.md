## Why

The EditTrail panel currently shows every history entry indiscriminately, making it hard to focus on a specific kind of work. Developers working in mixed-technology codebases (Kotlin + XML + SQL, for example) benefit greatly from being able to narrow the history to just the file types they care about at a given moment — without typing.

## What Changes

- Add a file-type classification system that maps file extensions to user-friendly labels (C#, Kotlin, JSON, Razor, etc.)
- Add a chip bar above the history list showing one chip per detected file type, each with a count
- Chips are toggleable; selecting one or more chips filters the history list to matching entries
- An `All` chip resets all file-type filters
- File-type filters compose with the existing search query and sort order

## Capabilities

### New Capabilities
- `file-type-filter`: Detection and classification of history entries by file type, a toggleable chip bar UI, count badges, multi-select behaviour, integration with search

### Modified Capabilities
- `tool-window`: History panel layout changes to include the chip bar row between the search field and the list

## Impact

- New `FileTypeChip` data class and `FileTypeClassifier` object in the main source set
- `EditTrailPanel` updated to add a chip bar panel component
- `FileHistoryRepository` or `EditTrailPanel` refresh logic updated to compute per-type counts on each refresh
- No plugin API changes; purely additive UI work
- No persistence changes required (selections are ephemeral)
