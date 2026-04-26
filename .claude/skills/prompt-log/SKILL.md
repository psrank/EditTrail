---
name: "prompt-log"
description: "Log an ad-hoc prompt as a polished, self-contained, documentation-quality version — rephrased from the original, with clarifications folded in — together with the agent's findings, what was done, and the iteration it belongs to. Optimized for re-use AND documentation: the saved prompt is what the user *wishes* they had asked. Use when: a coding agent has just finished an ad-hoc task (bug fix, tweak, chore, one-off request), the user wants to capture the prompt that drove it, the user says 'log this prompt' / 'save this' / 'remember this prompt' / 'log this fix'."
argument-hint: "Optional short slug for the filename (e.g. 'auth-token-loop'). If omitted, the skill derives one from the task subject."
compatibility: "Works with spec-kit (specs/NNN-name/) and openspec (openspec/changes/) projects, and falls back to the git branch name if neither is present."
metadata:
  author: "local"
  source: "local/skills/prompt-log"
  version: "3.0"
user-invocable: true
disable-model-invocation: false
---

## User Input

```text
$ARGUMENTS
```

If `$ARGUMENTS` is non-empty, treat it as the **slug** to use in the filename (kebab-case). If empty, derive a slug from the task subject in Step 3.

---

## Purpose

Capture an ad-hoc prompt → outcome interaction as a **polished, reusable, documentation-quality artifact**. The user's original input is the seed; the saved entry is the **rephrased, improved version they wish they had written**. The log doubles as a prompt library and as project documentation, so the rephrased prompt must read well as standalone documentation — clear, specific, self-contained, and copy-pasteable to a fresh agent without surrounding conversation context.

This is **not** a commit log, a postmortem, or a changelog. It is a curated prompt library indexed by task.

Scope: any ad-hoc prompt is fair game — bug fixes, small tweaks, chores, recurring maintenance requests, one-off scripts, refactors. The unifying property is **"I might want to run this prompt again, or hand it to a teammate."**

---

## Execution Steps

### Step 1 — Detect the iteration

Determine which iteration / change this prompt belongs to. Try in order:

1. **Spec-kit**: run `bash .specify/scripts/bash/check-prerequisites.sh --json --paths-only 2>/dev/null` from the repo root. If it succeeds, parse `BRANCH` (e.g. `005-spread-and-spike-models`) — that is the iteration.
2. **OpenSpec**: if `openspec/changes/` exists and contains a single in-progress change directory (no `archive/` parent), use that change name.
3. **Fallback**: use the current git branch (`git rev-parse --abbrev-ref HEAD`). If branch is `main` / `master`, set iteration to `post-<last-iteration>` by reading the most recently modified directory under `specs/` or `openspec/changes/archive/`.
4. If none of the above resolves, ask the user: *"Which iteration should this prompt be tagged with?"*

Record:
- `iteration` — the identifier above
- `iteration_kind` — `speckit` | `openspec` | `branch` | `manual`
- `branch` — current git branch
- `commit` — `git rev-parse --short HEAD`

### Step 2 — Rephrase the prompt (the centerpiece)

Find the **user input(s) that drove this work** in the current conversation — usually the most recent user message that described a task, defect, tweak, or request, plus any clarifying follow-ups that adjusted the scope.

Then **rewrite it as a polished, documentation-quality prompt**. The saved version is what the user *wishes* they had asked — not what they actually typed. Goals:

- **Self-contained.** A reader / fresh agent must be able to act on it without seeing this conversation. Spell out file paths, project names, ports, formats — do not say "this project" or "the script we discussed".
- **Specific.** Replace vague verbs ("run everything", "clean it up") with concrete deliverables and acceptance criteria.
- **Fold in clarifications.** If the user clarified scope mid-conversation (e.g. "actually I just mean start the API"), bake the clarified scope into the rephrased prompt. Do not preserve the back-and-forth.
- **Preserve intent, not scope creep.** Do not invent requirements the user did not actually want. If the agent learned constraints during execution that the user implicitly accepted (a port number, a flag), include them; if you're unsure, leave them out.
- **Fix grammar and structure.** Polish to clean prose. If multiple deliverables exist, structure as a short bullet list. Use code spans for paths, commands, identifiers.
- **Stay tight.** Aim for ≤ 250 words. The rephrased prompt is a brief, not a spec.

If the prompt is so context-dependent that rephrasing risks distorting it, ask the user once: *"Should I log a rephrased version, or paste a polished prompt yourself?"* — but default to rephrasing.

Optionally, if the original wording carried important nuance (a specific error message, an exact file path the user typed), include it inside the rephrased prompt as a short quoted snippet — but the surrounding prompt is your polished version, not theirs.

### Step 3 — Build the slug and title

- **Slug**: if `$ARGUMENTS` was provided, use it. Otherwise, derive a 2–5 word kebab-case slug from the task subject (e.g. `auth-token-refresh-loop`, `entsoe-timezone-off-by-one`, `add-startup-script`). Lowercase, ASCII, no punctuation.
- **Title**: a single-sentence human description of the task (≤ 80 chars), written by you from the prompt + findings.

