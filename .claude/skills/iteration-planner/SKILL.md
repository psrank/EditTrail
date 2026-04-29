---
name: "iteration-planner"
description: "Interactively plan one or more iterations before any spec is written. Captures the user's intent, splits large work into manageable iterations, drafts spec/plan files inline for review, and only writes them to spec-backlog/ after the user explicitly approves. Works with both speckit (NNN-<slug>-spec.md + NNN-<slug>-plan.md, plus optional NNN-constitution-update-*.md) and openspec (NNN-<slug>-proposal.md as the foundation). Use when: 'plan a new iteration', 'help me draft a spec', 'add this to the backlog', 'break this work into iterations', 'design the next chunk of work'."
argument-hint: "Optional: a high-level description of the work, and/or 'speckit' or 'openspec' to fix the framework"
compatibility: "Repository must contain a spec-backlog/ directory. For speckit, .specify/ should exist. For openspec, openspec/ should exist."
metadata:
  author: "local"
  source: "local/skills/iteration-planner"
  version: "1.0"
user-invocable: true
disable-model-invocation: false
---

## User Input

```text
$ARGUMENTS
```

## Goal

Plan one or more iterations end-to-end with the user, and write polished, autorun-ready files into `spec-backlog/` **only after the user has approved both the breakdown and the content**. Output filenames and section shapes must match the conventions of existing `spec-backlog/` entries so downstream tooling (`/speckit-autorun`, OpenSpec proposal flow) picks them up without manual cleanup.

This skill is the **planning-phase** counterpart to `/speckit-specify` and `/speckit-plan`. It does not run them — it stages the backlog inputs they read.

## Phase 1 — Choose the framework

Fix the framework **before** planning. State the choice in your first reply and keep it visible until files are written.

Decide as follows, in order:

1. If `$ARGUMENTS` contains `speckit` or `openspec`, use that.
2. Detect at the repo root:
   - `.specify/` present → propose **speckit**
   - `openspec/` present → propose **openspec**
   - both → ask the user which one
   - neither → ask the user, and note that the chosen framework appears not to be initialized
3. If the user has prior iterations in `spec-backlog/`, note the dominant suffix pattern (`-spec.md` + `-plan.md` ⇒ speckit; `-proposal.md` ⇒ openspec) and use it as a tiebreaker.

Open every subsequent reply during this skill with one short header line, e.g.:

> **Framework: speckit** — files will land at `spec-backlog/NNN-<slug>-spec.md` and `spec-backlog/NNN-<slug>-plan.md`.

## Phase 2 — Discover the existing backlog

1. List `spec-backlog/*.md` (Glob).
2. Determine the next iteration number:
   - Highest `NNN-` prefix among `spec-backlog/*.md` **and** (for speckit) `specs/NNN-*` directories.
   - Next number = `max + 1`, zero-padded to 3 digits.
   - If the user requests a specific number, use it but warn on collision.
3. Read the most recent 1–2 iterations to mirror their voice, depth, and section ordering. New drafts must look like siblings of the latest entries.

## Phase 3 — Gather the request

If `$ARGUMENTS` already carries a description, use it. Otherwise ask **one** focused question that captures:

- the outcome the user wants,
- the role who benefits,
- known constraints ("no new dependency", "local filesystem only", a deadline, etc.).

One round-trip — not a survey. If the user replies tersely, fill obvious blanks with informed guesses and flag them under **Assumptions** in the draft rather than asking again.

## Phase 4 — Assess scope and propose a breakdown

Decide whether the work fits one iteration or must be split. Split when **any** of the following holds:

- More than ~3 user stories that could ship independently.
- Mixes foundations + features (foundations in iteration N, features in N+1, N+2).
- Spans UI + backend + data and each layer could be sequenced.
- Estimated work > ~1 focused week for a single developer.
- The user explicitly says "this is big" or names sub-areas.

Present the breakdown as a numbered list before drafting any spec content:

```text
NNN-<slug>: <one-line goal>
  Depends on: <prior iterations | none>
  Why a separate iteration: <one line>
  Approx size: small | medium | large
```

Wait for the user's reaction. Iterate on the list — merge, split, reorder, drop, rename — until the user says it's good. **Do not draft specs until the breakdown is agreed.**

## Phase 5 — Draft inline (do not write to disk yet)

For each agreed iteration, draft the file(s) and present them inline as fenced markdown blocks for review. Match the heading shapes used by recent `spec-backlog/` entries.

