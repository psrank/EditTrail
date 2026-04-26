# EditTrail — Iteration 4: Global Project Search

## Goal

Allow the user to search beyond the EditTrail history list by including all project files.

Files that are not part of history should be visually distinct so the user understands they are search results rather than recent activity.

## User Value

As a developer, I want to use the same EditTrail panel to find files that are not yet in my recent history, while still preserving the distinction between recent files and project-wide search results.

## Scope

### Included

- `Include all project files` toggle
- Search all project files by file name
- Optional path matching for project files
- Display non-history files in grey and italic
- Open non-history files from the result list
- Add opened global-search result to history after navigation

### Excluded

- Full content search across all project files
- Indexing customisation
- Search ranking beyond basic history priority
- External search engines

## Requirements

### Requirement: Provide global project search toggle

The plugin SHALL provide an `Include all project files` toggle.

#### Scenario: Toggle disabled by default

- GIVEN the EditTrail panel is opened
- WHEN no user preference exists
- THEN `Include all project files` SHALL be disabled by default

#### Scenario: Toggle enabled

- GIVEN the user enables `Include all project files`
- WHEN a search query is entered
- THEN EditTrail SHALL search history entries and project files

---

### Requirement: Search project files by name

When global search is enabled, the plugin SHALL include matching project files not already in history.

#### Scenario: Non-history file matches

- GIVEN `InvoiceService.cs` exists in the project
- AND it is not in EditTrail history
- WHEN global search is enabled
- AND the user searches `invoice`
- THEN `InvoiceService.cs` SHALL appear in results

#### Scenario: Empty query with global search enabled

- GIVEN global search is enabled
- WHEN the search query is empty
- THEN EditTrail SHOULD avoid showing every project file
- AND SHOULD continue showing only history entries

---

### Requirement: Prioritise history entries

History entries SHALL appear before non-history global search results.

#### Scenario: History and global result both match

- GIVEN `UserService.cs` is in history
- AND `UserSettingsService.cs` is not in history
- WHEN global search is enabled
- AND the user searches `user`
- THEN the history result SHOULD appear before the non-history result

---

### Requirement: Visually distinguish non-history files

Non-history global search results SHALL be visually distinct.

#### Scenario: Non-history result displayed

- GIVEN a matching file is not in history
- WHEN it appears in the list
- THEN it SHALL be styled in grey and italic
- AND it MAY show a label such as `Project result`

---

### Requirement: Open global project result

The user SHALL be able to open a non-history project file from the list.

#### Scenario: Open non-history result

- GIVEN a non-history file appears in global search results
- WHEN the user clicks it
- THEN the IDE SHALL open the file
- AND the file SHALL be added to EditTrail history
- AND its `lastViewedAt` SHALL be updated

---

### Requirement: Combine with file-type filters

Global search SHALL respect file-type filters.

#### Scenario: Global search with file type selected

- GIVEN `Include all project files` is enabled
- AND the `JSON` file-type filter is selected
- WHEN the user searches `settings`
- THEN only matching JSON history entries and JSON project files SHALL be shown

---

### Requirement: Avoid blocking the IDE

Project-wide search SHALL not block the UI thread.

#### Scenario: Large project

- GIVEN a project contains many files
- WHEN global search is enabled and query changes
- THEN the UI SHALL remain responsive
- AND results MAY update progressively

## Suggested Result Model

```kotlin
sealed class EditTrailResult {
    data class HistoryResult(val entry: FileHistoryEntry) : EditTrailResult()
    data class ProjectFileResult(
        val fileUrl: String,
        val fileName: String,
        val relativePath: String,
        val fileType: String
    ) : EditTrailResult()
}
```

## Completion Criteria

- Global search can be toggled on/off
- Non-history project files appear only when a non-empty query is entered
- Non-history files are visually distinct
- Opening a non-history result adds it to history
- Global results combine correctly with search and file-type filters
- Large projects remain responsive
