## MODIFIED Requirements

### Requirement: Support path matching
The UI SHALL provide a `Match path` icon toggle on the EditTrail icon toolbar that extends matching to the full relative path. The control SHALL be rendered as an icon-only `ToggleAction` with a tooltip describing its function and a clear active visual state when on.

#### Scenario: Path matching disabled
- **WHEN** the `Match path` icon toggle is OFF
- **AND** the user searches `Application`
- **THEN** an entry at path `src/Application/Users/UserService.cs` whose file name does not contain `Application` SHALL NOT match

#### Scenario: Path matching enabled
- **WHEN** the `Match path` icon toggle is ON
- **AND** the user searches `Application`
- **THEN** an entry at path `src/Application/Users/UserService.cs` SHALL match

#### Scenario: Match path control is an icon toggle with a tooltip
- **WHEN** the icon toolbar is displayed
- **THEN** the `Match path` control SHALL be rendered as an icon-only button (no visible text label)
- **AND** hovering it SHALL show a non-empty tooltip describing path matching
- **AND** when the toggle is ON, the button SHALL render in the platform's selected/pressed state

---

### Requirement: Support content matching
The UI SHALL provide a `Match content` icon toggle on the EditTrail icon toolbar that extends matching to file content. The control SHALL be rendered as an icon-only `ToggleAction` with a tooltip describing its function and a clear active visual state when on.

#### Scenario: Content matching enabled
- **WHEN** the `Match content` icon toggle is ON
- **AND** the user searches `CreateUserCommand`
- **THEN** an entry whose file content contains `CreateUserCommand` SHALL be shown

#### Scenario: Content search does not block the UI
- **WHEN** the `Match content` icon toggle is ON and the user types into the search field
- **THEN** content search SHALL run off the EDT so the IDE remains responsive

#### Scenario: Binary or unreadable file is skipped
- **WHEN** the `Match content` icon toggle is ON
- **AND** a history entry refers to a binary or unreadable file
- **THEN** that entry SHALL be excluded from content match results
- **AND** the plugin SHALL NOT crash

#### Scenario: Match content control is an icon toggle with a tooltip
- **WHEN** the icon toolbar is displayed
- **THEN** the `Match content` control SHALL be rendered as an icon-only button (no visible text label)
- **AND** hovering it SHALL show a non-empty tooltip describing content matching
- **AND** when the toggle is ON, the button SHALL render in the platform's selected/pressed state

---

### Requirement: Support regex mode
The UI SHALL provide a `Match pattern` (regex) icon toggle on the EditTrail icon toolbar that interprets the query as a regular expression. The control SHALL be rendered as an icon-only `ToggleAction` with a tooltip describing regex/pattern behaviour and a clear active visual state when on.

#### Scenario: Regex matches correctly
- **WHEN** the `Match pattern` icon toggle is ON
- **AND** the user enters `User(Service|Repository)`
- **THEN** entries with file names matching the pattern SHALL be shown

#### Scenario: Invalid regex is handled safely
- **WHEN** the `Match pattern` icon toggle is ON
- **AND** the user enters an invalid regular expression
- **THEN** the search field SHALL display a non-blocking error indicator (e.g. red border)
- **AND** the plugin SHALL NOT crash

#### Scenario: Match pattern control is an icon toggle with a tooltip
- **WHEN** the icon toolbar is displayed
- **THEN** the `Match pattern` control SHALL be rendered as an icon-only button (no visible text label)
- **AND** hovering it SHALL show a non-empty tooltip explaining regex/pattern behaviour
- **AND** when the toggle is ON, the button SHALL render in the platform's selected/pressed state

---

### Requirement: Support case-sensitive search
The UI SHALL provide a `Case sensitive` icon toggle on the EditTrail icon toolbar. The control SHALL be rendered as an icon-only `ToggleAction` with a tooltip describing its function and a clear active visual state when on.

#### Scenario: Case-sensitive search enabled
- **WHEN** the `Case sensitive` icon toggle is ON
- **AND** the user searches `userservice`
- **THEN** an entry with file name `UserService.cs` SHALL NOT match

#### Scenario: Case-sensitive search disabled
- **WHEN** the `Case sensitive` icon toggle is OFF
- **AND** the user searches `userservice`
- **THEN** an entry with file name `UserService.cs` SHALL match

#### Scenario: Case sensitive control is an icon toggle with a tooltip
- **WHEN** the icon toolbar is displayed
- **THEN** the `Case sensitive` control SHALL be rendered as an icon-only button (no visible text label)
- **AND** hovering it SHALL show a non-empty tooltip describing case-sensitive matching
- **AND** when the toggle is ON, the button SHALL render in the platform's selected/pressed state
