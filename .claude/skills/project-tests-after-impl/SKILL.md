---
name: project-tests-after-impl
description: After /opsx:apply or /speckit.implement, fill test gaps the acceptance tests did not cover — add unit tests for edge cases, snapshot/approval tests for stable outputs, regression tests for bugs found during implementation, and policy tests that depend on final layout. Targets diff coverage (line ≥ 80%, branch ≥ 70%) and one direct test per new public API.
---

# Project Tests After Implementation

## Purpose

Use this skill **after** `/opsx:apply` or `/speckit.implement` to close the gap between "the acceptance tests pass" and "the change is actually well-tested".

This skill is the counterpart to `project-tests-before-impl`:

| Skill | When | Focus |
|---|---|---|
| `project-tests-before-impl` | before `/opsx:apply` / `/speckit.implement` | acceptance tests traced 1:1 to spec scenarios; expected red |
| `project-tests-after-impl` (this one) | after implementation | edge cases, snapshots of stable outputs, regressions, policy tests, characterization |

Do not re-generate acceptance tests here. Those are already in place from the before-skill. This skill is for the tests you could not reasonably write before the implementation existed.

This skill is optimized for:
- C# / .NET
- xUnit
- Aspire AppHost
- unit + snapshot/approval tests
- architecture / policy tests
- regression tests

## Supported spec sources

Works with either:

1. **OpenSpec** — `openspec/changes/*/…` (run after `/opsx:apply`)
2. **Spec Kit** — `specs/*/…` (run after `/speckit.implement`)

If both are present, prefer the one the user is currently discussing.

## When to use

Use this skill when:
- an OpenSpec change or Spec Kit feature has been **implemented** (production code exists and builds)
- acceptance tests from `project-tests-before-impl` are green
- the user wants to harden the change with additional automated tests
- there are new public APIs, new DTOs, new serialized outputs, or new branching logic added by the change
- bugs were found and fixed during implementation and have no regression test yet

Do not use this skill when:
- the change has not been implemented (use `project-tests-before-impl` instead)
- the acceptance tests from the before-skill are still red (drive those green first)
- there is no active OpenSpec change and no active Spec Kit feature

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
- `specs/*/contracts/**`, `specs/*/data-model.md`, `specs/*/research.md`, `specs/*/quickstart.md` (if present)

Then inspect:
- the git diff / recently changed files for the current change — this is the scope of "diff coverage"
- existing test projects (`backend/tests/GasLens.Unit`, `backend/tests/GasLens.Integration`, `backend/tests/GasLens.Contract`)
- existing acceptance tests produced by `project-tests-before-impl`
- any snapshot/approval infrastructure (e.g. Verify, ApprovalTests) already in use
- Aspire AppHost, API, worker, scheduler projects
- test utilities / fixtures / shared test infrastructure

## Required behavior

### 1. Determine the diff scope
Identify the set of files added/modified by the change. That set is the scope for coverage targets — repo-wide coverage is not the metric here.

### 2. Inventory what acceptance tests already cover
Before writing anything new, read the tests produced by `project-tests-before-impl` and list which scenarios, APIs, and code paths are already exercised. Avoid duplicating them.

### 3. Identify gap categories
For the diff scope, enumerate gaps in each category:

- **Unit tests — edge cases and branches**
  - input validation, null/empty/boundary values
  - error paths, exception types and messages
  - branches in business rules not hit by acceptance tests
  - parsing / mapping / transformation corners

- **Snapshot / approval tests — stable serialized outputs**
  - API response shapes (JSON)
  - generated SQL / migrations
  - generated files, code generation output
  - log/event schemas
  - DTO serialization
  - use the snapshot framework already in the repo if one exists; otherwise prefer `Verify.Xunit`. Do not introduce a second snapshot framework.

- **Regression tests**
  - for each bug discovered and fixed during implementation, add a failing-before / passing-after test
  - link the test name or comment to the fix commit / issue only if a durable identifier exists (no "fixes the thing from yesterday")

- **Architecture / policy tests (post-layout)**
  - project reference boundaries now that the final layout is known
  - forbidden dependencies introduced by the change
  - "no hardcoded localhost / ports" scan of new files
  - options/config binding in new code paths

