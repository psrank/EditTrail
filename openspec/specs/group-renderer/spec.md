## ADDED Requirements

### Requirement: Render a coloured group indicator for grouped entries
The cell renderer SHALL paint a coloured 4 px vertical bar on the left edge of each row whose `groupId` is non-null.

#### Scenario: Grouped entry shows coloured bar
- **WHEN** a history entry has a non-null `groupId`
- **THEN** a coloured 4 px vertical bar SHALL be visible on the left edge of that list row

#### Scenario: Ungrouped entry shows no bar
- **WHEN** a history entry has `groupId = null`
- **THEN** no coloured bar SHALL appear on that row
- **AND** the row SHALL render identically to the pre-grouping appearance

#### Scenario: Project file results are not grouped
- **WHEN** a `ProjectFileResult` row is rendered
- **THEN** no group indicator bar SHALL appear
- **AND** the row SHALL render identically to the pre-grouping appearance

---

### Requirement: Same group uses the same colour
Entries with the same `groupId` SHALL share the same colour within a session.

#### Scenario: Two entries in the same group share colour
- **WHEN** entry A and entry B have the same `groupId`
- **THEN** their coloured bars SHALL use identical colours

#### Scenario: Different groups use different colours where possible
- **WHEN** entry A and entry B have different `groupId` values
- **THEN** their coloured bars SHOULD use different colours

---

### Requirement: Group colours are stable within a session
The colour assigned to a group SHALL not change during the current IDE session unless the IDE is restarted.

#### Scenario: Colour stable after list refresh
- **WHEN** a group has been assigned colour blue during the current session
- **AND** the history list is refreshed (search, sort, or recalculation)
- **THEN** that group SHALL continue to display blue for the remainder of the session

#### Scenario: Colour stable after recalculation that reassigns same group ID
- **WHEN** `FileGrouper.assignGroups()` reassigns the same `groupId` integer after recalculation
- **THEN** the colour palette SHALL serve the same colour for that integer
