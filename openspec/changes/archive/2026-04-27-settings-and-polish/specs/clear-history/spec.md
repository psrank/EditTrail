## ADDED Requirements

### Requirement: Provide a clear history action
The plugin SHALL provide a "Clear history" action in the EditTrail tool window that removes all history entries for the current project after user confirmation.

#### Scenario: User clears history
- **GIVEN** the EditTrail tool window is open and history contains entries
- **WHEN** the user clicks "Clear history"
- **THEN** a confirmation dialog SHALL be shown
- **AND** after the user confirms, all history entries SHALL be removed
- **AND** the history list SHALL be empty

#### Scenario: User cancels the confirmation dialog
- **GIVEN** the confirmation dialog is shown
- **WHEN** the user clicks Cancel or No
- **THEN** history SHALL NOT be modified
