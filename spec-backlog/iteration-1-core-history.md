# EditTrail — Iteration 1: Core History

## Goal

Create the foundation of EditTrail: a JetBrains tool window that tracks recently viewed and edited files in the current project.

This iteration should provide immediate value before search, filters, grouping, or global project search are introduced.

## User Value

As a developer working in a large project, I want to see the files I recently viewed or edited so that I can quickly return to my current working context without using the project tree.

## Scope

### Included

- Project-level tool window
- Track recently viewed files
- Track recently edited files
- Deduplicate file history
- Sort by last edited or last viewed
- Open file from history list
- Persist history per project
- Basic empty state
- Basic maximum history limit

### Excluded

- Search
- File-type filter chips
- Global project search
- Grouping
- K-means clustering
- Content search
- Settings page

## Requirements

### Requirement: Provide an EditTrail tool window

The plugin SHALL provide a project-level tool window named `EditTrail`.

#### Scenario: Tool window is visible

- GIVEN the plugin is installed
- WHEN a project is opened
- THEN the IDE SHALL expose an `EditTrail` tool window
- AND the tool window SHALL contain the history panel

#### Scenario: No history exists

- GIVEN the project has no recorded history
- WHEN the EditTrail tool window is opened
- THEN the panel SHALL show an empty state message
- AND the message SHALL explain that recently viewed and edited files will appear there

---

### Requirement: Track viewed files

The plugin SHALL record when a user opens or selects a file in the editor.

#### Scenario: User opens a file

- GIVEN a project is open
- WHEN the user opens a file in the editor
- THEN the file SHALL be added to EditTrail history
- AND `lastViewedAt` SHALL be updated
- AND `viewCount` SHALL increase by one

#### Scenario: User switches editor tab

- GIVEN multiple files are open
- WHEN the user switches from one file tab to another
- THEN the newly selected file SHALL be recorded as viewed

#### Scenario: Non-file editor is selected

- GIVEN the user opens a tool window, settings page, database console, or other non-file editor
- WHEN the selection changes
- THEN EditTrail SHALL ignore the selection

---

### Requirement: Track edited files

The plugin SHALL record when a file is edited.

#### Scenario: User edits a file

- GIVEN a file is in the editor
- WHEN the file content changes
- THEN the file SHALL be added to EditTrail history if not already present
- AND `lastEditedAt` SHALL be updated
- AND `editCount` SHALL increase by one

#### Scenario: File is edited multiple times quickly

- GIVEN the user is actively typing in a file
- WHEN multiple document change events occur within a short period
- THEN EditTrail MAY debounce edit tracking
- AND the file SHALL still appear as recently edited

---

### Requirement: Deduplicate history entries

The plugin SHALL store one history entry per file.

#### Scenario: File is reopened

- GIVEN a file already exists in history
- WHEN the file is opened again
- THEN no duplicate entry SHALL be created
- AND the existing entry SHALL be updated

#### Scenario: File is edited after being viewed

- GIVEN a file already exists in history because it was viewed
- WHEN the file is edited
- THEN the same entry SHALL be updated with edit metadata

---

### Requirement: Sort history

The plugin SHALL support sorting by last edited and last viewed.

#### Scenario: Default sort is last edited

- GIVEN history contains viewed and edited files
- WHEN the tool window is displayed
- THEN files with recent edits SHALL appear before files with older edits

#### Scenario: Sort by last viewed

- GIVEN the user changes sorting mode to last viewed
- WHEN the list is refreshed
- THEN files SHALL be ordered by `lastViewedAt` descending

#### Scenario: Files without edit timestamp

- GIVEN a file has only been viewed but not edited
- WHEN sorting by last edited
- THEN the file SHALL appear after files with edit timestamps
- AND it SHALL still be visible in the history list

---

### Requirement: Open files from history

The plugin SHALL allow the user to open a file by selecting it from the history list.

#### Scenario: User clicks history item

- GIVEN a file is visible in the history list
- WHEN the user clicks or presses Enter on the item
- THEN the IDE SHALL open that file in the editor

#### Scenario: File no longer exists

- GIVEN a history entry points to a deleted or moved file
- WHEN the user attempts to open it
- THEN EditTrail SHALL not crash
- AND the entry SHALL be marked unavailable or removed

---

### Requirement: Persist project history

The plugin SHALL persist history per project.

#### Scenario: Project is reopened

- GIVEN the user has history entries in a project
- WHEN the IDE is closed and reopened
- THEN EditTrail SHALL restore the project's history

#### Scenario: Different projects

- GIVEN the user opens two different projects
- WHEN EditTrail history is viewed in each project
- THEN each project SHALL have independent history

---

### Requirement: Limit history size

The plugin SHALL enforce a maximum number of stored history entries.

#### Scenario: History exceeds limit

- GIVEN the maximum history size is configured as 500 entries
- WHEN a new file is added after the limit is reached
- THEN the oldest least-recently-used entry SHALL be removed

## Suggested Data Model

```kotlin
data class FileHistoryEntry(
    val fileUrl: String,
    val fileName: String,
    val relativePath: String,
    var lastViewedAt: Long?,
    var lastEditedAt: Long?,
    var viewCount: Int,
    var editCount: Int,
    var exists: Boolean = true
)
```

## Suggested Services

- `EditTrailProjectService`
- `FileHistoryState`
- `FileHistoryRepository`
- `EditorSelectionListener`
- `DocumentEditListener`
- `EditTrailToolWindowFactory`

## Implementation Notes

Use IntelliJ Platform project services for state and behaviour.

Use editor selection/listener APIs to detect viewed files.

Use document or VFS change APIs to detect edited files.

Use `VirtualFile` as the runtime file reference and persist stable file URL/path values.

## Completion Criteria

- Tool window appears in IntelliJ IDEA and Rider
- Opened files appear in the list
- Edited files appear in the list
- Sorting works for last edited and last viewed
- Clicking an item opens the file
- History persists after IDE restart
