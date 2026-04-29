# 📄 `iteration-7-ui-icon-refinement.md`

````md
# EditTrail — Iteration 7: UI Icon Refinement & Interaction Simplification

## Type

Change Request

---

## Goal

Refine the EditTrail UI to use compact, icon-based controls with tooltips instead of text-heavy elements.

The goal is to improve usability, reduce visual clutter, and align with JetBrains IDE design patterns while preserving discoverability.

---

## Motivation

The current design relies on:

- text-based toggles (e.g. Match path, Match content)
- explicit buttons (e.g. Recalculate groups, Clear history)
- text-heavy file-type filters

This creates unnecessary visual noise in a narrow tool window.

JetBrains IDEs favour:

- compact icon toolbars
- toggle icons with tooltips
- minimal text with contextual meaning

This change aligns EditTrail with that design language.

---

## Scope

### Included

- Replace search option checkboxes with icon toggles
- Replace action buttons with toolbar icons
- Introduce tooltips for all icons
- Refine file-type filters (icon chips + counts)
- Update main EditTrail icon direction

### Excluded

- Functional changes to search logic
- Functional changes to grouping logic
- Functional changes to history tracking
- New features

---

## Requirements

---

### Requirement: Replace search options with icon toggles

Search options SHALL be represented as compact toggle icons.

#### Scenario: Match path

- GIVEN the search toolbar is visible
- WHEN the UI is rendered
- THEN a `Match path` icon SHALL be displayed
- AND it SHALL behave as a toggle
- AND it SHALL have a tooltip explaining its function

#### Scenario: Match content

- GIVEN the search toolbar is visible
- WHEN the UI is rendered
- THEN a `Match content` icon SHALL be displayed
- AND it SHALL behave as a toggle
- AND it SHALL have a tooltip

#### Scenario: Match pattern

- GIVEN the search toolbar is visible
- WHEN the UI is rendered
- THEN a `Match pattern` icon SHALL be displayed
- AND it SHALL behave as a toggle
- AND it SHALL have a tooltip explaining regex/pattern behavior

#### Scenario: Case sensitive

- GIVEN the search toolbar is visible
- WHEN the UI is rendered
- THEN a `Case sensitive` icon SHALL be displayed
- AND it SHALL behave as a toggle
- AND it SHALL have a tooltip

#### Scenario: Toggle state visibility

- GIVEN a toggle is enabled
- WHEN displayed
- THEN it SHALL have a clear active visual state

---

### Requirement: Replace global search toggle with icon

Global project search SHALL be represented as a toggle icon.

#### Scenario: Global search icon

- GIVEN the toolbar is visible
- WHEN rendered
- THEN an icon SHALL represent `Include all project files`
- AND it SHALL have a tooltip describing its behaviour

---

### Requirement: Replace action buttons with toolbar icons

Actions SHALL be represented as compact toolbar icons.

#### Scenario: Recalculate groups

- GIVEN grouping is enabled
- WHEN the toolbar is displayed
- THEN a `Recalculate groups` icon SHALL be present
- AND it SHALL have a tooltip

#### Scenario: Clear history

- GIVEN history exists
- WHEN the toolbar is displayed
- THEN a `Clear history` icon SHALL be present
- AND it SHALL have a tooltip

---

### Requirement: File-type filters use icon chips

File-type filters SHALL be displayed as compact chips.

#### Scenario: File-type chip structure

- GIVEN file-type filters are displayed
- WHEN rendered
- THEN each chip SHALL:
  - display a label or icon
  - display a count (without brackets)
  - support toggle behaviour

#### Scenario: Example display

```text
[ All 42 ] [ C# 18 ] [ JSON 7 ] [ Razor 3 ]
````

#### Scenario: Toggle behavior

* GIVEN a chip is clicked
* WHEN toggled
* THEN the filter SHALL activate or deactivate

---

### Requirement: Tooltips are mandatory

All icon-based controls SHALL include tooltips.

#### Scenario: Tooltip visibility

* GIVEN any icon control
* WHEN the user hovers over it
* THEN a tooltip SHALL clearly describe the action or toggle

---

### Requirement: Update main EditTrail icon

The plugin icon SHALL be redesigned.

#### Scenario: Avoid current icon

* GIVEN the current icon is a circle with four dots
* WHEN redesigning
* THEN it SHALL NOT use this concept

#### Scenario: New icon direction

The new icon SHOULD represent:

* file navigation trail
* recent activity
* edit flow
* breadcrumb/path

#### Scenario: Small size readability

* GIVEN the icon is rendered in IDE toolbars
* WHEN displayed at small size
* THEN it SHALL remain recognisable

---

## Suggested Icon Set (Non-binding)

| Feature            | Icon idea             |
| ------------------ | --------------------- |
| Match path         | folder/search         |
| Match content      | text/search           |
| Match pattern      | regex or .*           |
| Case sensitive     | Aa                    |
| Global search      | globe or project icon |
| Recalculate groups | refresh/loop          |
| Clear history      | trash/bin             |
| Sort               | sort icon             |

---

## UI Example

```text
[ Search files... ] 🔍

[ path ] [ content ] [ pattern ] [ case ] [ global ]

[ sort ] [ refresh ] [ trash ]

[ All 42 ] [ C# 18 ] [ JSON 7 ] [ Razor 3 ]
```

---

## Acceptance Criteria

* All search options are icon toggles
* All actions are icon-based
* Every icon has a tooltip
* Active states are visually clear
* File-type filters show counts without brackets
* Main plugin icon is redesigned
* UI is more compact and aligned with JetBrains standards

---

## Notes

This change is purely UX-focused and should not alter underlying logic.

It should be implemented after core functionality is stable to avoid UI churn during earlier iterations.
