## ADDED Requirements

### Requirement: Redesign the EditTrail brand icon
The plugin SHALL ship a redesigned brand icon (used as the IDE plugin icon and tool window icon) that no longer uses the previous circle-with-four-dots motif.

#### Scenario: Old motif retired
- **WHEN** any version of the EditTrail brand icon is rendered after this change
- **THEN** the icon SHALL NOT depict a circle containing four dots

#### Scenario: New motif evokes navigation trail or edit flow
- **WHEN** the new brand icon is reviewed
- **THEN** its visual concept SHALL evoke at least one of: file navigation trail, recent activity, edit flow, or breadcrumb/path

---

### Requirement: Brand icon ships in light and dark variants
The plugin SHALL provide both light-theme and dark-theme variants of the brand icon.

#### Scenario: Light theme variant exists
- **WHEN** the plugin resources are inspected
- **THEN** a light-theme brand icon SHALL exist at `src/main/resources/META-INF/pluginIcon.svg`

#### Scenario: Dark theme variant exists
- **WHEN** the plugin resources are inspected
- **THEN** a dark-theme brand icon SHALL exist at `src/main/resources/META-INF/pluginIcon_dark.svg`

#### Scenario: Variants share a single visual concept
- **WHEN** the light and dark variants are compared
- **THEN** they SHALL depict the same visual concept (only stroke / fill colour differs)

---

### Requirement: Brand icon remains readable at small toolbar size
The brand icon SHALL remain recognisable when rendered at IDE toolbar size.

#### Scenario: Readable at 13×13
- **WHEN** the icon is rendered at the standard JetBrains tool window header size (13×13 logical pixels)
- **THEN** its silhouette SHALL remain distinguishable as the chosen motif and SHALL NOT degrade into an indistinct blob

#### Scenario: Readable at 40×40
- **WHEN** the icon is rendered at Marketplace listing size (40×40 logical pixels)
- **THEN** the icon SHALL appear cleanly without missing or clipped elements
