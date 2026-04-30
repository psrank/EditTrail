---
name: "jetbrains-ui-icon-conventions"
description: "Apply JetBrains-aligned UI conventions to EditTrail tool-window code: icon toggles instead of text checkboxes, mandatory tooltips, compact ActionToolbar layout, icon chips with bare counts, clear active states, and AllIcons usage. Use when adding or modifying UI in src/main/kotlin/com/psrank/edittrail/ (panels, ToggleActions, AnActions, chips, renderers), or when reviewing UI for JetBrains design alignment. Triggers on: 'add a toolbar icon', 'replace this text toggle with an icon', 'review my UI for JetBrains conventions', 'add a tooltip', 'make this UI compact'."
argument-hint: "Optional: a UI file path or feature name to focus the review (e.g. 'EditTrailPanel.kt' or 'sort menu')."
compatibility: "EditTrail IntelliJ plugin (Kotlin + IntelliJ Platform). Conventions are sourced from spec-backlog/iteration-7-ui-icon-refinement.md and the archived OpenSpec change at openspec/changes/archive/2026-04-28-ui-icon-refinement/."
metadata:
  author: "local"
  source: "local/skills/jetbrains-ui-icon-conventions"
  version: "1.0"
user-invocable: true
disable-model-invocation: false
---

## User Input

```text
$ARGUMENTS
```

## Source of truth

These conventions are not invented here — they are codified from:

- `spec-backlog/iteration-7-ui-icon-refinement.md` — the original change request.
- `openspec/changes/archive/2026-04-28-ui-icon-refinement/` — the implemented change (specs under `specs/icon-toolbar/`, `specs/file-type-filter/`, etc.).
- The implementation that landed in `src/main/kotlin/com/psrank/edittrail/`:
  - `actions/MatchPathToggleAction.kt`, `MatchContentToggleAction.kt`, `MatchPatternToggleAction.kt`, `CaseSensitiveToggleAction.kt`, `GlobalSearchToggleAction.kt` — icon `ToggleAction` pattern.
  - `actions/ClearHistoryAction.kt`, `RecalculateGroupsAction.kt` — icon `AnAction` pattern.
  - `ChipButton.kt`, `FileTypeChip.kt`, `FileTypeChipIcon.kt`, `FileTypeChipLabel.kt` — chip pattern with bare counts.
  - `EditTrailPanel.kt` — toolbar wiring via `ActionManager.createActionToolbar`.

When in doubt, mirror those files. Do not invent a new pattern when a sibling already exists.

## Core conventions

### 1. Icon toggles, not text checkboxes

Search options and any binary state SHALL be `ToggleAction` subclasses with an `AllIcons` (or bundled SVG) icon — not `JCheckBox` with a label.

- Set `templatePresentation.text` (becomes the tooltip title) and `templatePresentation.description` (becomes the tooltip body / status-bar hint).
- Implement `isSelected` and `setSelected` against the toolbar state holder (`EditTrailToolbarState` or equivalent).
- Override `getActionUpdateThread()` to return `ActionUpdateThread.EDT` for UI-state-reading actions (or `BGT` if the action only reads project services).

### 2. Tooltips are mandatory

Every icon-based control MUST have a tooltip. No exceptions.

- For `AnAction` / `ToggleAction`: tooltip = `templatePresentation.text` + `templatePresentation.description`. Both should be filled in.
- For custom Swing components (e.g. `ChipButton`): set `toolTipText` in the constructor or initializer.

If you add a new icon and skip the tooltip, the spec is violated — fix it before merging.

### 3. Compact toolbar via ActionGroup + ActionManager

Toolbars SHALL be built from a `DefaultActionGroup` rendered via `ActionManager.getInstance().createActionToolbar(place, group, horizontal)`.

- Use a stable `place` string scoped to EditTrail (e.g. `"EditTrail.SearchToolbar"`).
- Call `setTargetComponent(...)` so action update logic resolves the right context.
- Do NOT hand-roll a `JPanel` of `JButton`s for toolbar actions — use the IntelliJ ActionToolbar so it inherits theme, focus, and accessibility.

### 4. Action buttons → toolbar icons

Operational actions (`Recalculate groups`, `Clear history`, future actions) SHALL be `AnAction` with an icon — not `JButton` with a label.

- Pick an `AllIcons` constant where one fits (`AllIcons.Actions.Refresh`, `AllIcons.Actions.GC`, `AllIcons.Actions.GroupBy`, etc.).
- Only ship a custom SVG when no AllIcons match; place it under `src/main/resources/icons/` and load via `IconLoader.getIcon("/icons/foo.svg", javaClass)`.

### 5. File-type filters as icon chips with bare counts

File-type filters SHALL be rendered as `ChipButton` (or a sibling chip component), not `JCheckBox` or `JToggleButton` with text labels.

