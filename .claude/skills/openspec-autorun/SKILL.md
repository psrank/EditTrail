---
name: "openspec-autorun"
description: "Automatically orchestrate the full OpenSpec workflow for all pending iterations in spec-backlog/. Reads propose.md (and optional notes.md) per iteration, runs the full pipeline (propose → tests-before → apply → tests-after → verify → verify-runtime → archive), and tracks progress in spec-backlog/autorun-progress.md. Supports both flat layouts (spec-backlog/iteration-N.md) and subdirectory layouts (spec-backlog/iteration-N/propose.md). Use when: running a full automated OpenSpec iteration, processing spec-backlog items, continuing a paused autorun."
argument-hint: "Optional: iteration number (e.g. '006'), 'status', 'reset', or 'rollback 006 apply' to roll back to a stage"
compatibility: "Requires the openspec CLI installed and an OpenSpec project structure with an openspec/ directory. Backlog folder is spec-backlog/ — supports flat files (iteration-N-name.md) and subdirectories (iteration-N/propose.md), or a mix of both."
metadata:
  author: "local"
  source: "local/.claude/skills/openspec-autorun/SKILL.md"
  version: "1.1"
user-invocable: true
disable-model-invocation: false
---

## Agent Compatibility

This skill is designed for **VS Code Copilot** (GitHub Copilot Chat in VS Code). The `EXECUTE_COMMAND` directives below invoke named OpenSpec commands (`/opsx:propose`, `/opsx:apply`, `/opsx:verify`, `/opsx:archive`) that VS Code Copilot resolves automatically.

Other agents (Claude Code CLI, Cursor, etc.) do not support `EXECUTE_COMMAND` natively. In those environments, substitute each directive by reading and following the corresponding SKILL.md file in `.claude/skills/openspec-{name}/SKILL.md` directly:

| Directive                       | Equivalent skill file                                     |
|---------------------------------|-----------------------------------------------------------|
| `EXECUTE_COMMAND: opsx-propose` | `.claude/skills/openspec-propose/SKILL.md`                |
| `EXECUTE_COMMAND: opsx-apply`   | `.claude/skills/openspec-apply-change/SKILL.md`           |
| `EXECUTE_COMMAND: opsx-verify`  | `.claude/skills/openspec-verify-change/SKILL.md`          |
| `EXECUTE_COMMAND: opsx-archive` | `.claude/skills/openspec-archive-change/SKILL.md`         |

If those local skill files are not present, fall back to invoking the openspec CLI directly (`openspec new change`, `openspec instructions <artifact> --change <name> --json`, `openspec status --change <name> --json`, etc.) per the OpenSpec docs.

---

## Configuration (Optional)

Create `spec-backlog/autorun-config.yml` to override defaults:

```yaml
max_retries: 3           # auto-remediation attempts for verify/CI failures (default: 3)
context_warn_stages: 8   # warn to start fresh session after N stages in one session (default: 8 = 1 iteration)
stage_gates:             # human approval required before these stages (default: none)
  - apply
  - archive
```

If this file does not exist, use the defaults above.

---

## User Input

```text
$ARGUMENTS
```

- If `$ARGUMENTS` is `status` → execute Step 1 (display progress table) then stop.
- If `$ARGUMENTS` is `reset` → delete `spec-backlog/autorun-progress.md` and recreate it from scratch, then proceed from Step 1.
- If `$ARGUMENTS` is a number (e.g. `006`) → treat it as the forced starting iteration and skip all earlier ones.
- If `$ARGUMENTS` is `rollback {iteration} {stage}` (e.g. `rollback 006 apply`) → execute the Rollback procedure below, then stop.
- If empty → resume from the last incomplete stage automatically.

---

## Rollback Procedure

When `$ARGUMENTS` is `rollback {iteration} {stage}`:

