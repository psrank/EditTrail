---
name: workflow-status
description: "Show workflow status after any Spec Kit or OpenSpec command. Displays: which command just finished, the current iteration name (feature branch), and what your next command options are. Use when: a command just finished, checking what to do next, unsure which step comes next in the workflow, after /speckit.specify /speckit.plan /speckit.tasks /speckit.implement /speckit.done /opsx:apply /opsx:specify /opsx:plan."
argument-hint: "Name of the command that just finished (e.g. 'speckit.specify', 'speckit.plan')"
compatibility: "Requires spec-kit project structure with .specify/ directory"
metadata:
  author: "local"
  source: "local/commands/status.md"
user-invocable: true
disable-model-invocation: false
---

## User Input

```text
$ARGUMENTS
```

If `$ARGUMENTS` is non-empty, treat it as the name of the command that just finished (e.g. `speckit.specify`, `speckit.plan`, `opsx:apply`). If empty, infer from the conversation context or from which artifacts exist.

---

## Execution Steps

### Step 1 — Gather Feature Context

Run from the repo root:

```bash
bash .specify/scripts/bash/check-prerequisites.sh --json --paths-only 2>/dev/null
```

Parse the JSON to extract:
- `FEATURE_DIR` — absolute path to the current feature spec folder
- `BRANCH` — current Git branch name

Derive the **iteration name** from `BRANCH` (e.g. `004-market-drivers-features`).

If the script fails (no `.specify/` directory, no `feature.json`), output:

```
[speckit-status] No active Spec Kit feature found. Run /speckit.specify to start a new feature.
```

and stop.

---

### Step 2 — Detect Workflow State

Check which artifact files exist under `FEATURE_DIR`:

| Check | File |
|-------|------|
| Spec written | `spec.md` |
| Plan written | `plan.md` |
| Tasks generated | `tasks.md` |
| Tasks in progress / done | parse `tasks.md` for `- [X]` (done) vs `- [ ]` (pending) |

Determine the **current workflow stage**:

| Stage | Condition |
|-------|-----------|
| `fresh` | No `spec.md` |
| `specified` | `spec.md` exists, no `plan.md` |
| `planned` | `plan.md` exists, no `tasks.md` |
| `tasked` | `tasks.md` exists, 0 tasks complete |
| `implementing` | `tasks.md` exists, some tasks complete, some pending |
| `implemented` | `tasks.md` exists, all tasks complete |

---

### Step 3 — Determine Last Command

The **last command** is determined by:
1. `$ARGUMENTS` if provided (use that label directly)
2. Otherwise, infer from the conversation context (the command that the user or agent most recently invoked)
3. Otherwise, map stage to the most likely last command:

| Stage | Inferred Last Command |
|-------|-----------------------|
| `specified` | `/speckit.specify` or `/speckit.clarify` |
| `planned` | `/speckit.plan` |
| `tasked` | `/speckit.tasks` |
| `implementing` | `/speckit.implement` |
| `implemented` | `/speckit.implement` |

---

### Step 4 — Determine Next Command Options

| Current Stage | Next Command Options |
|---------------|---------------------|
| `fresh` | `/speckit.specify` — create the feature specification |
| `specified` | `/speckit.clarify` — refine the spec with targeted questions<br>`/speckit.plan` — generate the implementation plan |
| `planned` | `/speckit.tasks` — generate actionable task list |
| `tasked` | `/speckit.implement` — begin implementation |
| `implementing` | `/speckit.implement` — continue implementation<br>`/speckit.progress` — check remaining tasks |
| `implemented` | `/speckit.done` — verify, commit, push, merge to main |

---

### Step 5 — Output the Status

Print the following block (use the exact format below, substituting real values):

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  Spec Kit Status
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  Last command   : /speckit.plan
  Iteration      : 004-market-drivers-features
  Stage          : planned

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  What's next?
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  /speckit.tasks
      Generate an actionable, dependency-ordered tasks.md from the plan.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

If multiple next commands apply (e.g. `specified` stage), list all of them, one per line with a brief description below each.

For `implementing` stage, also include the task progress inline:

```
  Stage          : implementing (12 / 20 tasks complete)
```

---

## Workflow Reference

```
/speckit.specify
      ↓
[/speckit.clarify]  ← optional, repeat as needed
      ↓
/speckit.plan
      ↓
/speckit.tasks
      ↓
/speckit.implement  ← repeat until all tasks done
      ↓
[/speckit.progress] ← optional check
      ↓
/speckit.done       ← commit + push + merge to main
```

OpenSpec aliases: `/opsx:specify`, `/opsx:plan`, `/opsx:apply`, `/opsx:done` map to the same stages.
