---
name: project-gitignore-before-impl
description: Audit and, if needed, update .gitignore from the active OpenSpec change or Spec Kit feature BEFORE implementation so the upcoming /opsx:apply or /speckit.implement does not accidentally commit generated artifacts, local secrets, tool caches, data volumes, or snapshot noise.
---

# Project .gitignore Before Implementation

## Purpose

Use this skill **before** `/opsx:apply` or `/speckit.implement` to make sure the repository's `.gitignore` is ready for the artifacts the upcoming change will produce. The goal is that when the implementation step starts creating new files, only intentional, reviewable files end up tracked — and nothing sensitive, generated, or machine-local leaks into the commit.

This is the `.gitignore` counterpart to `project-tests-before-impl`: it runs in the same pre-implementation slot, reads the same spec sources, and hands off to the same next command.

This skill is optimized for:
- C# / .NET
- React / TypeScript / Vite
- Python (FastAPI, ruff, pytest)
- .NET Aspire AppHost
- lakehouse-style data directories (Bronze / Silver / Gold)
- snapshot/approval test outputs

## Supported spec sources

This skill works with **either** spec system present in the repo:

1. **OpenSpec** — `openspec/changes/*/…`
   - run this skill *before* `/opsx:apply`
2. **Spec Kit** — `specs/*/spec.md`, `specs/*/plan.md`, `specs/*/tasks.md`
   - run this skill *before* `/speckit.implement` (after `/speckit.tasks` has produced `tasks.md`)

If both are present, prefer the one the user is currently discussing. If ambiguous, ask.

## When to use

Use this skill when:
- an OpenSpec change or Spec Kit feature has been **specified but not yet implemented**
- the spec introduces something likely to generate ignorable artifacts (a new tool, new data directory, new language/runtime, new test framework, new code generator, new local config file, new secret, new log destination)
- the user is about to run `/opsx:apply` or `/speckit.implement` and wants `.gitignore` hygiene handled first
- the user asks for gitignore review, "what should I ignore", or mentions that a recent change accidentally committed `bin/`, `.env`, caches, snapshots, or similar

Do not use this skill when:
- there is no active OpenSpec change and no active Spec Kit feature
- the spec clearly adds no new artifacts (pure refactor, doc-only change, rename)
- the user only wants to clean up already-committed noise — that is a separate cleanup task (git rm --cached + gitignore update after the fact)

## Inputs

Read these first, if present (whichever system is active):

**OpenSpec**
- `openspec/changes/*/tasks.md`
- `openspec/changes/*/specs/**/*.md`
- `openspec/changes/*/design.md`
- `openspec/changes/*/proposal.md`

**Spec Kit**
- `specs/*/spec.md`
- `specs/*/plan.md`
- `specs/*/tasks.md`
- `specs/*/contracts/**` (if present)
- `specs/*/research.md`, `specs/*/data-model.md`, `specs/*/quickstart.md` (if present)

If more than one active change/feature exists, prefer the most recent one or the one the user is currently discussing.

Then inspect:
- the existing top-level `.gitignore`
- any nested `.gitignore` files (e.g. `frontend/.gitignore`, `ml/.gitignore`)
- `.gitattributes` if present
- solution / project layout to understand where new files will land
- existing tool configs (`.editorconfig`, `package.json`, `pyproject.toml`, `Directory.Build.props`) for hints about what tools are in use

## Required behavior

### 1. Discover the active spec
Find the active OpenSpec change *or* Spec Kit feature and identify:
- new tools, frameworks, SDKs, or languages introduced
- new data / cache / output directories
- new local-only config files or environment variables
- new test infrastructure (snapshot frameworks, coverage tools, fixtures writing to disk)
- new code-generation steps (OpenAPI clients, EF migrations bundles, protobuf, T4, source generators with emitted files)
- new log / trace / telemetry destinations that write to the working tree
- anything that might legitimately land as a file but should never be committed

### 2. Build an ignore map
For every signal from step 1, classify the artifact and decide whether it needs a new `.gitignore` entry. Common categories:

