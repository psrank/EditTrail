## MODIFIED Requirements

### Requirement: Provide an EditTrail tool window
The plugin SHALL provide a project-level tool window named `EditTrail` that renders the file history list with an integrated search bar, file-type chip bar, and global project search toggle.

#### Scenario: Tool window is visible after project opens
- **WHEN** the user opens a project with the EditTrail plugin installed
- **THEN** the IDE SHALL expose an `EditTrail` tool window in the tool window bar

#### Scenario: Tool window contains the history panel
- **WHEN** the user activates the EditTrail tool window
- **THEN** the tool window SHALL display the history panel as its content

#### Scenario: History panel includes a search bar
- **WHEN** the user activates the EditTrail tool window
- **THEN** the history panel SHALL display a search text field and filter toggles above the history list

#### Scenario: History panel includes a file-type chip bar
- **WHEN** the user activates the EditTrail tool window
- **THEN** the history panel SHALL display a file-type chip bar between the search controls and the history list

#### Scenario: History panel includes a global project search toggle
- **WHEN** the user activates the EditTrail tool window
- **THEN** the history panel SHALL display an `Include all project files` toggle in the search control row

#### Scenario: Empty state is shown when no history exists
- **WHEN** the EditTrail tool window is opened and the project has no recorded history
- **THEN** the panel SHALL display an empty-state message explaining that recently viewed and edited files will appear there

#### Scenario: Opening a file from the history list
- **WHEN** the user clicks or presses Enter on a history item in the tool window
- **THEN** the IDE SHALL open that file in the editor

#### Scenario: Missing file in history list
- **WHEN** a history entry refers to a file that no longer exists on disk
- **THEN** EditTrail SHALL NOT crash
- **AND** the entry SHALL be marked unavailable or removed from the visible list
