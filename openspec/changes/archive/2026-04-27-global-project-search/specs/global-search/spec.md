## ADDED Requirements

### Requirement: Provide global project search toggle
The EditTrail panel SHALL provide an `Include all project files` toggle control.

#### Scenario: Toggle disabled by default
- **WHEN** the EditTrail panel is opened for the first time with no saved preference
- **THEN** the `Include all project files` toggle SHALL be disabled

#### Scenario: Toggle state is remembered
- **WHEN** the user enables the toggle and closes/reopens the panel
- **THEN** the toggle SHALL remain enabled

#### Scenario: Toggle enabled triggers project scan
- **WHEN** the user enables `Include all project files`
- **AND** the search field contains a non-empty query
- **THEN** EditTrail SHALL include matching project files in the result list

#### Scenario: Empty query suppresses project file results
- **WHEN** `Include all project files` is enabled
- **AND** the search query is empty
- **THEN** EditTrail SHALL show only history entries and SHALL NOT enumerate all project files

---

### Requirement: Search project files by file name
When global search is enabled and a non-empty query is present, the plugin SHALL include matching project virtual files that are not already in history.

#### Scenario: Non-history file matches query
- **WHEN** `InvoiceService.cs` exists in the project and is not in history
- **AND** global search is enabled
- **AND** the user searches `invoice`
- **THEN** `InvoiceService.cs` SHALL appear in the result list

#### Scenario: History file is not duplicated
- **WHEN** `UserService.cs` is already in history
- **AND** global search is enabled
- **AND** the user searches `user`
- **THEN** `UserService.cs` SHALL appear exactly once in the result list (as a history entry)

#### Scenario: Query change re-triggers scan
- **WHEN** the user modifies the search query while global search is enabled
- **THEN** the project file results SHALL update to reflect the new query

---

### Requirement: Prioritise history entries in results
History entries SHALL appear before non-history global search results.

#### Scenario: History entry appears before project result
- **WHEN** `UserService.cs` is in history and `UserSettingsService.cs` is not
- **AND** global search is enabled
- **AND** the user searches `user`
- **THEN** `UserService.cs` SHALL appear above `UserSettingsService.cs` in the list

---

### Requirement: Open global project result and add to history
The user SHALL be able to open a non-history project file from the result list.

#### Scenario: Click opens non-history file
- **WHEN** a non-history project file appears in the result list
- **AND** the user clicks it or presses Enter on it
- **THEN** the IDE SHALL open the file in the editor

#### Scenario: Opened project file is added to history
- **WHEN** the user opens a non-history project file from the global search results
- **THEN** the file SHALL be added to EditTrail history
- **AND** its `lastViewedAt` SHALL be set to the current time

---

### Requirement: Combine global search with file-type filters
Global project search results SHALL respect the active file-type chip bar selection.

#### Scenario: File-type filter applies to project results
- **WHEN** `Include all project files` is enabled
- **AND** the `JSON` chip is selected
- **AND** the user searches `settings`
- **THEN** only JSON history entries and JSON project files SHALL appear

---

### Requirement: Keep UI responsive during project scan
Project file enumeration SHALL not block the EDT.

#### Scenario: Large project remains responsive
- **WHEN** global search is enabled on a large project
- **AND** the user types a query
- **THEN** the UI SHALL remain responsive while results are loading
