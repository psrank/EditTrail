## ADDED Requirements

### Requirement: Assign group IDs to history entries
The plugin SHALL assign a group ID (`groupId: Int`) to `FileHistoryEntry` objects that share strong co-occurrence signals. The group ID SHALL be session-scoped and NOT persisted across IDE restarts.

#### Scenario: Related files receive the same group ID
- **WHEN** two or more history entries have been opened within 30 minutes of each other and share the same parent directory
- **THEN** `FileGrouper.assignGroups()` SHALL assign them the same `groupId`

#### Scenario: Unrelated files receive no group ID
- **WHEN** two history entries have no time proximity and no path or extension overlap
- **THEN** both entries SHALL have `groupId = null`

#### Scenario: Isolated entry receives no group ID
- **WHEN** a history entry scores below the grouping threshold against every other entry
- **THEN** its `groupId` SHALL remain `null`

#### Scenario: Group assignment does not crash on empty history
- **WHEN** `FileGrouper.assignGroups()` is called with an empty list
- **THEN** it SHALL return immediately without error

#### Scenario: Group assignment does not crash on single entry
- **WHEN** `FileGrouper.assignGroups()` is called with exactly one entry
- **THEN** the entry SHALL have `groupId = null` and no error SHALL be thrown

---

### Requirement: Recalculate groups in the background
Group assignment SHALL run off the EDT and SHALL not block the UI.

#### Scenario: Groups assigned without blocking EDT
- **WHEN** the history list is refreshed
- **THEN** `FileGrouper.assignGroups()` SHALL run on a pooled thread
- **AND** the result SHALL be posted back to the EDT via `invokeLater`

#### Scenario: Grouping failure does not break the history list
- **WHEN** `FileGrouper.assignGroups()` throws an unexpected exception
- **THEN** the history list SHALL still render all entries without group indicators
- **AND** the plugin SHALL NOT crash

---

### Requirement: Provide a manual group recalculation action
The plugin SHALL provide a way for the user to trigger group recalculation on demand.

#### Scenario: User triggers recalculation
- **WHEN** the user activates the "Recalculate groups" action in the EditTrail panel
- **THEN** `FileGrouper.assignGroups()` SHALL be dispatched on a pooled thread
- **AND** the list SHALL update with new group indicators when complete