1. Read `spec-backlog/autorun-progress.md`.
2. In the Stage Detail for `{iteration}`, find the target `{stage}`.
3. Uncheck the target stage and **all stages after it** (set `- [x]` → `- [ ]`).
4. Set the iteration's `Status = pending` and `Current Stage = {stage}`.
5. Delete or revert the downstream OpenSpec artifacts so they will be regenerated:
   - `propose` rollback → `rm -rf openspec/changes/{change-name}` (the entire change directory). If already archived, move it back: `mv openspec/changes/archive/YYYY-MM-DD-{change-name} openspec/changes/{change-name}` and then delete.
   - `apply` rollback → in `openspec/changes/{change-name}/tasks.md`, flip every `- [x]` back to `- [ ]`. Inform the user that any implemented code must be reverted manually (check `git log` / `git diff`).
   - `verify` rollback → no artifacts to delete; just uncheck stages.
   - `archive` rollback → if the change is already archived, move it back: `mv openspec/changes/archive/YYYY-MM-DD-{change-name} openspec/changes/{change-name}`.
6. Update `autorun-progress.md` with rollback timestamp and a note.
7. Output a summary of what was rolled back and which files were removed or moved.

---

## Pipeline Overview

Each iteration in `spec-backlog/` passes through the following stages in order:

| # | Stage             | Skill / Command                                                | Gate           |
|---|-------------------|----------------------------------------------------------------|----------------|
| 1 | **branch**        | `git checkout -b feat/{change-name}` from latest `main`        | —              |
| 2 | **propose**       | `/opsx:propose <change-name>` with `propose.md` content        | —              |
| 3 | **review**        | Human review of generated proposal/design/tasks                | human approval |
| 4 | **tests-before**  | `project-tests-before-impl` skill                              | —              |
| 5 | **apply**         | `/opsx:apply <change-name>`                                    | configurable   |
| 6 | **tests-after**   | `project-tests-after-impl` skill                               | —              |
| 7 | **verify**        | `/opsx:verify <change-name>` — fix all CRITICAL issues         | —              |
| 8 | **verify-runtime**| pytest + API health + frontend build (auto-retry ×3)           | —              |
| 9 | **archive**       | `/opsx:archive <change-name>`                                  | configurable   |

> Note: OpenSpec's `propose` step generates `proposal.md`, `design.md`, and `tasks.md` in a single command, so this pipeline is shorter than the speckit equivalent (which has separate specify / clarify / plan / analyze / tasks / checklist phases). The `branch` stage always precedes `propose` so that all generated artifacts and implementation code land on the feature branch from the start.

---

## Step 1 — Initialize or Load Progress Tracking

### 1a. Discover iterations

Scan `spec-backlog/` for iteration entries. **Two layouts are supported and may be mixed in the same folder:**

**Layout A — flat files**: `spec-backlog/iteration-{N}-{slug}.md` (e.g. `iteration-07-add-risk-portfolio.md`)

**Layout B — subdirectories**: `spec-backlog/iteration-{N}/propose.md` (e.g. `iteration-7/propose.md`)

Discovery algorithm:

1. Collect all items in `spec-backlog/` (excluding `autorun-progress.md`, `autorun-config.yml`, and any non-iteration files).
2. For each **file** matching `iteration-*.md` → treat as Layout A. The propose content is the file itself.
3. For each **subdirectory** matching `iteration-*` that contains a `propose.md` file → treat as Layout B. The propose content is `propose.md` inside that directory. Also look for a sibling `notes.md`.
4. Extract the **iteration number** from the name: strip the `iteration-` prefix, read the leading digits, normalize to 3-digit zero-padded form for display (e.g. `7` → `007`).
5. Sort all discovered entries by their numeric iteration number.
6. For each entry, determine the **change name**:
   - If the propose content's first non-empty line is a directive of the form `/opsx:propose <change-name>` (kebab-case), use that as the change name.
   - Otherwise, derive a kebab-case name from the H1 heading in the file, or from the iteration file/directory name.

File path shorthand used in the rest of this document:
- `{propose-file}` = the resolved path to the propose content (Layout A: `spec-backlog/iteration-{N}-{slug}.md`; Layout B: `spec-backlog/iteration-{N}/propose.md`)
- `{notes-file}` = sibling `notes.md` in Layout B subdirectory, or absent in Layout A.

