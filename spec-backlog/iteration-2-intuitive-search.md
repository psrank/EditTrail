# EditTrail — Iteration 2: Intuitive Search

## Goal

Add simple and intuitive filtering to the EditTrail history list.

This replaces the earlier prefix-based search idea with a visible, user-friendly search interface.

## User Value

As a developer, I want to quickly filter my recent files without remembering special commands such as `i:`, `p:`, or `f:`.

## Scope

### Included

- Search text box
- Plain text search by default
- Case-insensitive matching by default
- Fuzzy file-name matching
- Optional toggles:
  - Match path
  - Match content
  - Regex
  - Case sensitive
- Debounced filtering
- Invalid regex handling

### Excluded

- File-type chips
- Global project search
- Grouping
- Saved searches

## Requirements

### Requirement: Provide search input

The EditTrail panel SHALL provide a search field above the history list.

#### Scenario: Search field is visible

- GIVEN the EditTrail tool window is open
- WHEN the history panel is displayed
- THEN a search text field SHALL appear above the list

#### Scenario: Empty search

- GIVEN the search field is empty
- WHEN history is displayed
- THEN all history entries SHALL be shown subject to active sort order

---

### Requirement: Search file names by default

The search field SHALL match file names by default.

#### Scenario: User searches by file name

- GIVEN history contains `UserService.cs`
- WHEN the user types `user`
- THEN `UserService.cs` SHALL be shown

#### Scenario: Search is case-insensitive by default

- GIVEN history contains `UserService.cs`
- WHEN the user types `userservice`
- THEN `UserService.cs` SHALL be shown

#### Scenario: Non-matching file

- GIVEN history contains `OrderService.cs`
- WHEN the user types `invoice`
- THEN `OrderService.cs` SHALL be hidden

---

### Requirement: Support fuzzy matching

The search SHALL support fuzzy matching for common file-name searches.

#### Scenario: Partial spaced search

- GIVEN history contains `UserApplicationService.cs`
- WHEN the user types `user service`
- THEN `UserApplicationService.cs` SHALL be shown

#### Scenario: Abbreviation search

- GIVEN history contains `UserController.cs`
- WHEN the user types `uc`
- THEN `UserController.cs` MAY be shown if abbreviation matching is enabled

---

### Requirement: Support path matching

The UI SHALL provide a `Match path` toggle.

#### Scenario: Path matching disabled

- GIVEN history contains `src/Application/Users/UserService.cs`
- WHEN `Match path` is disabled
- AND the user searches `Application`
- THEN the file SHALL NOT match unless the file name contains `Application`

#### Scenario: Path matching enabled

- GIVEN history contains `src/Application/Users/UserService.cs`
- WHEN `Match path` is enabled
- AND the user searches `Application`
- THEN the file SHALL be shown

---

### Requirement: Support content matching

The UI SHALL provide a `Match content` toggle.

#### Scenario: Content matching enabled

- GIVEN history contains a file whose content contains `CreateUserCommand`
- WHEN `Match content` is enabled
- AND the user searches `CreateUserCommand`
- THEN the file SHALL be shown

#### Scenario: Content search is asynchronous

- GIVEN `Match content` is enabled
- WHEN the user types into the search field
- THEN content search SHALL NOT block the UI thread

#### Scenario: Binary or unsupported file

- GIVEN a history entry points to a binary or unsupported file
- WHEN content search is enabled
- THEN the file SHALL be skipped for content matching
- AND the plugin SHALL NOT crash

---

### Requirement: Support regex mode

The UI SHALL provide a `Regex` toggle.

#### Scenario: Regex enabled

- GIVEN history contains `UserService.cs` and `UserRepository.cs`
- WHEN `Regex` is enabled
- AND the user searches `User(Service|Repository)`
- THEN both files SHALL be shown

#### Scenario: Invalid regex

- GIVEN `Regex` is enabled
- WHEN the user enters an invalid regular expression
- THEN the UI SHALL show a non-blocking validation state
- AND previous valid results MAY remain visible
- AND the plugin SHALL NOT crash

---

### Requirement: Support case-sensitive search

The UI SHALL provide a `Case sensitive` toggle.

#### Scenario: Case-sensitive search enabled

- GIVEN history contains `UserService.cs`
- WHEN `Case sensitive` is enabled
- AND the user searches `userservice`
- THEN `UserService.cs` SHALL NOT match

#### Scenario: Case-sensitive search disabled

- GIVEN history contains `UserService.cs`
- WHEN `Case sensitive` is disabled
- AND the user searches `userservice`
- THEN `UserService.cs` SHALL match

---

### Requirement: Debounce search input

The plugin SHALL debounce search input to avoid excessive recalculation.

#### Scenario: User types quickly

- GIVEN the user types multiple characters quickly
- WHEN the search field changes repeatedly
- THEN EditTrail SHALL avoid recalculating expensive filters for every keystroke

## Suggested UI

```text
[ Search files...                         ]

[ ] Match path   [ ] Match content   [ ] Regex   [ ] Case sensitive
```

## Suggested Data Model

```kotlin
data class SearchOptions(
    val query: String,
    val matchPath: Boolean,
    val matchContent: Boolean,
    val regex: Boolean,
    val caseSensitive: Boolean
)
```

## Completion Criteria

- Search box filters history by file name
- Search is case-insensitive by default
- Fuzzy matching works for common partial searches
- Match path works
- Match content works without freezing the IDE
- Regex mode works
- Invalid regex is handled safely