### speckit drafts (two files per iteration)

`NNN-<slug>-spec.md`:

```text
# Feature Specification: <Title>

**Feature Branch**: `NNN-<slug>`
**Status**: Draft
**Input**: "<original user request, quoted verbatim>"

## Goal

## User Scenarios
### User Story 1 - <name>
As a <role>, I want <outcome> so that <benefit>.
#### Acceptance
- ...

## Requirements
### Requirement: <name>
...

## Acceptance Criteria
- ...

## Constraints
- ...
```

`NNN-<slug>-plan.md`:

```text
# Implementation Plan: <Title>

**Branch**: `NNN-<slug>`
**Spec**: `specs/NNN-<slug>/spec.md`
**Status**: Draft

## Summary

## Architecture

## <component sections — backend modules, frontend, data shapes, UI, etc.>

## Testing Strategy

## Migration Plan

## Definition of Done

## Out of Scope
```

### openspec drafts (foundation-first)

`NNN-<slug>-proposal.md` — the foundation document, OpenSpec change-proposal shape:

```text
# Proposal: <Title>

**Change ID**: `NNN-<slug>`
**Status**: Draft
**Input**: "<original user request, quoted verbatim>"

## Why

## What Changes
- ...

## Impact
- Affected capabilities: ...
- Affected code: ...
- Risks / migration: ...

## Open Questions
- ...
```

Add a sibling `NNN-<slug>-design.md` **only** when the proposal alone is too thin to drive implementation (genuinely complex design decisions, novel architecture). Default is: proposal-only.

### Iterate to approval

Present drafts, ask for review, accept edits, re-present. Continue until the user explicitly approves (e.g. "looks good", "ship it", "write them"). Tacit silence is **not** approval.

## Phase 6 — Constitution update (speckit only, optional)

Once the iteration drafts are approved, ask exactly once:

> Does this iteration require a project-constitution change (a new principle, rule, or governance constraint)?

If yes, draft an extra file in the same shape as `spec-backlog/014-constitution-update-mantine-design-skill.md`:

`NNN-constitution-update-<topic>.md`:

```text
# Constitution Update: <Topic>

**Applies From Iteration**: `NNN-<slug>`
**Status**: Draft

## Principle: <Name>

## <Numbered rule sections>
```

This skill only **stages** the proposal in `spec-backlog/`. It does not amend `.specify/memory/constitution.md`. If the user wants the amendment applied right away, tell them to run `/speckit-constitution` afterwards.

## Phase 7 — Write the files

Only after explicit user approval:

1. Use the `Write` tool, one call per file.
2. Paths:
   - speckit: `spec-backlog/NNN-<slug>-spec.md`, `spec-backlog/NNN-<slug>-plan.md`, optional `spec-backlog/NNN-constitution-update-<topic>.md`.
   - openspec: `spec-backlog/NNN-<slug>-proposal.md`, optional `spec-backlog/NNN-<slug>-design.md`.
3. After writing, output a short summary:
   - each path written,
   - the next recommended command:
     - speckit → `/speckit-autorun` (or `/speckit-specify` to run only this iteration),
     - openspec → "ready for OpenSpec processing" (or the project's local proposal command if known from `openspec/` config).

## What this skill must NOT do

- Write files for iterations the user did not explicitly approve.
- Touch `specs/`, `.specify/memory/constitution.md`, `openspec/` directly — this skill only stages the backlog.
- Update `spec-backlog/autorun-progress.md` — `/speckit-autorun` regenerates that itself.
- Run downstream skills (`/speckit-specify`, `/speckit-plan`, etc.). Suggest them at the end; do not invoke them.
- Skip the framework declaration. If you cannot determine the framework, ask before drafting.

## Style rules for generated files

- Mirror the heading shape of the most recent 1–2 files in `spec-backlog/`. Do not invent new section names if a sibling pattern already exists.
- Use plain bulleted text for module names, paths, and statuses inside fenced ` ```text ` blocks — matches existing files.
- Quote the user's original request verbatim under **Input**.
- All dates in ISO format (YYYY-MM-DD).
- Slugs are lowercase, hyphenated, 2–5 words, action-noun where natural (`resilient-ingestion-coverage`, `mantine-design-system-audit`).
- Iteration numbers are 3-digit zero-padded.
- No emojis unless the user asks.
