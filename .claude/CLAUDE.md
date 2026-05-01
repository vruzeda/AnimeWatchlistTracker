# CLAUDE.md

@../AGENTS.md

## Milestone Checklist (mandatory after every working change)

After completing each logical unit of work, always run these steps **in order** before moving on:

1. **Run all unit tests** — `./gradlew :module:domain:test :module:remote-data-source-retrofit:test :module:repository:test :module:use-case:test :module:ui:test`
2. **Verify branch coverage** — `./gradlew jacocoTestCoverageVerification` (must pass ≥80%)
3. **Commit** — conventional commit (`feat:`, `fix:`, `refactor:`, `test:`, `chore:`) describing *why*, not *what*

Do not skip or defer any of these steps. Do not batch multiple milestones before committing.
