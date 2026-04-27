## ADDED Requirements

### Requirement: Render non-history project file results distinctly
The cell renderer SHALL visually distinguish non-history project file results from history entries.

#### Scenario: Non-history row uses grey italic style
- **WHEN** a non-history project file result appears in the list
- **THEN** the file name SHALL be rendered in grey italic text

#### Scenario: Non-history row shows Project result label
- **WHEN** a non-history project file result appears in the list
- **THEN** a `Project result` label or badge SHALL be visible on the row

#### Scenario: History entry rows are unaffected
- **WHEN** a history entry appears in the list
- **THEN** it SHALL render with the same appearance as before global search was introduced
