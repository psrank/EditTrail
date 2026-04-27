## ADDED Requirements

### Requirement: Deduplicate history entries
The plugin SHALL store one history entry per file; opening or editing a file a second time SHALL update the existing entry.

#### Scenario: File is reopened
- **WHEN** a file that already exists in history is opened again
- **THEN** no duplicate entry SHALL be created
- **AND** the existing entry's `lastViewedAt` and `viewCount` SHALL be updated

#### Scenario: File is edited after being viewed
- **WHEN** a file that already exists in history (view-only) is edited
- **THEN** the same entry SHALL be updated with `lastEditedAt` and `editCount`
- **AND** no new entry SHALL be created

### Requirement: Persist history per project
The plugin SHALL persist history entries per project across IDE restarts using IntelliJ's `PersistentStateComponent`.

#### Scenario: Project is reopened
- **WHEN** the IDE is closed and the same project is reopened
- **THEN** EditTrail SHALL restore the project's history entries

#### Scenario: Different projects have independent histories
- **WHEN** the user opens two different projects
- **THEN** each project SHALL have its own independent EditTrail history

### Requirement: Enforce a maximum history size
The plugin SHALL limit stored history entries to a configurable maximum (default 500). The limit is controlled by `EditTrailAppSettings.maxHistorySize` and applied at runtime without an IDE restart.

#### Scenario: History exceeds the limit
- **WHEN** a new file is added to history and the total count exceeds the configured maximum
- **THEN** the oldest least-recently-used entry SHALL be evicted
- **AND** the total count SHALL remain at or below the maximum

#### Scenario: User changes the limit in settings
- **WHEN** the user reduces `Max history entries` in IDE Settings
- **THEN** EditTrail SHALL trim oldest entries immediately so the total is at or below the new limit

### Requirement: Clear history
The plugin SHALL allow the user to remove all history entries for the current project via a dedicated action.

#### Scenario: History is cleared
- **WHEN** `clearHistory()` is invoked on `EditTrailProjectService`
- **THEN** all stored history entries SHALL be removed
- **AND** the `HISTORY_UPDATED` event SHALL be published so the UI refreshes
