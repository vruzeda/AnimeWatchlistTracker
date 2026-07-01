# CLAUDE.md

@../AGENTS.md

## Isolated Worktrees (mandatory before editing code)

Before making any code changes for a new topic (feature, bug fix, refactor), create a dedicated git worktree with the `EnterWorktree` tool instead of editing files directly in the primary checkout. The user or other agents may be working in the primary checkout at the same time — editing there directly risks clobbering their uncommitted work (e.g. via `git stash`, merges, or overlapping file edits).

- One worktree per topic — do not reuse a worktree across unrelated tasks, and do not share it with concurrent unrelated work.
- Run the full Milestone Checklist (tests, coverage, commits) inside the worktree, on its own branch.
- Skip this for read-only investigation, research, or planning that makes no file edits.
- When the topic is complete and verified, merge the worktree branch back into `main` and use `ExitWorktree` (`remove` once merged cleanly, `keep` if work is left unfinished).

## Milestone Checklist (mandatory after every working change)

After completing each logical unit of work, always run these steps **in order** before moving on:

1. **Run all unit tests** — `./gradlew :module:domain:test :module:remote-data-source-retrofit:test :module:repository:test :module:use-case:test :module:ui:test`
2. **Verify branch coverage** — `./gradlew jacocoTestCoverageVerification` (must pass ≥80%)
3. **Commit** — conventional commit (`feat:`, `fix:`, `refactor:`, `test:`, `chore:`) describing *why*, not *what*

Do not skip or defer any of these steps. Do not batch multiple milestones before committing.
