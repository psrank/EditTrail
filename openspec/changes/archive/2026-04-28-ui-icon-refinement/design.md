## Context

The EditTrail tool window assembles its UI from raw Swing primitives — `JCheckBox` for the four search-option toggles plus the global-search toggle, `JButton` for `Recalculate groups` and `Clear history`, and a custom chip component for the file-type filter row. Every control carries a visible text label. The result is functional but visually noisy in narrow IDE tool windows and stylistically off-key next to JetBrains' native panels (Find in Files, Project view, Commit), which exclusively use the `ActionToolbar` + `AnAction`/`ToggleAction` pattern with `AllIcons` glyphs and tooltips.

Iteration 6 (settings-and-polish) closed out the last functional capability, so the surface is now stable enough to refactor presentation without thrashing earlier work. The plugin already depends on the IntelliJ Platform SDK that ships `ActionToolbar`, `AllIcons`, and the `ToggleAction` base class, so this change requires no new dependencies.

The plugin's brand icon (`pluginIcon.svg`) is currently a circle with four dots. The user has explicitly rejected that motif and asked for an icon that evokes a navigation trail / breadcrumb / edit flow.

## Goals / Non-Goals

**Goals:**
- Replace every text-labelled control on the EditTrail toolbar row(s) with an icon-driven equivalent, using `AllIcons` glyphs and standard JetBrains `ActionToolbar` infrastructure.
- Attach a tooltip to every icon control so the action remains discoverable without text labels.
- Render toggle controls with a clear active visual state so users can tell at a glance which filters are on.
- Drop the parentheses around chip counts in the file-type filter bar (presentation-only).
- Redesign the brand icon away from the circle-with-four-dots motif into a trail/breadcrumb concept that survives 13×13 IDE-toolbar rendering.
- Preserve every existing behaviour: search filtering, global search, grouping, clear-history confirmation, chip selection logic, and persisted preferences must work bit-for-bit identically after the refactor.

**Non-Goals:**
- No changes to `SearchEngine`, `FileGrouper`, `HistoryStore`, `GlobalSearchService`, or any non-UI logic.
- No changes to settings-page contents or the persisted shape of toggle/chip state.
- No new functionality, new toggles, or new actions.
- No refactor of the chip widget beyond the label-format tweak.
- No changes to keyboard shortcuts or the way history items are opened.

## Decisions

### D1. Use the JetBrains `ActionToolbar` + `ToggleAction`/`AnAction` pattern, not custom Swing
**Choice:** Refactor the search-option toggles, the global-search toggle, the recalculate-groups action, and the clear-history action into `ToggleAction` / `AnAction` subclasses, group them in `DefaultActionGroup`s, and render via `ActionManager.getInstance().createActionToolbar(...)`.

**Rationale:**
- `ActionToolbar` automatically renders compact icon buttons sized to match native IDE toolbars and applies the platform's selected/hover/pressed visual treatment for free, satisfying the "clear active visual state" requirement without custom paint code.
- Tooltips are read from each action's `templatePresentation.description` (or computed in `update()`), giving us the mandatory-tooltip requirement without separate `setToolTipText` plumbing.
- This is the same pattern every other JetBrains tool window uses, so the result will look and feel native.
- Consolidating state plumbing in `update()` overrides keeps toggle persistence and event firing in one place per control.

**Alternatives considered:**
- Keep `JCheckBox` and `JButton` and only swap labels for icons via `setIcon` / `setText("")`. Rejected — checkboxes don't render as compact icon toolbar buttons in JetBrains' look-and-feel, and we'd still have to write custom selected-state paint to match native behaviour.
- Build a fully custom icon-button widget. Rejected — reinvents what `ActionToolbar` already provides and drifts from JetBrains conventions.

### D2. Icon mapping (uses `AllIcons` only, no custom glyphs)

| Control                     | Icon                                          | Tooltip text                                              |
|-----------------------------|-----------------------------------------------|-----------------------------------------------------------|
| Match path                  | `AllIcons.Actions.GroupByFile` (folder/path)  | "Match path — search across the relative file path"      |
| Match content               | `AllIcons.Actions.Find` (text/search)         | "Match content — search inside file contents"            |
| Match pattern (regex)       | `AllIcons.Actions.Regex`                      | "Match pattern — interpret query as regex"               |
| Case sensitive              | `AllIcons.Actions.MatchCase`                  | "Case sensitive"                                          |
| Include all project files   | `AllIcons.General.ProjectStructure` or `Globe`| "Include all project files in search results"            |
| Recalculate groups          | `AllIcons.Actions.Refresh`                    | "Recalculate file groups"                                 |
| Clear history               | `AllIcons.General.RemoveJdk` or `Actions.GC`  | "Clear history…"                                          |

The exact `AllIcons` constant is finalised during implementation by previewing in the running IDE; the table above is the binding starting point. Any substitution stays within `AllIcons` (no custom SVG glyphs for toolbar controls — only the brand icon is custom).

