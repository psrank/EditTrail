## ADDED Requirements

### Requirement: Provide a settings page
The plugin SHALL expose a settings page under IDE Settings → Tools → EditTrail.

#### Scenario: Settings page is visible
- **GIVEN** the plugin is installed
- **WHEN** the user opens IDE Settings
- **THEN** an **EditTrail** entry SHALL appear under Tools
- **AND** it SHALL display controls for max history size, default sort mode, and search option persistence
