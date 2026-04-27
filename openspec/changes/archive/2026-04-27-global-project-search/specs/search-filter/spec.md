## MODIFIED Requirements

### Requirement: Provide search input
The EditTrail panel SHALL provide a search text field above the history list.

#### Scenario: Search field is visible
- **WHEN** the EditTrail tool window is open and the history panel is displayed
- **THEN** a search text field SHALL appear above the history list

#### Scenario: Empty search shows all entries
- **WHEN** the search field is empty
- **AND** global project search is disabled
- **THEN** all history entries SHALL be shown subject to the active sort order

#### Scenario: Empty search with global search enabled shows only history
- **WHEN** the search field is empty
- **AND** global project search is enabled
- **THEN** only history entries SHALL be shown (no project-wide enumeration)

---

### Requirement: Search file names by default
The search field SHALL filter history entries by file name in a case-insensitive manner by default. When global search is enabled, the same query also applies to project file results.

#### Scenario: User searches by file name
- **WHEN** the user types `user` into the search field
- **THEN** only history entries whose file name contains `user` (case-insensitive) SHALL be shown

#### Scenario: Search is case-insensitive by default
- **WHEN** the case-sensitive toggle is OFF
- **AND** the user types `userservice`
- **THEN** entries with file name `UserService.cs` SHALL match

#### Scenario: Non-matching file is hidden
- **WHEN** the user types a query that matches no file name
- **THEN** non-matching entries SHALL be hidden from the list

#### Scenario: Search query applies to both history and project results
- **WHEN** global project search is enabled
- **AND** the user types `invoice`
- **THEN** both history entries and non-history project files whose name contains `invoice` SHALL be included in results