- **Characterization tests**
  - if the change touched untested legacy code incidentally, lock current behavior with a characterization test before future refactors can change it

### 4. Write the tests
Create or update real test files in the existing test projects. Prefer extending existing test projects and test classes over new ones.

Follow existing repository conventions (namespaces, fixture style, assertion style, naming, packages, directory layout). Do not introduce new libraries unless clearly necessary.

### 5. Run verification — expect green
After generating tests:
- run build
- run relevant test projects
- the new tests should pass (unless they are regression tests explicitly reproducing an unfixed bug — rare at this stage)
- if a new test fails, decide: is it revealing a real defect (fix the production code, separately and minimally) or is the test wrong (fix the test)?
- do not make broad unrelated refactors

### 6. Measure diff coverage against targets
Compute or estimate coverage scoped to the diff:

- **Target: line coverage ≥ 80% on changed files**
- **Target: branch coverage ≥ 70% on changed files**
- **Hard rule: every public API added by the change has at least one direct test**
- **Hard rule: every stable serialized output added by the change has a snapshot test, unless explicitly deemed unstable**

These are *targets*, not CI gates — the skill reports against them, the user decides whether to invest more. If a coverage tool is already wired up (coverlet, `dotnet test --collect:"XPlat Code Coverage"`), use it; otherwise report qualitatively and list uncovered branches explicitly.

### 7. Report
At the end, report:
- tests added, grouped by category (unit / snapshot / regression / policy / characterization)
- diff coverage numbers (or qualitative summary if no tool is available) against the targets above
- new public APIs and whether each has a direct test
- new serialized outputs and whether each has a snapshot
- remaining gaps with justification ("not worth automating because …")

## Specific guidance for this repository type

### Snapshot tests
- If the repo already uses `Verify.Xunit` or similar, use it. Check for `*.verified.*` files or `Verifier.Verify(...)` usage.
- If no snapshot framework is present and the change genuinely benefits from snapshots, prefer `Verify.Xunit` — but call this out in the report rather than silently adding a dependency.
- Snapshot DTOs/JSON responses, not entire HTML pages. Huge snapshots are a churn magnet.
- Commit `.verified.*` files; gitignore `.received.*`.

### Aspire AppHost
- For post-impl policy tests, verify the final set of registered resources matches the spec, including any conditional registration introduced by the change.

### "No hardcoded localhost or ports" (post-impl)
- Re-run the architecture scan specifically against files added by the change, in addition to whatever the before-skill already covers repo-wide.

### Environment/config
- For new options classes, assert they bind from configuration and that required fields are validated.

## Output expectations

When this skill finishes, it should provide:

1. Summary of tests added, grouped by category
2. Diff coverage report against targets (line ≥ 80%, branch ≥ 70%)
3. Public API / snapshot checklist
4. Remaining gaps with justification
5. Any manual verification items still outstanding

## Editing rules

- Keep changes minimal and local to test projects
- If a new test reveals a real defect, fix the production code in a separate, minimal edit and say so in the report
- Do not refactor production code for style or aesthetics
- Prefer deterministic tests over fragile end-to-end behavior
- Avoid network dependencies on external services
- Do not duplicate acceptance tests from `project-tests-before-impl`

## Test naming guidance

Use names such as:
- `ParseGasFlow_returns_empty_when_payload_missing_nominations`
- `CreateForecastEndpoint_response_shape_matches_snapshot`
- `Regression_scheduler_does_not_double_fire_when_clock_skews_backwards`
- `ModelWorker_project_must_not_reference_Api`
- `ForecastOptions_binds_required_fields_from_configuration`

## Completion checklist

- Active spec source identified (OpenSpec change or Spec Kit feature)
- Implementation confirmed present and acceptance tests confirmed green
- Diff scope identified
- Existing acceptance-test coverage inventoried (no duplication)
- Gap categories enumerated
- Tests added in the correct category and project
- Build executed
- Tests executed and green (or intentionally red for unfixed regressions)
- Diff coverage reported against targets
- Public API / snapshot checklist reported
- Remaining gaps called out with justification
