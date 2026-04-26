# EditTrail Constitution

## Purpose

EditTrail exists to help developers navigate code by recent activity rather than by folder structure.

Large projects often require moving between controllers, services, tests, configuration files, UI components, domain models, database scripts, and documentation. The project tree is not always useful for this workflow because related files are often spread across different folders or modules.

EditTrail must provide a clear, fast, and persistent view of the developer's current working context.

## Core Principles

### 1. Activity-first navigation

The plugin must prioritise files the user has recently viewed or edited.

The main list must answer the question:

> What files have I recently touched or looked at in this project?

The plugin must not try to replace the JetBrains project tree. It should complement it by showing recent working context.

### 2. Simple by default

The default experience must work without configuration.

A new user should be able to install the plugin, open a project, move between files, and immediately see useful history.

Advanced features must not make the basic workflow harder.

### 3. Search should be intuitive

The plugin must avoid hidden prefix commands for common search operations.

Search should use visible controls such as:

- Match path
- Match content
- Regex
- Case sensitive
- Include all project files

Plain text, case-insensitive file-name search should be the default.

### 4. Performance must be protected

The plugin must not block the IDE UI thread with heavy operations.

The following operations must be asynchronous or debounced where needed:

- content search
- global project search
- file indexing
- grouping recalculation
- clustering
- persistence writes

The plugin must remain responsive in large projects.

### 5. Local-first and private

EditTrail must store data locally per project.

The plugin must not send file names, file paths, file contents, or usage history to any external service.

### 6. Safe degradation

If an advanced feature fails, the plugin must continue working in a simpler mode.

Examples:

- if content search fails, file-name search should still work
- if grouping fails, the history list should still render
- if project indexing is incomplete, history results should still show
- if persistence is unavailable, in-memory history should still operate

### 7. Clear visual distinction

The UI must clearly distinguish:

- recently viewed files
- recently edited files
- files from history
- files from global project search
- grouped files
- pinned files, if added later

Non-history global search results should be visually quieter, for example grey and italic.

### 8. IDE-native design

The plugin must feel like part of JetBrains IDEs.

It should use IntelliJ Platform UI patterns, tool windows, actions, icons, colours, and editor navigation behaviour.

### 9. Iterative delivery

The plugin must be delivered in small increments.

Each iteration should produce a usable improvement and should avoid depending on unfinished future features.

## Technical Guardrails

- Kotlin is the preferred implementation language.
- The plugin should target IntelliJ Platform APIs shared by IntelliJ IDEA and Rider where possible.
- Platform-specific behaviour should be isolated behind adapters.
- Project-level state should be persisted using JetBrains service/state APIs.
- File references should use `VirtualFile` or stable file URLs/paths.
- UI state should not be used as the source of truth.
- Heavy work must not run directly on the Swing Event Dispatch Thread.

## Acceptance Standard

A change is acceptable only when:

- the IDE starts successfully with the plugin installed
- opening files updates history
- editing files updates history where relevant
- the tool window remains responsive
- no obvious UI freezes occur during normal use
- behaviour works in both IntelliJ IDEA and Rider, unless explicitly marked as platform-specific