- **Build output / intermediates**
  - `bin/`, `obj/`, `dist/`, `out/`, `build/`, `.next/`, `.turbo/`, framework-specific output dirs
  - only add if the spec introduces a *new* location not already covered

- **Tool caches**
  - `.ruff_cache/`, `.pytest_cache/`, `.mypy_cache/`, `.tox/`, `.venv/`, `node_modules/`
  - test coverage: `coverage/`, `*.coverage`, `coverage.cobertura.xml`, `TestResults/`
  - formatter/linter state: `.eslintcache`, `.stylelintcache`

- **Snapshot / approval test artifacts**
  - `*.received.*` (keep `*.verified.*` tracked)
  - approval test output folders

- **Generated code / assets**
  - generated OpenAPI clients, protobuf stubs, T4 outputs, emitted source generator files that live in the working tree
  - only ignore if the spec clearly treats them as generated and reproducible; otherwise they may be intentionally tracked

- **Local config / secrets**
  - `.env`, `.env.local`, `.env.*.local`, `appsettings.Development.local.json`, `secrets.json`, user-secrets stores
  - any token / credential file implied by the spec (`*.pem`, `*.pfx`, `*.key`) — with care, since some may be intentionally tracked test fixtures

- **Data volumes (lakehouse-style)**
  - new subtrees under `data/` (e.g. `data/raw/`, `data/staging/`) following the existing pattern:
    - ignore contents: `data/<tier>/*`
    - keep placeholder: `!data/<tier>/.gitkeep`

- **Logs / traces / local telemetry**
  - `*.log`, `logs/`, OpenTelemetry exporter file sinks, crash dumps (`*.dmp`)

- **Editor / OS noise**
  - only if the spec introduces a new editor or toolchain that is not already covered

### 3. Diff against the current `.gitignore`
For each proposed entry:
- check whether it is already covered (exact match, glob match, or superset pattern)
- if already covered, **do not add a duplicate**
- if partially covered (e.g. repo ignores `dist/` at root but the spec adds a new `packages/foo/dist/`), decide whether the existing pattern is sufficient (it usually is for unanchored patterns) or whether an anchored entry is warranted

Prefer the repository's existing style:
- unanchored vs. anchored patterns (most of the current file is unanchored — match that)
- grouping under existing section headers (`# Build results`, `# Node`, `# Python`, `# Data volumes (local dev)`, etc.)
- casing patterns (`[Bb]in/` style) only where the existing file already uses them

