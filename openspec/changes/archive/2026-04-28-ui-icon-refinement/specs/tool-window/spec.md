## MODIFIED Requirements

### Requirement: Provide an EditTrail tool window
The plugin SHALL provide a project-level tool window named `EditTrail` that renders the file history list with an integrated search bar, an icon toolbar of search-option toggles and actions, a file-type chip bar, and a redesigned tool window icon.

#### Scenario: Tool window is visible after project opens
- **WHEN** the user opens a project with the EditTrail plugin installed
- **THEN** the IDE SHALL expose an `EditTrail` tool window in the tool window bar

#### Scenario: Tool window contains the history panel
- **WHEN** the user activates the EditTrail tool window
- **THEN** the tool window SHALL display the history panel as its content

#### Scenario: History panel includes a search bar
- **WHEN** the user activates the EditTrail tool window
- **THEN** the history panel SHALL display a search text field above the icon toolbar

#### Scenario: History panel includes an icon toolbar of search options and actions
- **WHEN** the user activates the EditTrail tool window
- **THEN** the history panel SHALL display a single-row icon `ActionToolbar` between the search field and the file-type chip bar
- **AND** the toolbar SHALL contain icon toggles for `Match path`, `Match content`, `Match pattern` (regex), `Case sensitive`, and `Include all project files`
- **AND** the toolbar SHALL contain icon actions for `Recalculate groups` and `Clear history`

#### Scenario: History panel includes a file-type chip bar
- **WHEN** the user activates the EditTrail tool window
- **THEN** the history panel SHALL display a file-type chip bar between the icon toolbar and the history list

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

#### Scenario: Clear history action prompts for confirmation
- **WHEN** the user clicks the `Clear history` icon action on the toolbar
- **THEN** a confirmation dialog SHALL be shown before any history is removed

#### Scenario: Tool window header uses the redesigned brand icon
- **WHEN** the EditTrail tool window appears in the tool window bar
- **THEN** its header icon SHALL be the redesigned brand icon
- **AND** the icon SHALL NOT use the previous circle-with-four-dots motif