**Rationale:** `AllIcons` glyphs are theme-aware (light/dark), HiDPI-correct, and instantly readable by any JetBrains user. Custom toolbar icons would require maintaining two SVGs each and would clash with the rest of the IDE.

### D3. Layout — single `ActionToolbar` row above the search field; chip bar unchanged below

```
[ Search files... ]                       <- JBTextField (unchanged)
[ path ][ content ][ regex ][ Aa ][ globe ][ refresh ][ trash ]   <- new ActionToolbar
[ All 42 ][ C# 18 ][ JSON 7 ][ Razor 3 ]   <- existing chip bar, label format tweaked
[ history list ... ]                       <- unchanged
```

**Rationale:** A single horizontal toolbar row above the chip bar keeps the panel compact, mirrors `Find in Files` layout, and groups all icon controls together. Toggle filters come first (left), then global-search separator, then action buttons (right side or end of row). Order is fixed in code, not user-configurable in this iteration.

**Alternatives considered:**
- Split toggles and actions onto two separate rows. Rejected — wastes vertical space in narrow tool windows.
- Inline the toolbar inside the search field's trailing component slot. Rejected — too cramped for 7 icons; doesn't match Find in Files.

### D4. Chip count format — drop parentheses, keep space separator

`C# (8)` → `C# 8`. Done in the chip's label rendering only (no model change). The `All` chip becomes `All 42` from `All (42)`.

**Rationale:** Minimal change, matches the spec example, and a single-character separator is enough visual delimiter.

### D5. Brand icon redesign — breadcrumb / trail motif at two sizes

- **Concept:** A short horizontal trail of three connected file-glyphs (or three dots → arrow → file) suggesting a navigation breadcrumb. The exact composition is finalised at implementation time by sketching a few variants and picking the one that survives 13×13.
- **Required deliverables:**
  - `src/main/resources/META-INF/pluginIcon.svg` — 40×40 baseline (Marketplace).
  - `src/main/resources/META-INF/pluginIcon_dark.svg` — same shape, dark-theme stroke/fill.
  - `src/main/resources/icons/editTrailToolWindow.svg` — 13×13 toolbar variant if the brand icon doesn't reduce cleanly (otherwise reuse `pluginIcon.svg` via JetBrains' automatic scaling).
- **Hard constraints:**
  - MUST NOT use the circle-with-four-dots motif.
  - MUST remain recognisable at 13×13 (toolbar) without becoming a blob.
  - MUST work in both light and dark IDE themes (the `_dark.svg` variant covers this).

**Rationale:** A breadcrumb / trail directly visualises "edit trail" — the plugin's name and core idea. Three-element compositions survive small sizes better than detailed scenes.

**Alternatives considered:**
- A single arrow or pencil glyph. Rejected — too generic; doesn't convey "trail" or "history".
- A clock-with-files icon. Rejected — overlaps semantically with IDE recent-files; doesn't say "edit flow".

### D6. Tooltip phrasing — short imperative + clarification, no period

Each tooltip starts with the control name followed by a one-clause clarification, e.g. `"Match path — search across the relative file path"`. Toggles do not flip wording when active (we rely on the visual selected state to communicate state, per `D1`); the wording always describes what the control does, not its current state. This avoids the awkward "Click to enable…" / "Click to disable…" rewrite churn and matches JetBrains conventions.

## Risks / Trade-offs

- **Risk:** Discoverability drops without text labels for users new to JetBrains plugins. → **Mitigation:** Tooltip on every icon (mandatory, enforced by spec); icon glyphs are pulled from `AllIcons` so they match conventions users already know from Find in Files.
- **Risk:** A chosen `AllIcons` constant may not exist in older IntelliJ Platform versions targeted by `build.gradle.kts`. → **Mitigation:** Pick icons present in the platform version pinned by `intellijPlatform { ... }`; verify by sandbox-launching the plugin once during implementation.
- **Risk:** The `ActionToolbar` refactor accidentally drops behaviour wired into the existing checkboxes/buttons (e.g. the clear-history confirmation, the search re-trigger on toggle change). → **Mitigation:** Each `ToggleAction.setSelected()` / `AnAction.actionPerformed()` MUST call the same underlying handler the old `ItemListener` / `ActionListener` called; existing tests for search, global search, grouping, and clear-history MUST continue to pass without modification.
- **Risk:** The new brand icon looks fine at 40×40 but turns into mush at 13×13. → **Mitigation:** Design the 13×13 variant first; only scale up to 40×40 once the small size reads cleanly. If the brand SVG cannot reduce cleanly, ship a dedicated 13×13 toolbar variant per `D5`.
- **Trade-off:** Active-state visibility now depends entirely on JetBrains' selected-toolbar-button paint. If a future LAF change weakens that contrast, our toggles become hard to read. We accept this — it's the same trade-off every native tool window makes.
- **Trade-off:** Tooltip text becomes the discoverability story for new users; we must keep that text accurate as behaviour evolves. We accept this — a single-line description is cheaper to maintain than a permanent visual label.
