## MODIFIED Requirements

### Requirement: Show per-type counts on chips
Each file-type chip SHALL display the count of currently visible entries of that type, formatted as `{label} {count}` with a single space separator and NO surrounding parentheses.

#### Scenario: Count shown on chip without parentheses
- **WHEN** history contains 8 C# entries that match the current search
- **THEN** the C# chip SHALL display `C# 8`
- **AND** the label SHALL NOT contain parentheses around the count

#### Scenario: All chip count format
- **WHEN** the chip bar is displayed and 42 entries are currently visible across all types
- **THEN** the `All` chip SHALL display `All 42`
- **AND** the label SHALL NOT contain parentheses around the count

#### Scenario: Counts update with search
- **WHEN** the user types a search query that reduces C# matches from 8 to 3
- **THEN** the C# chip count SHALL update to display `C# 3`
- **AND** the updated label SHALL still NOT contain parentheses
