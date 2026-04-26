# EditTrail — Iteration 6: Settings and Polish

## Goal

Add settings, UX polish, and stability improvements after the core workflows are implemented.

This iteration turns EditTrail from a functional prototype into a more configurable and comfortable plugin.

## User Value

As a developer, I want EditTrail to fit my workflow, project size, and preferences without becoming complicated.

## Scope

### Included

- Settings page
- Maximum history size setting
- Default sort setting
- Search option persistence
- Global search preference
- Grouping enable/disable
- Clear history action
- Pin file action, if feasible
- UI polish
- Error handling improvements

### Excluded

- Cloud sync
- Team sharing
- AI-based grouping
- Dependency graph visualisation

## Requirements

### Requirement: Provide settings page

The plugin SHALL provide a settings page under IDE settings/preferences.

#### Scenario: Settings visible

- GIVEN the plugin is installed
- WHEN the user opens IDE settings
- THEN an EditTrail settings section SHALL be available

---

### Requirement: Configure maximum history size

The user SHALL be able to configure the maximum number of history entries.

#### Scenario: User changes limit

- GIVEN the default limit is 500
- WHEN the user changes it to 1000
- THEN EditTrail SHALL retain up to 1000 entries per project

#### Scenario: User lowers limit

- GIVEN history contains 500 entries
- WHEN the user changes the limit to 100
- THEN EditTrail SHALL trim older entries safely

---

### Requirement: Configure default sorting

The user SHALL be able to choose default sort mode.

#### Scenario: Default sort set to last viewed

- GIVEN the user selects `Last viewed` as default sort
- WHEN a project is reopened
- THEN EditTrail SHALL sort by last viewed by default

---

### Requirement: Persist search options

The plugin MAY persist search option preferences.

#### Scenario: Match path preference

- GIVEN the user enables `Match path`
- WHEN the project is reopened
- THEN EditTrail MAY restore `Match path` if preference persistence is enabled

---

### Requirement: Clear history

The plugin SHALL provide a clear history action.

#### Scenario: User clears history

- GIVEN history contains entries
- WHEN the user selects `Clear history`
- THEN EditTrail SHALL ask for confirmation
- AND after confirmation history SHALL be cleared

---

### Requirement: Pin files

The plugin SHOULD allow users to pin important files.

#### Scenario: Pin file

- GIVEN a file appears in history
- WHEN the user pins it
- THEN the file SHALL remain visible at the top of the list
- AND it SHALL not be removed by normal history trimming unless explicitly unpinned

#### Scenario: Unpin file

- GIVEN a file is pinned
- WHEN the user unpins it
- THEN it SHALL return to normal history ordering

---

### Requirement: Improve unavailable file handling

The plugin SHALL handle deleted, moved, or unavailable files gracefully.

#### Scenario: Deleted file

- GIVEN a history entry points to a deleted file
- WHEN the list is rendered
- THEN the item SHALL be marked unavailable or removed according to settings
- AND the plugin SHALL not crash

---

### Requirement: Provide responsive empty states

The plugin SHALL show helpful empty states.

#### Scenario: No search results

- GIVEN history contains entries
- WHEN the user searches for text with no matches
- THEN EditTrail SHALL show a no-results state
- AND SHOULD suggest clearing filters or enabling global search

#### Scenario: No history

- GIVEN no history exists
- WHEN the tool window is opened
- THEN EditTrail SHALL explain that files will appear after navigation or editing

## Suggested Settings

```text
History
- Maximum history entries: 500
- Default sort: Last edited / Last viewed
- Track viewed files: enabled
- Track edited files: enabled

Search
- Match path by default: disabled
- Regex by default: disabled
- Case sensitive by default: disabled
- Include all project files by default: disabled

Grouping
- Enable visual grouping: enabled
- Recalculate groups automatically: enabled

Privacy
- Store data locally only: always enabled
```

## Completion Criteria

- Settings page exists
- History size can be configured
- Sort preference can be persisted
- Clear history works with confirmation
- Deleted files do not break the UI
- Empty and no-result states are helpful
- Plugin feels stable and IDE-native
