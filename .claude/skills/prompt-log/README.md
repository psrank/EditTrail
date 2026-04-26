# prompt-log

Capture an ad-hoc prompt as a **polished, self-contained, documentation-quality
version** — rephrased and improved from what you originally typed — so you can
re-run it later or hand it to a teammate as documentation.

## Why

The prompts you actually type are usually rushed: vague verbs, missing paths,
context buried in the conversation around them. A raw "fix the auth thing
again" prompt re-pasted a month later doesn't reproduce the original outcome,
and it doesn't read as documentation either.

This skill does **not** save what you typed. It saves the polished version
you *wish* you had asked — the one that:

- spells out file paths, ports, formats, and acceptance criteria
- folds in clarifications you made mid-conversation
- reads cleanly to a fresh agent (or a human reader) without surrounding
  context

The result is one artifact that serves both as a re-runnable prompt and as
project documentation.

## Usage

```text
/prompt-log
/prompt-log auth-token-refresh-loop      # provide a slug
/prompt-log add-startup-script
```

Run it after a coding agent has finished an ad-hoc task in the same
conversation. The skill reads the conversation to extract the prompt and
findings, detects the active spec-kit / openspec iteration (or falls back to
the git branch), and writes:

```
prompt-log/
  INDEX.md
  2026-04-25-005-auth-token-refresh-loop.md
```

## Scope

Anything ad-hoc is fair game:

- Bug fixes (the original use case)
- Small tweaks and chores
- One-off scripts you might need to regenerate
- Recurring maintenance prompts ("clean up dead branches", "regenerate fixtures")
- Quick refactors

If the work is large and structured enough to warrant its own iteration,
spec-kit / openspec is the better home. But when in doubt, log it.

## Layout of a log file

1. **Reusable Prompt** — the polished, rephrased version of what the user asked.
   This is what gets re-pasted on recurrence and serves as documentation.
2. **Context** — iteration, branch, commit, date.
3. **Findings** — short bullets on what the agent uncovered.
4. **What Was Done** — short bullets on what changed.
5. **Recurrence Log** — appended each time the prompt is re-run.

## Recurrence handling

If you invoke `/prompt-log` with a slug that already exists, the skill does
**not** create a new file. It appends a dated entry to the existing log's
`Recurrence Log` section and bumps the `recurrences` counter. The original
prompt stays untouched at the top.