- Display: optional icon + label + count. No brackets around the count: `C# 18`, not `C# (18)` or `[C# 18]`.
- Toggle behavior on click; active state visually distinct from inactive.
- Reuse `FileTypeChipIcon` / `FileTypeChipLabel` for rendering — do not duplicate.

### 6. Active state must be obvious

A selected toggle / pressed chip MUST be visually distinct from an unselected one at a glance.

- For `ToggleAction`: the IntelliJ Platform handles active-state styling automatically when `isSelected` is correct — do not override the painter unless needed.
- For custom chips: use the existing `ChipButton` selected/hover styling. If you change it, change it in one place (`ChipButton.kt`) and verify all callers.

### 7. Use AllIcons first

Prefer `com.intellij.icons.AllIcons` over custom SVGs. The JetBrains icon set is theme-aware (light/dark/high-contrast), accessibility-tested, and consistent with the rest of the IDE.

Common picks for EditTrail-shaped features:

| Feature             | AllIcons candidate                         |
| ------------------- | ------------------------------------------ |
| Match path          | `AllIcons.Actions.MenuPaste` / `Find`      |
| Match content       | `AllIcons.Actions.Find`                    |
| Match pattern       | `AllIcons.Actions.Regex`                   |
| Case sensitive      | `AllIcons.Actions.MatchCase`               |
| Global search       | `AllIcons.Actions.FindEntireFile` / `Show` |
| Recalculate groups  | `AllIcons.Actions.Refresh`                 |
| Clear history       | `AllIcons.Actions.GC`                      |
| Sort                | `AllIcons.ObjectBrowser.Sorted`            |

Treat the table as a starting point — check `AllIcons` for the closest semantic match, not just the closest visual one.

## Workflow when this skill is invoked

1. **Identify the surface**: read the file(s) named in `$ARGUMENTS`, or the UI file the user is editing. If unclear, list candidate files under `src/main/kotlin/com/psrank/edittrail/` and ask which one.
2. **Run the checklist** below against the surface and produce a concrete list of violations + fixes. One bullet per issue; cite file:line.
3. **Implement fixes** if the user asks — mirror the patterns in the reference files listed under "Source of truth". Don't introduce a new abstraction when a sibling already exists.
4. **Verify**: build with `./gradlew buildPlugin` (or `gradlew.bat buildPlugin` on Windows shells) and, where feasible, run the IDE sandbox to confirm tooltips appear and active states render correctly. If you can't run the IDE, say so explicitly.

## Review checklist

- [ ] Every icon control has a tooltip (`templatePresentation.text` + `description`, or `toolTipText`).
- [ ] No `JCheckBox` / `JToggleButton` with text label is used for a binary state that has an icon equivalent.
- [ ] No `JButton` with text label is used for an action that has an icon equivalent.
- [ ] Toolbars are built via `ActionManager.createActionToolbar`, not hand-rolled `JPanel`s of buttons.
- [ ] Toolbar `place` string is unique and scoped (`"EditTrail.*"`).
- [ ] `setTargetComponent(...)` is called on the toolbar.
- [ ] File-type chips display counts without brackets (`C# 18`, not `[C# 18]`).
- [ ] Active / selected state is visually distinct.
- [ ] Icons come from `AllIcons` where a reasonable match exists; custom SVGs live under `src/main/resources/icons/` and are loaded via `IconLoader`.
- [ ] `getActionUpdateThread()` is overridden on every new `AnAction` / `ToggleAction`.
- [ ] No functional logic changes ride along with a UX-only change — keep the diff scoped.

## Anti-patterns (do not ship)

- A `JCheckBox("Match path")` next to the search field. → Convert to `MatchPathToggleAction`-style icon toggle.
- A toolbar icon with no `description` set. → Tooltip will be empty in some themes; fill `description`.
- A custom 16x16 PNG checked into `resources/` for a behavior `AllIcons` already covers. → Use `AllIcons`.
- File-type filter rendered as `JCheckBox("C# (18)")`. → Use `ChipButton` / `FileTypeChip` with `C# 18`.
- A new toolbar built as `JPanel(FlowLayout()).apply { add(JButton("Refresh")) }`. → Build a `DefaultActionGroup` and an `ActionToolbar`.
- An action class without `getActionUpdateThread()` override → causes deprecation warnings on current platform versions.

## Out of scope

- Functional changes (search algorithm, grouping logic, history persistence). This skill is UX-only, matching iteration-7's exclusion list.
- Redesigning the main plugin icon (`pluginIcon.svg`) — that is a one-shot artwork task, not a recurring code convention.
- Settings page layout — covered separately by the settings/configurable conventions; out of scope here unless the settings UI itself is adding new icon toolbars.
