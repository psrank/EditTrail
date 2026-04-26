## ADDED Requirements

### Requirement: Sort history by last edited (default)
The plugin SHALL display history entries sorted by `lastEditedAt` descending by default.

#### Scenario: Default sort shows most recently edited files first
- **WHEN** the EditTrail tool window is displayed with no user sort preference set
- **THEN** files with the most recent `lastEditedAt` SHALL appear at the top of the list

#### Scenario: Files without an edit timestamp appear after edited files
- **WHEN** a file has only been viewed and has no `lastEditedAt` value
- **AND** the current sort is by last edited
- **THEN** the file SHALL appear after all files that have an edit timestamp
- **AND** the file SHALL still be visible in the history list

### Requirement: Sort history by last viewed
The plugin SHALL support an alternative sort mode ordering entries by `lastViewedAt` descending.

#### Scenario: Sort by last viewed orders entries correctly
- **WHEN** the user selects the "last viewed" sort mode
- **THEN** history entries SHALL be ordered by `lastViewedAt` descending
- **AND** the list SHALL refresh immediately to reflect the new order
