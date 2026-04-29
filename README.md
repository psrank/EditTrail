# EditTrail

A JetBrains/IntelliJ plugin that tracks the files you view and edit in each project, and surfaces them in a dedicated tool window so you can return to your working context without hunting through the project tree.

- **Per-project history** — every project keeps its own list, persisted across IDE restarts.
- **View vs. edit timestamps** — sort by either.
- **Powerful search** — text, path, content, regex, case-sensitive, file-type chips.
- **Global project search** — extend search to files you have not opened yet.
- **Visual grouping** — related files (same directory, same time window, same extension) get a coloured bar.
- **Configurable** — adjust max history size, default sort, and search-option persistence.

---

## Requirements

- IntelliJ Platform IDE build **251+** (IntelliJ IDEA 2025.1 and later, including Community Edition)
- Java **21** runtime (bundled with modern JetBrains IDEs)

The plugin only depends on `com.intellij.modules.platform` and `com.intellij.modules.lang`, so it runs in any IntelliJ-based IDE on a recent build (IDEA, PyCharm, WebStorm, Rider, GoLand, etc.).

---

## Installation

### From source (current distribution method)

```bash
git clone https://github.com/psrank/EditTrail.git
cd EditTrail
./gradlew buildPlugin
```

The packaged plugin lands at `build/distributions/EditTrail-<version>.zip`.

In your IDE:

1. Open **Settings → Plugins**.
2. Click the gear icon → **Install Plugin from Disk…**.
3. Pick the `.zip` from `build/distributions/`.
4. Restart the IDE when prompted.

### Try it in a sandbox IDE

```bash
./gradlew runIde
```

This launches a fresh IntelliJ Community sandbox with the plugin installed — handy for trying it without touching your main IDE config.

---

## Getting Started

1. Open a project.
2. Open the **EditTrail** tool window — by default it's on the right side, in the secondary tool-window strip. Use **View → Tool Windows → EditTrail** if you don't see it.
3. Start editing files. Each file you open or edit is recorded automatically.
4. Click any entry to jump back to that file.

The history is project-scoped: switching projects shows a different list. Files that no longer exist on disk are removed automatically on project open.

---

## Tool Window Layout

```
┌─────────────────────────────────────────────────┐
│ Sort: [ Last Edited ▾ ]                         │  ← sort selector
├─────────────────────────────────────────────────┤
│ [ Search files…                              ]  │  ← search field
├─────────────────────────────────────────────────┤
│ [✓] Match path  [ ] Match content               │  ← search options
│ [ ] Regex       [ ] Case sensitive              │
│ [ ] Include all project files                   │
├─────────────────────────────────────────────────┤
│ [C#] [Kotlin] [JSON] [XML] …                    │  ← file-type chips
├─────────────────────────────────────────────────┤
│ ▌ MyService.cs            src/Services          │  ← coloured bar = group
│ ▌ MyService.Tests.cs      tests/Services        │
│   appsettings.json        src/                  │
│ ▌ Repository.cs           src/Data              │
└─────────────────────────────────────────────────┘
```

### Sort modes

- **Last Edited** — most recently modified file first. Files you have only viewed (never edited) appear after edited files.
- **Last Viewed** — most recently opened/focused file first.

The default sort is configurable in **Settings → Tools → EditTrail**.

### Search

Typing in the search field filters the list as you type (with a 300 ms debounce). Search is incremental: matches narrow, non-matches disappear.

| Option                       | Effect                                                                               |
|------------------------------|--------------------------------------------------------------------------------------|
| (default)                    | Match against the file name only.                                                    |
| **Match path**               | Also match against the project-relative path.                                        |
| **Match content**            | Also search file contents (runs off the EDT; results stream in).                     |
| **Regex**                    | Treat the query as a Java regular expression. Invalid regex highlights the field red. |
| **Case sensitive**           | Disable case folding for all of the above.                                           |
| **Include all project files**| Extend the search across every file in the project, not just files in your history.  |

### File-type chips

Each chip toggles a file-type filter. Multiple chips combine with **OR**: selecting `C#` and `Kotlin` shows files of either type. The "Other" chip catches anything not in the built-in classifier (C#, XAML, Razor, JSON, XML, SQL, YAML, Markdown, Kotlin, Java, JavaScript, TypeScript, HTML, CSS).

The chip bar only renders types that actually appear in your current history, so it stays compact.

### Visual grouping

EditTrail looks for clusters of files you worked on together and assigns them a shared coloured bar on the left edge of the row. Two files are grouped when their combined score crosses a threshold:

- **Time proximity** — same 5-minute / 30-minute / 2-hour window
- **Path prefix** — same parent directory, or same grandparent
- **Extension** — same file extension

Singletons stay uncoloured. Groups are session-scoped and recalculate after every history change.

---

## Settings

Open **File → Settings → Tools → EditTrail** (Windows/Linux) or **Preferences → Tools → EditTrail** (macOS).

| Setting                       | Default      | Notes                                                                            |
|-------------------------------|--------------|----------------------------------------------------------------------------------|
| **Max history entries**       | 500          | Range 50–10 000. Reducing the limit trims oldest entries on next update.         |
| **Default sort**              | Last edited  | Applied to newly-opened tool windows.                                            |
| **Remember search options**   | off          | When on, search-option checkboxes (path/content/regex/case) persist across sessions. |

Changes apply immediately to all open projects.

### Clearing history

The tool window has a **Clear history** action that wipes the current project's history (with confirmation). Application-level settings are not affected.

---

## How It Works (briefly)

- A **project listener** subscribes to the file-editor manager and records every file open / tab switch as a "viewed" event.
- Each opened file gets a **document listener** attached, which stamps an "edited" timestamp on the first modification.
- History is stored as a `PersistentStateComponent` per project (`EditTrailProjectService`) and survives IDE restarts.
- Application preferences (max size, default sort, etc.) live in `EditTrailAppSettings` (`APP_CONFIG/edittrail-settings.xml`).
- Content search and global-project scans run on background threads with a generation counter so stale results are dropped.

---

## Development

The project is a standard IntelliJ Platform Gradle plugin built with Kotlin 2.3 / JVM 21.

```bash
./gradlew build           # compile + run tests
./gradlew test            # JUnit 5 tests only
./gradlew runIde          # launch sandbox IDE with the plugin
./gradlew buildPlugin     # produce the distributable .zip
./gradlew verifyPlugin    # IntelliJ Plugin Verifier (compatibility report)
```

Source layout:

```
src/main/kotlin/com/psrank/edittrail/   # plugin code
src/main/resources/META-INF/plugin.xml  # plugin descriptor
src/test/kotlin/...                     # tests
openspec/                               # spec-driven change history
spec-backlog/                           # planned iterations
```

Each feature in this plugin shipped through an OpenSpec iteration (see `openspec/changes/archive/` for the trail of designs, deltas, and tasks).

---

## License

See the repository for license details.
