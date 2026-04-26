## ADDED Requirements

### Requirement: Provide search input
The EditTrail panel SHALL provide a search text field above the history list.

#### Scenario: Search field is visible
- **WHEN** the EditTrail tool window is open and the history panel is displayed
- **THEN** a search text field SHALL appear above the history list

#### Scenario: Empty search shows all entries
- **WHEN** the search field is empty
- **THEN** all history entries SHALL be shown subject to the active sort order

---

### Requirement: Search file names by default
The search field SHALL filter history entries by file name in a case-insensitive manner by default.

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

---

### Requirement: Support fuzzy matching
The search SHALL apply fuzzy matching to file names, interpreting each space-separated token as a required substring.

#### Scenario: Space-separated tokens all present
- **WHEN** the user types `user service`
- **THEN** an entry with file name `UserApplicationService.cs` SHALL match because both `user` and `service` appear in the name

#### Scenario: Token absent
- **WHEN** one space-separated token does not appear in the file name
- **THEN** the entry SHALL NOT match

---

### Requirement: Support path matching
The UI SHALL provide a `Match path` toggle that extends matching to the full relative path.

#### Scenario: Path matching disabled
- **WHEN** `Match path` is OFF
- **AND** the user searches `Application`
- **THEN** an entry at path `src/Application/Users/UserService.cs` whose file name does not contain `Application` SHALL NOT match

#### Scenario: Path matching enabled
- **WHEN** `Match path` is ON
- **AND** the user searches `Application`
- **THEN** an entry at path `src/Application/Users/UserService.cs` SHALL match

---

### Requirement: Support content matching
The UI SHALL provide a `Match content` toggle that extends matching to file content.

#### Scenario: Content matching enabled
- **WHEN** `Match content` is ON
- **AND** the user searches `CreateUserCommand`
- **THEN** an entry whose file content contains `CreateUserCommand` SHALL be shown

#### Scenario: Content search does not block the UI
- **WHEN** `Match content` is ON and the user types into the search field
- **THEN** content search SHALL run off the EDT so the IDE remains responsive

#### Scenario: Binary or unreadable file is skipped
- **WHEN** `Match content` is ON
- **AND** a history entry refers to a binary or unreadable file
- **THEN** that entry SHALL be excluded from content match results
- **AND** the plugin SHALL NOT crash

---

### Requirement: Support regex mode
The UI SHALL provide a `Regex` toggle that interprets the query as a regular expression.

#### Scenario: Regex matches correctly
- **WHEN** `Regex` is ON
- **AND** the user enters `User(Service|Repository)`
- **THEN** entries with file names matching the pattern SHALL be shown

#### Scenario: Invalid regex is handled safely
- **WHEN** `Regex` is ON
- **AND** the user enters an invalid regular expression
- **THEN** the search field SHALL display a non-blocking error indicator (e.g. red border)
- **AND** the plugin SHALL NOT crash

---

### Requirement: Support case-sensitive search
The UI SHALL provide a `Case sensitive` toggle.

#### Scenario: Case-sensitive search enabled
- **WHEN** `Case sensitive` is ON
- **AND** the user searches `userservice`
- **THEN** an entry with file name `UserService.cs` SHALL NOT match

#### Scenario: Case-sensitive search disabled
- **WHEN** `Case sensitive` is OFF
- **AND** the user searches `userservice`
- **THEN** an entry with file name `UserService.cs` SHALL match

---

### Requirement: Debounce search input
The plugin SHALL debounce search input to avoid recalculating filters on every keystroke.

#### Scenario: Fast typing triggers only one filter pass
- **WHEN** the user types multiple characters in rapid succession
- **THEN** the filter SHALL be recalculated at most once per debounce window (300 ms) rather than on every keystroke
