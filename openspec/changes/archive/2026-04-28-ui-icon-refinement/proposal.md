## Why

The EditTrail tool window currently relies on text-labelled checkboxes (`Match path`, `Match content`, `Match pattern`, `Case sensitive`, `Include all project files`), text-labelled buttons (`Recalculate groups`, `Clear history`), and chips that wrap counts in parentheses. In a narrow IDE tool window this produces visual clutter, breaks line-wrap unpredictably, and feels foreign next to JetBrains' native compact icon toolbars (e.g. Find in Files, Project view). Iteration 6 closed out core functionality, so the UI surface is now stable enough to refine without churn risk.

This change replaces the text-heavy controls with icon-based equivalents using the JetBrains AllIcons set, attaches mandatory tooltips for discoverability, and redesigns the plugin's main brand icon away from the current circle-with-four-dots motif.

## What Changes

- Replace the four search-option checkboxes (`Match path`, `Match content`, `Match pattern`/regex, `Case sensitive`) with compact icon toggle buttons that share a single toolbar row.
- Replace the `Include all project files` checkbox with an icon toggle on the same toolbar row.
- Replace the `Recalculate groups` and `Clear history` text buttons with toolbar icon actions.
- Add a tooltip to every icon control describing its action or current toggle state.
- Render every toggle's active state with a clear visual distinction (selected/pressed appearance) so users can tell which filters are on at a glance.
- Drop the parentheses around chip counts (`C# (8)` → `C# 8`) and keep the `All` chip first; chip toggle behaviour is unchanged.
- Redesign the plugin's main brand icon (the `EditTrail` tool window icon) so it no longer uses the circle-with-four-dots concept and instead evokes file navigation trail / breadcrumb / edit flow, while remaining recognisable at the 13×13 IDE toolbar size.
- **NON-BREAKING**: no functional change to search, grouping, history tracking, or persistence. Toggle states, chip selections, and global-search preference continue to persist exactly as before.

## Capabilities

### New Capabilities
- `icon-toolbar`: Conventions and contract for the EditTrail icon-based toolbar — toggle icons, action icons, active-state visual treatment, and mandatory tooltips on every control.
- `plugin-icon`: Conventions for the redesigned main EditTrail brand icon (concept, motif, and small-size readability).

### Modified Capabilities
- `tool-window`: History panel control layout changes — search-option toggles, global-search toggle, recalculate-groups action, and clear-history action are now rendered as icon toolbar buttons rather than text checkboxes/buttons.
- `search-filter`: The `Match path`, `Match content`, `Regex`/`Match pattern`, and `Case sensitive` controls become icon toggles with tooltips. Filter behaviour and persistence are unchanged.
- `global-search`: The `Include all project files` control becomes an icon toggle with a tooltip. Search behaviour and persistence are unchanged.
- `file-grouper`: The user-facing "Recalculate groups" trigger becomes a toolbar icon action with a tooltip. Background recalculation behaviour is unchanged.
- `clear-history`: The "Clear history" trigger becomes a toolbar icon action with a tooltip. Confirmation flow is unchanged.
- `file-type-filter`: Chip label format drops the parentheses around counts (`C# (8)` → `C# 8`). Selection, multi-select, and `All` chip behaviour are unchanged.

## Impact

- **Affected code (Kotlin):**
  - `src/main/kotlin/.../ui/HistoryPanel.kt` (or equivalent) — replaces `JCheckBox` / `JButton` constructions with `ActionToolbar` / `ToggleAction` / `AnAction` driven controls.
  - The toggle-action classes for path/content/regex/case-sensitive/global-search filters (new `ToggleAction` subclasses or refactored existing handlers).
  - The action classes backing recalculate-groups and clear-history (refactored to plug into an `ActionToolbar`).
  - `FileTypeChipBar` (or equivalent) — chip label rendering drops parentheses.
- **Resources:**
  - `src/main/resources/META-INF/pluginIcon.svg` and `pluginIcon_dark.svg` — redesigned brand icon at 40×40 (Marketplace) and 13×13 (toolbar) sizes.
  - `src/main/resources/META-INF/plugin.xml` — no functional changes; toolbar action group registration may be added.
- **Dependencies:** none — uses existing IntelliJ Platform `AllIcons`, `ToggleAction`, `AnAction`, `ActionToolbar` APIs already pulled in by the plugin.
- **Persistence / data:** none. No state, settings, or history schema changes.
- **Out of scope:** search logic, grouping algorithm, history-tracking logic, settings page contents, sort/order behaviour.