If `spec-backlog/` does not exist, output:

```
No spec-backlog/ directory found. Create it with either:
  - Flat files:        spec-backlog/iteration-N-name.md
  - Subdirectories:    spec-backlog/iteration-N/propose.md
before running /openspec-autorun.
```

and stop.

### 1b. Create or update `spec-backlog/autorun-progress.md`

**If the file does not exist**, create it with the discovery results:

```markdown
# OpenSpec Autorun Progress

Last updated: {ISO timestamp}
Session stages completed: 0

## Status Table

| Iteration | Change Name                          | Current Stage | Status   | Started | Completed |
|-----------|--------------------------------------|---------------|----------|---------|-----------|
| 003       | add-event-extraction-normalization   | —             | pending  |         |           |
| 004       | add-spread-and-spike-models          | —             | pending  |         |           |
...

## Stage Detail

### 003 — add-event-extraction-normalization
- [ ] branch
- [ ] propose
- [ ] review
- [ ] tests-before
- [ ] apply
- [ ] tests-after
- [ ] verify
- [ ] verify-runtime
- [ ] archive

### 004 — add-spread-and-spike-models
...
```

**If the file exists**, read it and parse the current state for each iteration. If the discovery in 1a found new iterations not yet in the file, append them in pending state.

### 1c. Read optional config

Read `spec-backlog/autorun-config.yml` if it exists. Apply:

- `max_retries` (default: 3)
- `context_warn_stages` (default: 8)
- `stage_gates` list (default: `[]`)

### 1d. Display status

Print the full status table to the user. If `$ARGUMENTS` is `status`, stop here.

---

## Step 2 — Select the Active Iteration

Determine which iteration to process:

1. If `$ARGUMENTS` specifies an iteration number, select that iteration.
2. Otherwise, select the **first iteration** whose status is NOT `complete` or `skipped`.
3. Within that iteration, find the **first unchecked stage** (`- [ ]`) in the Stage Detail section.

### Stale Artifact Detection

Before executing any stage, **check if upstream artifacts are newer than downstream ones**:

- Before **apply**: if `{propose-file}` was modified *after* `openspec/changes/{change-name}/tasks.md` was last written, warn and offer to re-run **propose** first.
- Before **verify**: if any of `openspec/changes/{change-name}/{proposal,design,tasks}.md` was modified *after* the last git commit touching implementation files, warn that verify may be checking against newer artifacts than were implemented.
- Before **archive**: if `openspec/changes/{change-name}/tasks.md` still contains any `- [ ]` entries, warn that the archive will proceed with incomplete tasks (the openspec-archive-change skill will also prompt).

Use file timestamps (`ls -l --time-style=+"%Y-%m-%dT%H:%M:%S"`) to compare. If a stale artifact is detected:

```
⚠ Stale artifact detected: {upstream} is newer than {downstream}.
  Options: (1) Re-run the earlier stage first (recommended). (2) Ignore and continue anyway.
```

Wait for user response.

If all iterations are `complete`, output:

```
All iterations complete! No work remaining.
```

and stop.

Show the user which iteration, stage, and any stale artifact warnings before proceeding to Step 3.

---

## Step 3 — Execute Pipeline Stages

Execute each stage sequentially. **Before starting each stage**, update the progress file:

- Set the iteration's `Current Stage` to the stage name
- Set the iteration's `Status` to `in-progress`
- Set `Started` if this is the first stage for this iteration
- Increment `Session stages completed` counter
- Update `Last updated` timestamp

**After each stage completes successfully**, update the progress file:

- Check off the completed stage (`- [x] stage-name`)
- Update `Last updated` timestamp

**Context budget check**: After each stage, compare `Session stages completed` against `context_warn_stages` (default: 8). If the threshold is reached, output the budget warning (see Step 4) and stop — do not begin the next stage automatically.

**Stage gates**: If the next stage appears in the `stage_gates` config list, pause and display:

```
Gate: About to execute '{stage}' for iteration {id} ({change-name}).
Approve? (yes/no/skip)
```

- `yes` → proceed. `no` → halt and wait. `skip` → mark stage skipped and move to next.

