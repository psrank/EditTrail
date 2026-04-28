## 1. Action classes for toolbar controls

- [x] 1.1 Create `src/main/kotlin/com/psrank/edittrail/actions/MatchPathToggleAction.kt` — a `ToggleAction` whose selected state mirrors the existing `matchPathBox`; on selection change it calls the same `onSearchChange()` flow and persists `EditTrailAppSettings.matchPath` when the persistence opt-in is on.
- [x] 1.2 Create `actions/MatchContentToggleAction.kt` — `ToggleAction` mirroring `matchContentBox`, same persistence rules.
- [x] 1.3 Create `actions/MatchPatternToggleAction.kt` — `ToggleAction` mirroring `regexBox` (also re-runs regex border validation), same persistence rules.
- [x] 1.4 Create `actions/CaseSensitiveToggleAction.kt` — `ToggleAction` mirroring `caseSensitiveBox`, same persistence rules.
- [x] 1.5 Create `actions/GlobalSearchToggleAction.kt` — `ToggleAction` mirroring `globalSearchBox`; on toggle change it calls `EditTrailProjectService.setGlobalSearchEnabled(...)` and triggers `onSearchChange()` (same as today's listener).
- [x] 1.6 Create `actions/RecalculateGroupsAction.kt` — `AnAction` whose `actionPerformed` runs the same pooled-thread block currently inside the `recalcButton` listener.
- [x] 1.7 Create `actions/ClearHistoryAction.kt` — `AnAction` whose `actionPerformed` shows the same `JOptionPane.YES_NO_OPTION` confirmation and calls `EditTrailProjectService.clearHistory()` on confirm.
- [x] 1.8 Each action class above MUST set a non-empty `templatePresentation.description` (used as tooltip) and an `AllIcons` icon picked from the table in `design.md` §D2; verify the icon constant exists in the IntelliJ Platform version pinned by `build.gradle.kts`.

## 2. Wire actions into an icon `ActionToolbar` in `EditTrailPanel`

- [x] 2.1 In [EditTrailPanel.kt](src/main/kotlin/com/psrank/edittrail/EditTrailPanel.kt), introduce a private helper `createIconToolbar(): JComponent` that builds a `DefaultActionGroup` containing (in order): `MatchPathToggleAction`, `MatchContentToggleAction`, `MatchPatternToggleAction`, `CaseSensitiveToggleAction`, a `Separator.getInstance()`, `GlobalSearchToggleAction`, a `Separator.getInstance()`, `RecalculateGroupsAction`, `ClearHistoryAction`.
- [x] 2.2 In `createIconToolbar()`, build the toolbar with `ActionManager.getInstance().createActionToolbar("EditTrail.IconToolbar", group, /* horizontal */ true)` and call `setTargetComponent(this)` so the `ToggleAction.update()` lookups have a context.
- [x] 2.3 Each new action class's `update()` MUST read its selected/enabled state from `EditTrailPanel` (or from the project/app services those actions already consult), so multiple panel instances stay in sync. Use a `DataKey<EditTrailPanel>` registered in the panel's `getData(...)` override, OR pass the panel reference through the action's constructor — pick one and apply it consistently.
- [x] 2.4 Replace the existing `createSortBar()` body so it returns ONLY the sort label + sort combo (recalc and clear buttons MOVE to the icon toolbar); update the `init` north-panel layout to be: sort bar → search bar → icon toolbar → chip bar (still 4 rows in a `GridLayout(4, 1)`, but row 3 is now the icon toolbar component).
- [x] 2.5 Remove `createToggleBar()` and the `JCheckBox` fields (`matchPathBox`, `matchContentBox`, `regexBox`, `caseSensitiveBox`, `globalSearchBox`); their state now lives on the action classes themselves (or on a shared state holder created in step 2.3).
- [x] 2.6 Refactor `currentSearchOptions()`, `onSearchChange()`, and `updateRegexBorder()` to read toggle states from the action classes / state holder instead of the removed checkboxes. The persisted-options restore block in `init` must still apply on panel construction.
- [x] 2.7 Verify by sandbox-launching the IDE that all five toggles flip correctly, the recalc and clear actions run, the global-search toggle persists across panel reopen, and the search results update on every toggle change exactly as before.

## 3. Tooltip and active-state polish

- [x] 3.1 Confirm each action's `templatePresentation.description` is set to the phrasing in `design.md` §D6 (control name + em-dash + clarification, no period). For `ToggleAction`s, the tooltip text MUST NOT change between selected/unselected states.
- [x] 3.2 In an IDE sandbox, hover each icon and confirm the IDE shows the tooltip with the expected text.
- [x] 3.3 In the same sandbox, toggle each filter on and confirm the button visibly switches to the platform's selected/pressed appearance (filled background or pressed look). If a chosen icon doesn't render a clearly distinct selected state, swap it for an alternative `AllIcons` constant that does.

## 4. Chip count format

- [x] 4.1 In `EditTrailPanel.rebuildChipBar(...)`, change the `JToggleButton` label from `"${chip.label} (${chip.count})"` to `"${chip.label} ${chip.count}"` (drop both parentheses, keep the single space).
- [x] 4.2 Update the `All` chip's label to display `"All ${visibleCount}"` (computed from the merged result list before chip filtering is applied) instead of the current text-only `"All"`. The All-chip count tracks the total visible across all types.
- [x] 4.3 If any test asserts on the chip label format using parentheses, update those assertions to the new format.

## 5. Brand icon redesign

- [x] 5.1 Sketch 2–3 trail/breadcrumb concept variants (e.g. three connected file glyphs, breadcrumb arrow + file, dotted path with a file at the end) and pick the one that survives 13×13 best.
- [x] 5.2 Author `src/main/resources/META-INF/pluginIcon.svg` at 40×40 baseline with the chosen concept; ensure stroke widths and gaps survive the 13×13 reduction.
- [x] 5.3 Author `src/main/resources/META-INF/pluginIcon_dark.svg` with the same shapes and dark-theme stroke/fill colours.
- [x] 5.4 If the brand SVG does not reduce cleanly to 13×13, author a dedicated `src/main/resources/icons/editTrailToolWindow.svg` (and `_dark.svg`) variant tuned for that size.
- [x] 5.5 Update [plugin.xml](src/main/resources/META-INF/plugin.xml) `<toolWindow>` registration to set `icon="/icons/editTrailToolWindow.svg"` (only if step 5.4 was needed; otherwise rely on JetBrains' automatic plugin-icon use).
- [x] 5.6 Confirm in the running IDE that the new icon appears in the tool window strip at small size AND in Settings → Plugins at large size, in both Light and Darcula themes, and that it never falls back to the previous circle-with-four-dots motif.

## 6. Tests

- [x] 6.1 Add unit tests for each new action class verifying: tooltip / `templatePresentation.description` is non-empty, toggle actions flip selected state correctly, action classes invoke the same handlers as the old listeners (assert via fakes / spies on `EditTrailProjectService`).
- [x] 6.2 Add a UI-light test (using `LightPlatformTestCase` or the existing test harness) that constructs `EditTrailPanel` and asserts: the icon toolbar component exists between the search field and the chip bar, no `JCheckBox` controls remain in the panel hierarchy for the five filter toggles, no `JButton` named "Recalculate groups" / "Clear history" remains.
- [x] 6.3 Add a chip-label test that asserts the rendered text is `"C# 8"` (not `"C# (8)"`) for a sample chip with label `C#` and count `8`, and `"All 42"` for a sample All chip.
- [x] 6.4 Run the full Gradle test suite (`./gradlew test`) and confirm all existing tests for search filtering, global search, grouping, and clear-history still pass without modification.
