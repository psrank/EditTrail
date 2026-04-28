## MODIFIED Requirements

### Requirement: Provide global project search toggle
The EditTrail panel SHALL provide an `Include all project files` icon toggle on the EditTrail icon toolbar. The control SHALL be rendered as an icon-only `ToggleAction` with a tooltip describing its behaviour and a clear active visual state when on.

#### Scenario: Toggle disabled by default
- **WHEN** the EditTrail panel is opened for the first time with no saved preference
- **THEN** the `Include all project files` icon toggle SHALL be in the OFF / unselected state

#### Scenario: Toggle state is remembered
- **WHEN** the user enables the icon toggle and closes/reopens the panel
- **THEN** the toggle SHALL remain enabled with its active visual state

#### Scenario: Toggle enabled triggers project scan
- **WHEN** the user enables the `Include all project files` icon toggle
- **AND** the search field contains a non-empty query
- **THEN** EditTrail SHALL include matching project files in the result list

#### Scenario: Empty query suppresses project file results
- **WHEN** the `Include all project files` icon toggle is ON
- **AND** the search query is empty
- **THEN** EditTrail SHALL show only history entries and SHALL NOT enumerate all project files

#### Scenario: Global search control is an icon toggle with a tooltip
- **WHEN** the icon toolbar is displayed
- **THEN** the `Include all project files` control SHALL be rendered as an icon-only button (no visible text label)
- **AND** hovering it SHALL show a non-empty tooltip describing global project search behaviour
- **AND** when the toggle is ON, the button SHALL render in the platform's selected/pressed state
