## ADDED Requirements

### Requirement: Provide an icon toolbar for EditTrail panel controls
The EditTrail history panel SHALL render its filter toggles, action buttons, and global-search toggle as a single horizontal `ActionToolbar` row of icon-only controls, placed above the file-type chip bar and below the search field.

#### Scenario: Toolbar exists when the panel is open
- **WHEN** the EditTrail tool window is opened
- **THEN** the panel SHALL render an icon `ActionToolbar` row between the search text field and the file-type chip bar

#### Scenario: Toolbar contains only icon controls
- **WHEN** the icon toolbar is rendered
- **THEN** every control in it SHALL be an icon-only button (no visible text label)

#### Scenario: Toolbar uses native JetBrains action toolbar infrastructure
- **WHEN** the icon toolbar is constructed
- **THEN** it SHALL be created via `ActionManager.getInstance().createActionToolbar(...)` with a `DefaultActionGroup` of `ToggleAction` / `AnAction` instances
- **AND** it SHALL NOT use raw `JCheckBox` or `JButton` controls for its members

---

### Requirement: Toggle controls SHALL show a clear active visual state
Every toggle control on the icon toolbar SHALL render a visually distinct state when its toggle is on, so the user can identify active filters at a glance.

#### Scenario: Inactive toggle
- **GIVEN** a toggle action on the icon toolbar
- **WHEN** the toggle's selected state is `false`
- **THEN** its button SHALL render in the default unselected toolbar appearance

#### Scenario: Active toggle
- **GIVEN** a toggle action on the icon toolbar
- **WHEN** the toggle's selected state is `true`
- **THEN** its button SHALL render in the platform's selected/pressed toolbar appearance, visually distinguishable from the inactive state at a glance

#### Scenario: Active state survives panel rerender
- **GIVEN** one or more toolbar toggles are active
- **WHEN** the history panel rerenders (e.g. on history update or recalculation)
- **THEN** every previously-active toggle SHALL continue to render in its active visual state

---

### Requirement: Every icon control SHALL have a tooltip
Every `ToggleAction` and `AnAction` shown on the EditTrail icon toolbar SHALL define a non-empty tooltip describing the control.

#### Scenario: Tooltip on hover
- **GIVEN** any icon control on the EditTrail toolbar
- **WHEN** the user hovers the pointer over it
- **THEN** the IDE SHALL display a tooltip with non-empty descriptive text

#### Scenario: Tooltip describes the action, not the current state
- **GIVEN** a toggle icon
- **WHEN** the toggle is on, off, or hovered
- **THEN** the tooltip SHALL describe what the control does (e.g. "Match path — search across the relative file path")
- **AND** the tooltip text SHALL NOT change based on the current toggle state

#### Scenario: No control on the toolbar lacks a tooltip
- **WHEN** the icon toolbar is constructed
- **THEN** every action added to the toolbar's action group SHALL have a non-empty `templatePresentation.description` (or equivalent populated in `update()`)

---

### Requirement: Toolbar controls SHALL preserve underlying behaviour
Replacing text controls with icon toolbar actions SHALL NOT alter the underlying logic the controls trigger.

#### Scenario: Toggle change re-runs the search
- **GIVEN** any search-option toggle on the icon toolbar
- **WHEN** the user activates or deactivates the toggle
- **THEN** the search results SHALL be recomputed using the new toggle state, exactly as they were before this change

#### Scenario: Action invocation calls the same handler
- **GIVEN** an action button on the icon toolbar (recalculate groups, clear history)
- **WHEN** the user clicks the button
- **THEN** the same handler that was previously invoked by the text button SHALL run, with no change in side effects

#### Scenario: Persisted state is unchanged
- **WHEN** the icon toolbar is introduced
- **THEN** the persisted shape of toggle states, chip selections, and global-search preference SHALL remain identical to before this change
- **AND** existing user preferences SHALL load without migration
