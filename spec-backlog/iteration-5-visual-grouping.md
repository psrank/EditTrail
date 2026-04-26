# EditTrail — Iteration 5: Visual Grouping

## Goal

Introduce visual grouping of related files using simple usage-based heuristics.

This iteration prepares the plugin for more advanced clustering later, but avoids overcomplicating the first grouping implementation.

## User Value

As a developer, I want visually recognisable groups of files so that I can quickly identify files that likely belong to the same feature, workflow, or recent editing session.

## Scope

### Included

- Assign group IDs to history entries
- Use simple heuristic grouping
- Show a coloured vertical indicator for each group
- Recalculate groups safely
- Preserve normal history behaviour if grouping fails

### Excluded

- True K-means clustering
- Dependency graph analysis
- Semantic code understanding
- Manual group editing
- Group naming

## Requirements

### Requirement: Assign group IDs

The plugin SHALL assign a group ID to history entries where grouping data is available.

#### Scenario: File receives group

- GIVEN the history contains files with related usage patterns
- WHEN grouping is calculated
- THEN related entries MAY receive the same group ID

#### Scenario: File has no group

- GIVEN a file has insufficient usage data
- WHEN grouping is calculated
- THEN the file MAY have no group ID
- AND it SHALL still appear normally in history

---

### Requirement: Use usage-based grouping

The first grouping implementation SHALL use simple heuristics based on user activity.

Possible signals include:

- files opened close together in time
- files edited close together in time
- files repeatedly opened in sequence
- files sharing a nearby path
- files sharing the same module/source root
- files sharing the same extension

#### Scenario: Files opened together

- GIVEN the user repeatedly opens `UserController.cs`, `UserService.cs`, and `UserRepository.cs` within short time windows
- WHEN grouping is recalculated
- THEN those files SHOULD be candidates for the same group

#### Scenario: Files unrelated in usage

- GIVEN two files are never opened or edited near each other
- WHEN grouping is recalculated
- THEN they SHOULD NOT be grouped only because they are recent

---

### Requirement: Show coloured group indicators

Each grouped history item SHALL show a coloured vertical indicator.

#### Scenario: Grouped file

- GIVEN a history entry has a group ID
- WHEN the list is rendered
- THEN a coloured vertical line SHALL appear on the left of the item

#### Scenario: Same group

- GIVEN two history entries have the same group ID
- WHEN the list is rendered
- THEN both entries SHALL use the same group colour

#### Scenario: Different groups

- GIVEN two history entries have different group IDs
- WHEN the list is rendered
- THEN they SHOULD use different colours where possible

---

### Requirement: Stable colours

Group colours SHOULD remain stable during a session.

#### Scenario: List refresh

- GIVEN a group is assigned colour blue
- WHEN search or sorting changes
- THEN the same group SHOULD continue using blue during the current session

---

### Requirement: Safe fallback

Grouping SHALL not be required for the history list to function.

#### Scenario: Grouping calculation fails

- GIVEN grouping throws an error or times out
- WHEN the history list is displayed
- THEN the list SHALL still show files without group indicators
- AND the plugin SHALL not crash

---

### Requirement: Manual recalculation action

The plugin SHOULD provide a way to recalculate groups.

#### Scenario: User recalculates groups

- GIVEN history contains enough entries
- WHEN the user triggers recalculation
- THEN grouping SHALL be recomputed in the background
- AND the UI SHALL update when complete

## Suggested Heuristic

A simple score can be calculated between two files:

```text
score =
  recent_open_sequence_weight +
  recent_edit_sequence_weight +
  same_directory_weight +
  same_extension_weight +
  repeated_cooccurrence_weight
```

Files above a threshold can be assigned into connected groups.

This is not K-means yet, but it gives useful grouping quickly.

## UI Example

```text
▌ UserController.cs
  src/Web/Controllers/UserController.cs

▌ UserService.cs
  src/Application/Users/UserService.cs

▌ IUserRepository.cs
  src/Domain/Users/IUserRepository.cs
```

The coloured vertical line indicates the group.

## Completion Criteria

- History entries can have group IDs
- Group indicators render in the list
- Same group uses same colour
- Search and sorting preserve group indicators
- Grouping is recalculated in the background
- Failure in grouping does not break the history list
