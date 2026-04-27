## Context

`FileHistoryEntry` objects are collected by `DocumentEditListener` and `EditorSelectionListener`, stored in `FileHistoryRepository`, and rendered by `FileHistoryCellRenderer` inside `EditTrailPanel`. There is currently no concept of grouping between entries. Each entry is independent.

The IntelliJ Platform imposes a strict rule: long-running or expensive operations must not run on the Event Dispatch Thread (EDT). Group assignment must run off-EDT and post results back on the EDT via `invokeLater`.

## Goals / Non-Goals

**Goals:**
- Co-occurrence heuristic assigns group IDs based on activity signals (time proximity, path proximity, extension)
- Group IDs are session-scoped (not persisted) and recalculated at startup and on demand
- Cell renderer paints a 4 px coloured vertical bar on the left of grouped rows
- Colour per group is stable within a session (same group ID → same colour until IDE restart)
- Grouping failure silently falls back to ungrouped rendering
- Panel exposes a "Recalculate groups" button in the sort bar

**Non-Goals:**
- K-means or other ML clustering
- Dependency graph or semantic analysis
- Manual group editing or naming by the user
- Persisting group assignments across IDE restarts
- Grouping of project file results (global search rows)

## Decisions

### D1 — Session-scoped `groupId: Int?` on `FileHistoryEntry`
**Decision:** Add a nullable `@Transient`-equivalent `var groupId: Int? = null` field to `FileHistoryEntry`. Mark it transient by exclusion from `FileHistoryState` serialisation (the field is not inside `FileHistoryState`'s persistence — `FileHistoryEntry` is serialised via `FileHistoryState.entries`, so the new field simply needs `@get:Transient` or to be ignored by the XmlSerializer).

**Rationale:** Keeping groupId on the entry is the simplest approach. It lets the renderer and any future logic read the group without lookup tables. It is reset to `null` each session and repopulated by `FileGrouper`.

**Alternative considered:** A separate `Map<String, Int>` (url → groupId) stored in the panel — rejected because it requires synchronisation with the entry list and introduces a second source of truth.

### D2 — Scoring-based connected-component grouping in `FileGrouper`
**Decision:** `FileGrouper.assignGroups(entries: List<FileHistoryEntry>)` computes a pairwise score for every entry pair using four signals:
1. **Time proximity** (0–3 pts): entries last-viewed within 5 min → 3, 30 min → 2, 2 h → 1, else 0
2. **Path prefix** (0–2 pts): same parent directory → 2, same grandparent → 1
3. **Extension match** (0–1 pt): identical extension → 1
4. **Repeated co-occurrence** (0–2 pts): not implemented in v1, reserved slot defaulting to 0

Pairs whose total score ≥ 4 are considered connected. Groups are formed by connected components (union-find). Groups of size 1 (isolated entries) get `groupId = null`.

**Rationale:** Weighted scoring with a threshold is deterministic, fast (O(n²) for n ≤ 500), and easy to tune. Connected components ensure transitive grouping.

**Alternative considered:** Hierarchical clustering — rejected as unnecessarily complex for the first implementation.

### D3 — Background execution with `executeOnPooledThread`
**Decision:** `EditTrailPanel.refresh()` dispatches `FileGrouper.assignGroups()` on a pooled thread after the history is loaded. On completion it calls `invokeLater` to update `groupId` fields and trigger a list repaint.

**Rationale:** Consistent with the approach used for global project search in iteration 004. Pooled threads are the lightest IntelliJ-idiomatic off-EDT mechanism.

### D4 — Stable colour palette via `GroupColourPalette`
**Decision:** A singleton (or panel-level) `GroupColourPalette` maps group IDs to colours from a fixed cycle of 8 IntelliJ-friendly colours. The map is not cleared between refreshes — only a full IDE restart resets it.

**Rationale:** Stable colours reduce cognitive load. A fixed palette of 8 is sufficient for most practical grouping scenarios (n ≤ 8 groups visible simultaneously).

### D5 — Custom painting in `FileHistoryCellRenderer`
**Decision:** Override `paintComponent` in `FileHistoryCellRenderer` (or use a wrapping panel). Draw a 4 px filled rectangle on the left edge in the group colour when `groupId != null`. The rest of the rendering is unchanged.

**Alternative considered:** Embedding a coloured `JComponent` as a border — rejected because it interferes with the existing `DefaultListCellRenderer` HTML text layout.

## Risks / Trade-offs

- [Risk] O(n²) scoring on large histories (n = 500) → ~125 000 comparisons. Each comparison is cheap (integer arithmetic), so at 500 entries this should complete in < 5 ms. Mitigation: cap scoring at n = 200 entries if performance degrades.
- [Risk] XmlSerializer may attempt to serialise `groupId` → Mitigation: use `@Transient` annotation or ensure `groupId` is a `var` initialised to `null` and excluded from state class.
- [Risk] Colour palette may clash with custom IntelliJ themes → Mitigation: use `JBColor` pairs that adapt to light/dark mode.
- [Risk] Group IDs shift between recalculations, changing colours → Mitigation: acceptable for v1; the Non-Goals explicitly exclude cross-session stability.