### Step 4 — Summarize findings and outcome

From the conversation and recent edits, fill two short sections. Keep them tight — they are context, not the main artifact.

- **Findings**: 2–6 bullets. For bug fixes, root cause and where. For other tasks, the relevant context the agent uncovered (current state, gaps, constraints). Reference files as `path:line`. Skip narration of the investigation.
- **What Was Done**: 2–6 bullets on what the agent changed or produced. List the files touched (use `git diff --name-only HEAD` if staged/committed, or the edits made this turn), the core change, and any follow-up notes (e.g. "added regression test at …").

If the work is committed, also record the commit SHA via `git log -1 --pretty=%H`.

### Step 5 — Write the log file

Path: `prompt-log/<YYYY-MM-DD>-<iteration-tag>-<slug>.md` from repo root.

Where `<iteration-tag>` is:
- the leading number of a spec-kit iteration if present (e.g. `005`),
- the openspec change name otherwise,
- `branch` for branch-only fallbacks.

Create the `prompt-log/` directory if it does not exist.

File template:

```markdown
---
title: <Title from Step 3>
slug: <slug>
iteration: <iteration identifier>
iteration_kind: <speckit | openspec | branch | manual>
branch: <branch name>
commit: <short sha at time of work>
date: <YYYY-MM-DD>
status: done
recurrences: 0
tags: []
---

# <Title>

## Reusable Prompt

> Polished, self-contained version. Paste this to a coding agent (or use as documentation) when this task comes up again.

```text
<rephrased, improved prompt from Step 2>
```

## Context When First Run

- **Iteration**: <iteration> (<iteration_kind>)
- **Branch**: <branch>
- **Commit at completion**: <short sha>
- **Date**: <YYYY-MM-DD>

## Findings

- <bullet>
- <bullet>

## What Was Done

- <bullet — files touched, core change>
- <bullet>

## Recurrence Log

<!-- Append a dated bullet here each time this prompt is re-run. Bump `recurrences` in frontmatter. -->
```

Do **not** add commentary, AI-generated summaries, or "what we learned" sections. Keep the file lean.

### Step 6 — Update the index

Path: `prompt-log/INDEX.md`.

If the file does not exist, create it with this header:

```markdown
# Prompt Log Index

Reusable prompts captured by `/prompt-log`. When a task or bug comes back up, find its row, open the file, and re-run the prompt.

| Date | Iter | Slug | Title | Recurrences | File |
|------|------|------|-------|-------------|------|
```

Append a new row for this entry. Keep rows newest-first (insert directly under the header row). Use a relative markdown link in the `File` column.

If a row with the same `slug` already exists (the prompt is being re-logged because the task came back):
- **Do not** create a new file. Open the existing file.
- Append a dated bullet to its `## Recurrence Log` section: `- <YYYY-MM-DD> re-run on iteration <iteration> at commit <sha>. <one-line note>`.
- Increment `recurrences` in the frontmatter.
- In `INDEX.md`, update that row's `Recurrences` cell and `Date` (most recent).
- Tell the user clearly: *"This prompt has been logged N times — appended to existing log instead of creating a new one."*

### Step 7 — Report

Output a short confirmation:

```
[prompt-log] Saved → prompt-log/<filename>.md
  iteration: <iteration>
  slug: <slug>
  prompt length: <N chars>
  recurrences: <N>
```

Do not print the full prompt back to the user — they just sent it.

---

## Notes for the agent running this skill

- **Polished, not verbatim.** The saved prompt is the rephrased, improved version the user *wishes* they had asked. Fix grammar, expand vague verbs into concrete deliverables, fold in clarifications, replace "this" / "that" / "the thing we discussed" with the actual identifier. The original wording is discarded — we never quote `you should update readme file and also create start up script` again.
- **Self-contained.** A reader who has not seen the conversation must be able to act on the prompt. Spell out paths, ports, commands, formats. Do not say "this script" — name the file.
- **Don't invent scope.** Polish and clarify, but do not add deliverables the user did not ask for. If the agent's implementation included something the user did not explicitly request (a new flag, a refactor), include it only if the user implicitly accepted it during the conversation.
- **Prompt-first layout is non-negotiable.** The "Reusable Prompt" section comes immediately after the title. Findings / what-was-done sit below.
- **Don't gold-plate the surrounding sections.** No prose explanations, no markdown art, no "Lessons Learned". The polished prompt is the centerpiece; the rest is context.
- **Don't auto-commit.** Leave the new file unstaged unless the user asks to commit.
- **Anything ad-hoc is in scope.** Bug fixes, tweaks, chores, one-off scripts, recurring maintenance, refactors — if the user might want to re-run the same prompt or hand it to a teammate as documentation, log it. Long-running structured features generally belong to spec-kit / openspec instead, but if the user explicitly asks to log them here, do it without arguing.
