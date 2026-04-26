## ADDED Requirements

### Requirement: Track viewed files
The plugin SHALL record when a user opens or selects a file in the editor.

#### Scenario: User opens a file
- **WHEN** the user opens a file in the editor
- **THEN** the file SHALL be added to EditTrail history
- **AND** `lastViewedAt` SHALL be set to the current timestamp
- **AND** `viewCount` SHALL increase by one

#### Scenario: User switches editor tab
- **WHEN** the user switches from one open file tab to another
- **THEN** the newly selected file SHALL be recorded as viewed

#### Scenario: Non-file editor is selected
- **WHEN** the user activates a tool window, settings page, database console, or other non-file editor
- **THEN** EditTrail SHALL ignore the selection event and make no history change

### Requirement: Track edited files
The plugin SHALL record when a file is edited.

#### Scenario: User edits a file
- **WHEN** the content of an open file changes
- **THEN** the file SHALL be added to EditTrail history if not already present
- **AND** `lastEditedAt` SHALL be updated to the current timestamp
- **AND** `editCount` SHALL increase by one

#### Scenario: Rapid edits are debounced
- **WHEN** multiple document change events occur for the same file within a short period (≤ 2 seconds)
- **THEN** EditTrail MAY coalesce those events into a single history update
- **AND** the file SHALL still appear as recently edited after the debounce window expires
