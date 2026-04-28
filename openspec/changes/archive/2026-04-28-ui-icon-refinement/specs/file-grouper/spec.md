## MODIFIED Requirements

### Requirement: Provide a manual group recalculation action
The plugin SHALL provide a way for the user to trigger group recalculation on demand via an icon action on the EditTrail icon toolbar. The action SHALL be rendered as an icon-only `AnAction` with a tooltip describing it.

#### Scenario: User triggers recalculation
- **WHEN** the user activates the `Recalculate groups` icon action on the EditTrail toolbar
- **THEN** `FileGrouper.assignGroups()` SHALL be dispatched on a pooled thread
- **AND** the list SHALL update with new group indicators when complete

#### Scenario: Recalculate control is an icon action with a tooltip
- **WHEN** the icon toolbar is displayed
- **THEN** the `Recalculate groups` control SHALL be rendered as an icon-only button (no visible text label)
- **AND** hovering it SHALL show a non-empty tooltip describing the recalculation action
