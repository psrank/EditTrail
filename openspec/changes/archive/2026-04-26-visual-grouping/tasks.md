## 1. Data model

- [x] 1.1 Add `var groupId: Int? = null` field to `FileHistoryEntry` (session-scoped; not persisted — verify XmlSerializer ignores it by default since it is not in `FileHistoryState`)

## 2. File grouper

- [x] 2.1 Create `FileGrouper.kt` with a single public function `assignGroups(entries: List<FileHistoryEntry>)` that mutates `groupId` on each entry in-place
- [x] 2.2 Implement time-proximity scoring (0–3 pts): entries whose `lastViewedAt` timestamps are within 5 min → 3, 30 min → 2, 2 h → 1, else 0; use `max(lastViewedAt, lastEditedAt)` as the representative timestamp
- [x] 2.3 Implement path-prefix scoring (0–2 pts): same parent directory → 2, same grandparent directory → 1 (derived from `relativePath`)
- [x] 2.4 Implement extension-match scoring (0–1 pt): identical file extension → 1
- [x] 2.5 For every pair of entries whose combined score ≥ 4, mark them as connected; assign group IDs via union-find (connected components)
- [x] 2.6 Set `groupId = null` for any entry that ends up in a component of size 1 (no meaningful group)
- [x] 2.7 Wrap the entire `assignGroups` body in try/catch so any exception leaves all entries with their previous `groupId` values and does not propagate

## 3. Group colour palette

- [x] 3.1 Create `GroupColourPalette.kt` as an `object` (singleton) with a `colourFor(groupId: Int): Color` function
- [x] 3.2 Define a palette of 8 `JBColor` instances (warm/cool alternating, light/dark mode aware)
- [x] 3.3 Maintain a `MutableMap<Int, Color>` that assigns colours lazily on first access and reuses them on subsequent calls

## 4. Cell renderer

- [x] 4.1 Override `paintComponent(g: Graphics)` in `FileHistoryCellRenderer`; call `super.paintComponent(g)` first, then, if the current value is an `EditTrailResult.HistoryResult` with a non-null `groupId`, fill a 4 px wide rectangle on the left edge in the group colour from `GroupColourPalette`
- [x] 4.2 Ensure the existing text layout is not displaced: add 4 px left inset to the renderer panel so the HTML text does not overlap the coloured bar

## 5. Panel integration

- [x] 5.1 After loading history in `refreshNamePathOnly()`, dispatch `FileGrouper.assignGroups(allEntries)` on a pooled thread; on completion call `invokeLater` to repaint the list (no full model rebuild needed — only `list.repaint()` is required)
- [x] 5.2 Add a "Recalculate groups" `JButton` to the sort bar in `EditTrailPanel`; clicking it dispatches `FileGrouper.assignGroups()` on a pooled thread and calls `list.repaint()` on the EDT when done
