---
name: project-tests-before-impl
description: Generate C#/.NET tests from the active OpenSpec change or Spec Kit feature BEFORE implementation (TDD), run them to confirm they fail for the right reason, then hand off to /opsx:apply or /speckit.implement.
---

# Project Tests Before Implementation

## Purpose

Use this skill **before** `/opsx:apply` or `/speckit.implement` to turn spec scenarios into real automated tests that are expected to fail until the implementation lands. This enforces a test-first (TDD) workflow: tests are written from the spec, confirmed red, and then handed to the implementation step to drive to green.

This skill is optimized for:
- C# / .NET
- xUnit
- Aspire AppHost
- integration tests
- architecture / policy tests
- config validation tests

## Supported spec sources

This skill works with **either** of the following spec systems present in the repo:

1. **OpenSpec** — `openspec/changes/*/…`
   - run this skill *before* `/opsx:apply`
2. **Spec Kit** — `specs/*/spec.md`, `specs/*/plan.md`, `specs/*/tasks.md`
   - run this skill *before* `/speckit.implement` (after `/speckit.tasks` has produced `tasks.md`)

If both are present, prefer the one the user is currently discussing. If ambiguous, ask.

## When to use

Use this skill when:
- an OpenSpec change or Spec Kit feature has been **specified but not yet implemented**
- `tasks.md` (OpenSpec or Spec Kit) exists and lists work that should be verifiable
- requirements / acceptance criteria / scenarios exist that should become tests
- the user is about to run `/opsx:apply` or `/speckit.implement` and wants tests first
- the user asks for test generation from spec, BDD scenarios, requirements, or acceptance criteria

Do not use this skill when:
- there is no active OpenSpec change and no active Spec Kit feature
- the spec has no testable scenarios or acceptance criteria yet (run `/speckit.clarify` or flesh out the spec first)
- the repository has no .NET test projects and the user asked only for a plan rather than code

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
- solution / project layout
- existing test projects
- Aspire AppHost
- API / worker / scheduler projects
- any existing architecture test project
- test utilities / fixtures / shared test infrastructure

## Required behavior

### 1. Discover the active spec
Find the active OpenSpec change *or* Spec Kit feature and identify:
- requirements / acceptance criteria / user stories
- scenarios under each requirement (BDD-style if present)
- tasks that imply verification
- code areas the change is expected to touch (may not yet exist — that is fine)

### 2. Build a verification map
For every scenario, classify the best test type:

- **Integration test**
  - service startup
  - Aspire resource health
  - HTTP behavior
  - message flow
  - persistence integration

- **Architecture / policy test**
  - no hardcoded localhost or port literals
  - project reference boundaries
  - forbidden dependencies
  - configuration must come from environment / options

- **Unit test**
  - pure logic
  - small business rules
  - parsing / mapping / transformation

- **Smoke / command test**
  - host boots
  - expected services are registered
  - application starts without obvious runtime misconfiguration

### 3. Generate tests first (TDD)
Create or update real test files **before** the implementation exists. Prefer extending existing test projects rather than creating unnecessary new ones.

Recommended conventions:
- `tests/ArchitectureTests`
- `tests/IntegrationTests`
- `tests/UnitTests`

For this repository, target the existing `backend/tests/GasLens.Unit`, `backend/tests/GasLens.Integration`, and `backend/tests/GasLens.Contract` projects unless a new project is clearly justified.

Use xUnit unless the repository clearly uses another .NET framework already.

Because the implementation does not yet exist, tests may reference types, endpoints, options, or DI registrations that are not yet present. That is expected and desirable — these tests should fail on missing symbols or unmet assertions, not on unrelated issues.

### 4. Match scenario intent, not wording
Do not copy scenario text blindly into code.
Translate each scenario into:
- test name
- setup
- action
- assertions

### 5. Preserve repository conventions
Follow existing:
- namespaces
- fixture style
- assertion style
- naming conventions
- package choices
- directory layout

Avoid introducing new test libraries unless clearly necessary.

### 6. Run verification — expect red
After generating tests:
- run build
- run relevant test projects
- **confirm the new tests fail for the right reason** (missing type/endpoint/behavior described by the spec), not for unrelated reasons (typos, wrong namespace, missing package, broken fixture)
- fix only setup/compile issues in the new tests themselves; do not start implementing the feature to make tests pass
- do not make broad unrelated refactors

If a new test accidentally passes before any implementation exists, treat it as suspect: either the scenario was already satisfied (note it and move on) or the test is not actually asserting the intended behavior (strengthen it).

### 7. Hand off to implementation
Report the red tests and clearly hand off to the implementation step:
- for OpenSpec: the user should next run `/opsx:apply`
- for Spec Kit: the user should next run `/speckit.implement`

The implementation step is responsible for turning these red tests green. This skill does not implement production code.

### 8. Report gaps
At the end, report:
- which scenarios were covered by new tests (and are currently red)
- which scenarios were partially covered
- which scenarios could not reasonably be automated and why
- any scenarios that were already green (and why)

## Specific guidance for this repository type

### For Aspire AppHost scenarios
When a requirement says resources must be declared in AppHost:
- target `backend/src/GasLens.AppHost/Program.cs`
- write tests that verify expected resources are registered
- prefer integration or host boot tests where feasible
- where direct Aspire dashboard assertions are impractical, use the closest reliable automated assertion available in the repo
- explain any unavoidable approximation

### For "no hardcoded localhost or ports"
Create an architecture-style test that scans non-test source files for:
- `localhost`
- `127.0.0.1`
- explicit port literals in URLs
- obvious hardcoded service endpoints

Exclude:
- test projects
- sample files
- docs
- generated files
- migration snapshots if irrelevant

Fail with a clear message naming offending files.

### For environment/config requirements
Prefer assertions around:
- options binding
- environment variable usage
- absence of direct literal endpoint configuration in production code

## Output expectations

When this skill finishes, it should provide:

1. A short summary of tests created or updated
2. A scenario-to-test mapping
3. Build/test results, with explicit note that the new tests are **red and expected to be red**
4. The recommended next command (`/opsx:apply` or `/speckit.implement`)
5. Any remaining manual verification items

## Editing rules

- Keep changes minimal and local to test projects
- Do not write or modify production code to make tests pass — that is the implementation step's job
- The only exception is trivial, spec-mandated scaffolding strictly needed for tests to compile (e.g. a public marker interface the spec already requires). Prefer to avoid even this; if unavoidable, call it out in the report.
- If a scenario cannot be automated exactly, choose the nearest robust automated check and state the limitation
- Prefer deterministic tests over fragile end-to-end behavior
- Avoid network dependencies on external services

## Test naming guidance

Use names such as:
- `AppHost_should_register_all_required_resources`
- `Production_code_should_not_contain_hardcoded_localhost_urls`
- `Scheduler_should_resolve_broker_address_from_configuration`
- `Api_should_start_with_dependencies_provided_by_apphost`

## Completion checklist

- Active spec source identified (OpenSpec change or Spec Kit feature)
- Relevant scenarios extracted
- Verification map created
- Tests generated in the correct test project(s)
- Build executed
- Tests executed and confirmed red for the right reason
- Setup/compile issues in new tests fixed
- Coverage summary reported
- Handoff to `/opsx:apply` or `/speckit.implement` stated
