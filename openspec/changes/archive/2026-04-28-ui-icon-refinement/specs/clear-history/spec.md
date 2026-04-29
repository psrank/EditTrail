## MODIFIED Requirements

### Requirement: Provide a clear history action
The plugin SHALL provide a `Clear history` icon action on the EditTrail icon toolbar that removes all history entries for the current project after user confirmation. The action SHALL be rendered as an icon-only `AnAction` with a tooltip describing it.

#### Scenario: User clears history
- **GIVEN** the EditTrail tool window is open and history contains entries
- **WHEN** the user clicks the `Clear history` icon action
- **THEN** a confirmation dialog SHALL be shown
- **AND** after the user confirms, all history entries SHALL be removed
- **AND** the history list SHALL be empty

#### Scenario: User cancels the confirmation dialog
- **GIVEN** the confirmation dialog is shown
- **WHEN** the user clicks Cancel or No
- **THEN** history SHALL NOT be modified

#### Scenario: Clear history control is an icon action with a tooltip
- **WHEN** the icon toolbar is displayed
- **THEN** the `Clear history` control SHALL be rendered as an icon-only button (no visible text label)
- **AND** hovering it SHALL show a non-empty tooltip describing the clear-history action