### 4. Propose the minimal change
Produce a single proposed patch to `.gitignore` (and to nested `.gitignore` files only if that is the repo's established pattern). The patch should:
- add only entries justified by the active spec
- group new entries under the most appropriate existing section, or add a new section header that matches the file's style if no section fits
- avoid sweeping "just in case" additions (no wholesale import of the GitHub templates — the repo already chose a streamlined subset)
- avoid reordering or reformatting unrelated lines

If a proposed entry is debatable (e.g. a generated file that *might* be checked in intentionally), list it under "needs decision" rather than silently adding it.

### 5. Apply and verify
After editing `.gitignore`:
- run `git check-ignore -v <path>` for a representative file per new entry to confirm the pattern matches what it should
- run `git status` to confirm nothing that *was* tracked has become ignored (ignoring an already-tracked file is a footgun — the file stays tracked but future changes become confusing; call it out)
- run `git ls-files -ci --exclude-standard` to surface any already-tracked files that are now matched by an ignore pattern; report them, do **not** `git rm --cached` without explicit user approval

### 6. Preserve repository conventions
Follow existing:
- section ordering and headers
- pattern style (anchored vs. unanchored, casing brackets)
- placeholder convention for data directories (`.gitkeep` + negation)
- scope (root `.gitignore` vs. nested)

Do not introduce `.gitattributes`, `.gitkeep`, or nested `.gitignore` files unless the spec or existing repo pattern calls for them.

### 7. Hand off to implementation
Report the updated `.gitignore` and clearly hand off to the implementation step:
- for OpenSpec: the user should next run `/opsx:apply`
- for Spec Kit: the user should next run `/speckit.implement`

The implementation step will create the files the spec describes; with `.gitignore` already correct, generated artifacts will be ignored from their first appearance.

### 8. Report gaps and risks
At the end, report:
- entries added and why (which spec signal each one traces to)
- entries considered and skipped because already covered
- entries flagged as "needs decision" (possibly-tracked generated files, ambiguous fixtures)
- any already-tracked files now matched by a new ignore pattern (with a recommendation but no destructive action)
- any spec signals that did not map to a `.gitignore` change but might warrant `.gitattributes`, LFS, or secret-management follow-up

## Specific guidance for this repository type

### For new data tiers under `data/`
The existing pattern is:
```
data/<tier>/*
!data/<tier>/.gitkeep
```
When a spec introduces a new tier (e.g. `data/raw/`, `data/archive/`), follow the same pattern and add a matching `.gitkeep` placeholder under the new directory so the directory is preserved but its contents are ignored.

### For new .NET projects
Most build output (`bin/`, `obj/`) is already covered by the root file's casing-bracket patterns. Do **not** add per-project `bin/obj` entries. Only add new entries for genuinely new output locations (e.g. `artifacts/` subtrees the existing entry does not already catch, NuGet local feeds, user-secrets-adjacent files).

### For new frontend tooling
If a spec adds a framework with a new cache/output dir (`.next/`, `.turbo/`, `.parcel-cache/`, `.svelte-kit/`, Storybook `storybook-static/`), add the specific dir. Do not expand the existing `node_modules/` / `dist/` rules unless the new tool truly writes elsewhere.

### For new Python tooling
If a spec adds `mypy`, `tox`, `hypothesis`, `pytest-cov`, etc., add only the cache/output dirs those tools produce (`.mypy_cache/`, `.tox/`, `.hypothesis/`, `.coverage`, `htmlcov/`). The existing `.ruff_cache/` and `__pycache__/` entries already cover current tooling.

### For snapshot / approval tests
If the spec introduces `Verify.Xunit` (or similar) as part of the test strategy, add `*.received.*` to ignore rejected snapshots while keeping `*.verified.*` tracked. Do not blanket-ignore the whole snapshot directory.

### For secrets and local config
Prefer explicit filenames the spec names (e.g. `appsettings.Local.json`) over broad globs like `*.local.json` that might catch intentionally-tracked fixtures. If the spec introduces credentials that must never be committed, consider also suggesting that the user configure `git-secrets` or a pre-commit hook — but that is a recommendation, not something this skill installs.

### For Aspire and orchestration
Aspire itself does not usually require new ignore entries. If a spec adds a local orchestration artifact (e.g. a generated `aspire-manifest.json` the team does not want tracked, a local dashboard export), ignore that specific file. Do not speculatively ignore Aspire config files — they are usually intentionally tracked.

## Output expectations

When this skill finishes, it should provide:

1. A short summary of `.gitignore` entries added, grouped by category
2. A spec-signal-to-entry mapping (which spec bullet motivated which entry)
3. `git check-ignore` verification for representative paths
4. A list of entries considered but skipped (already covered)
5. A list of "needs decision" entries the user should confirm
6. Any already-tracked files now matched by new patterns (report only; do not remove)
7. The recommended next command (`/opsx:apply` or `/speckit.implement`)

## Editing rules

- Only edit `.gitignore` (and nested `.gitignore` files if the repo already uses them for this scope)
- Do not edit production code, tests, or CI config
- Do not run `git rm --cached` or otherwise untrack files without explicit user approval
- Do not reformat or reorder unrelated lines
- Do not import wholesale gitignore templates — add only what the spec justifies
- Prefer the existing file's style over "more correct" style from external templates
- If unsure whether a file should be ignored or tracked, list it under "needs decision" and stop

## Completion checklist

- Active spec source identified (OpenSpec change or Spec Kit feature)
- Relevant artifact signals extracted from the spec
- Ignore map built and diffed against existing `.gitignore`
- Minimal patch applied in repo style
- `git check-ignore -v` verification run for representative paths
- Already-tracked-but-now-ignored files reported (not removed)
- "Needs decision" entries flagged for the user
- Handoff to `/opsx:apply` or `/speckit.implement` stated