If a stage **fails**, update the progress file with `Status = error` and `Notes = <failure summary>`, then STOP and report to the user. Do not proceed automatically.

---

### Stage 1: branch

1. Update progress file: stage = `branch`, status = `in-progress`.
2. Ensure the working tree is clean — no uncommitted changes. If dirty, stop and report:

   ```
   Working tree has uncommitted changes. Stash or commit them before starting a new iteration.
   ```

3. Fetch and reset to the latest `main` (or the repo's default trunk branch):

   ```bash
   git fetch origin
   git checkout main
   git reset --hard origin/main
   ```

4. Derive the branch name from the change name: `feat/{change-name}` (e.g. `feat/add-spread-and-spike-models`).
5. Check if the branch already exists locally or on origin:

   ```bash
   git branch --list feat/{change-name}
   git ls-remote --heads origin feat/{change-name}
   ```

   - If it **does not exist**, create and check it out:

     ```bash
     git checkout -b feat/{change-name}
     ```

   - If it **exists locally or on origin**, ask the user:

     ```
     Branch feat/{change-name} already exists.
     Options: (1) Check it out and continue. (2) Delete it and recreate from main. (3) Abort.
     ```

     Wait for user response before proceeding.

6. Record the branch name in the progress file's Stage Detail for this iteration (as a note under the `branch` checkbox).
7. Update progress: check off `branch`.

---

### Stage 2: propose

1. Update progress file: stage = `propose`, status = `in-progress`.
2. Read the full content of `{propose-file}` (resolved from Step 1a — Layout A flat file or Layout B subdirectory).
3. Determine the change name:
   - If `propose.md` begins with a `/opsx:propose <name>` line, strip that line and use `<name>`.
   - Otherwise derive a kebab-case name from the H1 or iteration directory.
4. Verify no existing change directory conflicts: if `openspec/changes/{change-name}/` already exists, ask the user whether to (a) continue the existing change (skip the rest of this stage), (b) delete and recreate, or (c) abort.
5. Execute the following command, passing the (cleaned) `propose.md` body as the change description:

   **EXECUTE_COMMAND: opsx-propose**

   Arguments: `<change-name>` plus the body of `propose.md` as the change description.

6. Wait for completion. Verify that the following exist:

   - `openspec/changes/{change-name}/proposal.md`
   - `openspec/changes/{change-name}/design.md`
   - `openspec/changes/{change-name}/tasks.md`

   Use `openspec status --change "{change-name}" --json` to confirm `applyRequires` artifacts are all `done`.

7. Record the resolved change name in the progress file's Status Table for this iteration.
8. Update progress: check off `propose`.

---

### Stage 3: review

1. Update progress file: stage = `review`.
2. Read `openspec/changes/{change-name}/proposal.md`, `design.md`, and `tasks.md`. Read `{notes-file}` if present (Layout B subdirectories only).
3. Summarize for the user:

   ```
   Proposal review for {change-name}:
     - Proposal: {1-line summary}
     - Design key decisions: {bulleted list}
     - Tasks: {N tasks, grouped by area}
     - Notes from backlog: {summary if notes.md exists}

   Approve? (yes / request-edits {what to change} / skip)
   ```

4. Wait for user response:

   - `yes` → proceed.
   - `request-edits {description}` → edit the relevant artifact(s) in `openspec/changes/{change-name}/` per the description, then re-summarize and re-prompt.
   - `skip` → mark this iteration `skipped` and move on to the next iteration (do not run the remaining stages).

5. Update progress: check off `review`.

> This stage is the equivalent of speckit-autorun's `clarify` + `analyze` gates collapsed into one human checkpoint, since OpenSpec emits all artifacts in a single `propose` step.

---

### Stage 4: tests-before

1. Update progress file: stage = `tests-before`.
2. Read and follow the instructions in `.claude/skills/project-tests-before-impl/SKILL.md`. This skill generates tests before implementation (TDD). Use the requirements / scenarios in `openspec/changes/{change-name}/specs/` (delta specs) and the tasks list as input.

   > Note: this project's existing `project-tests-before-impl` skill is C#/.NET-flavored. If the iteration's tests are Python/pytest, adapt accordingly. Default test command for this repo is `dotnet test`.

3. Run the generated tests to confirm they fail for the right reason (expected red):

   ```bash
   dotnet test 2>&1 | tail -30
   ```

   (Substitute `python -m pytest tests/ -x --tb=short -q` if the iteration is Python.)

4. If tests pass unexpectedly (green before implementation), investigate — it may mean the functionality already exists or the tests are not testing the right things. Adjust if needed.
5. **Auto-remediation** (up to `max_retries` attempts): if tests show import / build errors or scaffolding issues (not business logic failures), fix the test scaffolding and re-run. Count attempts; after `max_retries` ask the user.
6. Update progress: check off `tests-before`.

---

### Stage 5: apply

1. Update progress file: stage = `apply`.
2. Execute:

   **EXECUTE_COMMAND: opsx-apply**

   Arguments: `<change-name>`.

3. Wait for full implementation completion. The openspec-apply-change skill loops through tasks in `openspec/changes/{change-name}/tasks.md`, marking each `- [x]` as it goes. Confirm all tasks are checked off, or that any pause was deliberate (in which case STOP this stage and report).
4. Update progress: check off `apply`.

---

### Stage 6: tests-after

1. Update progress file: stage = `tests-after`.
2. Read and follow the instructions in `.claude/skills/project-tests-after-impl/SKILL.md`. This skill fills test gaps after implementation (edge cases, snapshots, regressions).
3. Run the full test suite and verify it is green:

   ```bash
   dotnet test 2>&1 | tail -30
   ```

   (Or `python -m pytest tests/ -x --tb=short -q` for Python.)

4. **Auto-remediation loop** (up to `max_retries` attempts):

   - If tests fail, inspect failure output to determine root cause.
   - Attempt 1: fix the failing test or implementation. Re-run.
   - Attempt 2: if still failing, try an alternative fix. Re-run.
   - Attempt 3: if still failing, try a third approach. Re-run.
   - If all `max_retries` attempts fail, update progress to `error` and ask the user: "Tests still failing after {max_retries} attempts. Options: (1) I'll fix manually — re-run from `tests-after` when ready. (2) Skip `tests-after` and continue. (3) Halt autorun."

5. Update progress: check off `tests-after`.

---

### Stage 7: verify

1. Update progress file: stage = `verify`.
2. Execute:

   **EXECUTE_COMMAND: opsx-verify**

   Arguments: `<change-name>`.

3. Review the verification report carefully:

   - **CRITICAL issues** must be resolved before continuing. Edit the relevant code or artifacts to fix them, then re-run `/opsx:verify` until no CRITICAL issues remain.
   - **WARNING-level issues** should be noted in the progress file's Notes column but do not block progress.
   - **SUGGESTION-level issues** can be ignored (or addressed opportunistically).

4. Update progress: check off `verify`, note any warnings in the Notes column.

---

### Stage 8: verify-runtime

1. Update progress file: stage = `verify-runtime`.
2. Run all verification checks below. **Auto-retry up to `max_retries` times** before asking the user.

**Check A — Backend test suite:**

```bash
dotnet test 2>&1 | tail -30
```

(Substitute `python -m pytest tests/ -x --tb=short -q` if the iteration is Python.)

Pass criteria: exit code 0, no failures.

**Check B — API health check:**

```bash
# Start the AppHost or API in the background on a test port
dotnet run --project apps/api/TradeContext.Api --urls http://127.0.0.1:8099 > /tmp/api.log 2>&1 &
API_PID=$!
sleep 6
HEALTH=$(curl -sf http://127.0.0.1:8099/health 2>&1)
CURL_EXIT=$?
kill $API_PID 2>/dev/null
wait $API_PID 2>/dev/null
echo "Health response: $HEALTH"
echo "Curl exit: $CURL_EXIT"
```

Pass criteria: curl exit code 0, response contains `"status":"Healthy"` or `"status":"ok"` (depending on this project's health endpoint contract).

**Check C — Frontend build** (if `apps/web/package.json` exists):

```bash
npm run build --workspace apps/web 2>&1 | tail -15
```

Pass criteria: exit code 0, build output directory created.

**Auto-remediation loop** (up to `max_retries` attempts per check):

For each failing check:

- **Check A failure**: inspect the failure, make a targeted fix, re-run only that check. Count attempts.
- **Check B failure**: check for port conflicts, missing env vars, or AppHost composition issues; fix; re-run.
- **Check C failure**: check for TypeScript errors or missing deps; fix; re-run.

If a check still fails after `max_retries` attempts:

```
Verification failed at Check {A/B/C} after {max_retries} attempts.
Last error: {error}
Options:
  (1) I'll fix it manually — re-run `/openspec-autorun` to retry verify-runtime.
  (2) Skip verify-runtime and continue to archive anyway.
  (3) Halt autorun.
```

Wait for user response before continuing.

If all checks pass:

- Update progress: check off `verify-runtime`.
- Proceed to Stage 8.

---

### Stage 9: archive

1. Update progress file: stage = `archive`.
2. Execute:

   **EXECUTE_COMMAND: opsx-archive**

   Arguments: `<change-name>`.

3. The openspec-archive-change skill will:

   - Confirm all artifacts are `done` (prompts if not — answer `yes` if `verify` already passed).
   - Sync delta specs into `openspec/specs/` (recommended; accept the default).
   - Move `openspec/changes/{change-name}/` → `openspec/changes/archive/YYYY-MM-DD-{change-name}/`.

4. After `/opsx:archive` completes successfully:

   - Update the progress file:
     - Check off `archive`
     - Set `Status = complete`
     - Set `Completed = {ISO timestamp}`
   - Update the Status Table row for this iteration.

5. Output a completion summary for this iteration.

---

## Step 4 — Context Budget Check and Fresh Session Handoff

### Context budget warning

After every stage completes, check `Session stages completed` in the progress file. If it has reached or exceeded `context_warn_stages` (default: 8 = one full iteration), output the following and **stop immediately** — do not begin the next stage:

```
Context budget reached ({N} stages completed in this session).

To save token usage and ensure the next iteration starts with a clean context:

  1. This session's progress is fully saved to spec-backlog/autorun-progress.md.
  2. Close this chat and start a fresh session.
  3. In the new session, run: /openspec-autorun
     (or /openspec-autorun {next-iteration-number} to jump directly)

Current progress summary:
  {status table}
```

Reset `Session stages completed` to 0 in the progress file before stopping so the next session starts fresh.

> This is the primary mechanism for context clearing. Because agents cannot programmatically wipe their own context window, the skill saves all state to the progress file and hands control back to you to start a new session. The next `/openspec-autorun` call will resume exactly where this one left off.

---

## Step 5 — Continue or Stop (Between Iterations)

After completing all 9 stages of an iteration (status = `complete`), check for more pending iterations:

- **If more pending iterations exist**:

  Output the following and **stop** (do not continue automatically):

  ```
  ✓ Iteration {id} ({change-name}) complete!

  To save token usage, start a fresh session for the next iteration:

    1. Close this chat and open a new session.
    2. Run: /openspec-autorun
       (or /openspec-autorun {next-id} to start {next-change-name} directly)

  All progress is saved to spec-backlog/autorun-progress.md.
  ```

  Reset `Session stages completed` to 0 in the progress file.

- **If no more pending iterations**:

  Display the full progress table and output:

  ```
  All iterations complete!
  ```

---

## Backlog Folder Format Reference

Both layouts are supported and can be mixed freely within the same `spec-backlog/` folder.

**Layout A — flat files** (current carbonedge convention):

```text
spec-backlog/
├── autorun-config.yml                                    # optional
├── autorun-progress.md                                   # generated/maintained by this skill
├── iteration-07-add-risk-portfolio.md                    # propose content (flat file)
├── iteration-08-add-production-hardening.md
└── iteration-09-add-backtesting.md
```

**Layout B — subdirectories**:

```text
spec-backlog/
├── autorun-config.yml          # optional
├── autorun-progress.md         # generated/maintained by this skill
├── iteration-3/
│   ├── propose.md              # required — fed to /opsx:propose
│   └── notes.md                # optional — extra context for review/verify
├── iteration-4/
│   └── propose.md
└── iteration-5/
    └── propose.md
```

**Mixed layout** (both at once — iteration numbers must not collide):

```text
spec-backlog/
├── iteration-07-add-risk-portfolio.md   # flat
├── iteration-08/                        # subdirectory
│   ├── propose.md
│   └── notes.md
└── iteration-09-add-backtesting.md      # flat
```

**`propose.md` format** (mirrors carbonedge convention):

```markdown
/opsx:propose add-event-extraction-normalization

# Iteration 3 — add-event-extraction-normalization

<free-form prose describing what to build, the architectural intent,
the goals, and the required functional scope>
```

The first non-empty line `/opsx:propose <name>` is optional but recommended — it pins the kebab-case change name. Without it, the skill derives the name from the H1 or directory name.

---

## Progress File Format Reference

`spec-backlog/autorun-progress.md` follows this structure:

```markdown
# OpenSpec Autorun Progress

Last updated: 2026-04-25T10:30:00Z
Session stages completed: 0

## Status Table

| Iteration | Change Name                        | Current Stage    | Status      | Started    | Completed  |
|-----------|------------------------------------|------------------|-------------|------------|------------|
| 003       | add-event-extraction-normalization | archive          | complete    | 2026-04-20 | 2026-04-21 |
| 004       | add-spread-and-spike-models        | verify-runtime   | in-progress | 2026-04-22 |            |
| 005       | add-backtesting-and-evaluation     | —                | pending     |            |            |

## Stage Detail

### 003 — add-event-extraction-normalization
- [x] branch  <!-- feat/add-event-extraction-normalization -->
- [x] propose
- [x] review
- [x] tests-before
- [x] apply
- [x] tests-after
- [x] verify
- [x] verify-runtime
- [x] archive

### 004 — add-spread-and-spike-models
- [x] branch  <!-- feat/add-spread-and-spike-models -->
- [x] propose
- [x] review
- [x] tests-before
- [x] apply
- [x] tests-after
- [x] verify
- [ ] verify-runtime
- [ ] archive

### 005 — add-backtesting-and-evaluation
- [ ] branch
- [ ] propose
- [ ] review
- [ ] tests-before
- [ ] apply
- [ ] tests-after
- [ ] verify
- [ ] verify-runtime
- [ ] archive
```

---

## Error Handling Reference

| Situation                            | Action                                                                              |
|--------------------------------------|-------------------------------------------------------------------------------------|
| Working tree dirty at branch stage   | Stop, prompt user to stash or commit before proceeding                              |
| Branch already exists                | Ask: check out / recreate from main / abort                                         |
| Stage command fails                  | Set `Status=error` in progress file, stop, report to user                           |
| `spec-backlog/` missing              | Print message and stop                                                              |
| Iteration has no `propose.md`        | Mark iteration `skipped`, note reason, move on                                      |
| Change directory already exists      | Ask: continue / delete-and-recreate / abort                                         |
| Verify reports CRITICAL issues       | Fix them, re-run `/opsx:verify`, do not advance until clean                         |
| Stale artifact detected              | Warn user with options before proceeding                                            |
| Stage gate hit                       | Pause and wait for human approval                                                   |
| Tests fail in `tests-after`          | Auto-remediate up to `max_retries` times, then ask user                             |
| `verify-runtime` check fails         | Auto-remediate up to `max_retries` times, then ask user                             |
| API health check fails               | Auto-remediate (port conflict, env vars, AppHost startup), then ask user            |
| Context budget reached               | Save progress, output fresh-session instructions, stop                              |
| All iterations complete              | Report success, display final table, stop                                           |
| Rollback requested                   | Uncheck stages, delete/move downstream change directory, report                     |
| Archive prompt about incomplete tasks| If `verify` passed, accept the prompt; otherwise STOP and surface to user           |
