# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Architecture Reference

See `AGENTS.md` for the comprehensive architecture guide, coding standards, and contribution rules. Follow it strictly.

## Commands

If `JAVA_HOME` is not set and `/usr/libexec/java_home` cannot locate a suitable JDK, use Android Studio's bundled JRE:
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```
If Android Studio is installed elsewhere, locate it with: `find /Applications -name "java" -path "*/jbr/*" -maxdepth 6`

```bash
# Build
./gradlew build
./gradlew assembleRelease

# Run all unit tests
./gradlew test

# Run branch-coverage verification (≥80%) across all jacoco-enabled modules
# To opt a module in, apply the `jacoco` plugin in its build.gradle.kts
./gradlew jacocoTestCoverageVerification

# Run tests for a specific module
./gradlew :module:<name>:test

# Run a single test class
./gradlew :module:<name>:test --tests "com.fully.qualified.ClassName"
```

All test modules use JUnit 5 — `useJUnitPlatform()` is configured in each module's `build.gradle.kts`.

## Module Structure

9 Gradle modules (all under `module/` except `:app`) with strict one-way dependency enforcement:

```
:app → :module:ui → :module:use-case → :module:repository → api(:module:remote-data-source) → :module:domain
                                                           → :module:local-data-source
     → :module:design-system
     → :module:local-data-source-room
     → :module:remote-data-source-retrofit
```

| Module | Type | Contains |
|--------|------|----------|
| `:module:domain` | Pure Kotlin lib | Domain models only |
| `:module:local-data-source` | Pure Kotlin lib | Local data source interfaces (using domain types) |
| `:module:local-data-source-room` | Android lib | Room entities, DAOs, DataStore, migrations |
| `:module:remote-data-source` | Pure Kotlin lib | `AnimeRemoteDataSource` interface |
| `:module:remote-data-source-retrofit` | Pure Kotlin lib | Retrofit impl, DTOs, DTO mappers, interceptors |
| `:module:repository` | Pure Kotlin lib | Repository interfaces + implementations, entity mappers |
| `:module:use-case` | Pure Kotlin lib | All use cases |
| `:module:design-system` | Android lib | Material 3 theme, reusable Compose components |
| `:module:ui` | Android lib | Compose screens, ViewModels, navigation (MVVM) |
| `:app` | Android app | Hilt entry point, DI wiring, WorkManager |

**Critical boundary rules:**
- `:module:domain` has zero dependencies on Android or other modules
- `:module:ui` never imports any `:module:local-data-source*` or `:module:remote-data-source*` module
- `:module:design-system` never imports `:module:domain` or any other module
- Only `:app` sees all modules (cross-module Hilt bindings live here)

## Key Architecture Patterns

**Use cases:** Single `operator fun invoke()`, no use-case-to-use-case calls — compose at ViewModel level.

**ViewModels:** Expose state via `StateFlow` (never `MutableStateFlow` publicly). Depend only on use cases. Observe database reactively via `Flow` — write operations update DB and let Flow deliver the new state (never manually reconstruct state after writes).

**Repositories:** Only layer that coordinates local (Room) and remote (Retrofit) sources. Map all DTOs/entities to domain models via extension functions (`toDomainModel()`, `toEntity()`, `toDto()`).

**Composables:** Stateless — receive state and lambda callbacks. Screens are built exclusively from `:module:design-system` components; no one-off styled components in `:module:ui`.

**Error handling:** Use `Result`-based sealed types (`DataError`) throughout data/domain layers. ViewModels map domain errors to user-facing messages — never Composables.

## Testing Conventions

- Libraries: JUnit 5, MockK, Turbine (Flow/StateFlow), Truth, Kotlin Coroutines Test
- Class naming: `{ClassUnderTest}Test`
- Method naming: descriptive sentence in backticks, e.g., `` `returns empty list when watchlist is empty` ``
- Pattern: Arrange-Act-Assert with blank line separation
- Tests must be fast and deterministic — no network, no real DB (use fake DAOs / in-memory Room for DAO tests)

## Milestone Checklist (mandatory after every working change)

After completing each logical unit of work, always run these steps **in order** before moving on:

1. **Run all unit tests** — `./gradlew :module:domain:test :module:remote-data-source-retrofit:test :module:repository:test :module:use-case:test :module:ui:test`
2. **Verify branch coverage** — `./gradlew jacocoTestCoverageVerification` (must pass ≥80%)
3. **Commit** — conventional commit (`feat:`, `fix:`, `refactor:`, `test:`, `chore:`) describing *why*, not *what*

Do not skip or defer any of these steps. Do not batch multiple milestones before committing.

## Coding Standards (summary)

- No comments except KDoc on public use case classes and `TODO` markers
- Never use `!!` — handle nullability with `?.`, `?:`, `let`
- `suspend` for one-shot operations; `Flow` for data displayed on screen
- `_uiState` / `uiState` convention for private/public state flows

## Technology Stack

- Kotlin 2.3.10 · AGP 9.0.1 · Gradle 9.2.1
- Jetpack Compose BOM 2026.02.01 · Material 3
- Hilt 2.59.2 · Room 2.8.4 · DataStore 1.1.7
- Retrofit 3.0.0 · OkHttp 5.3.2 · Moshi 1.15.2
- Coil 2.7.0 · WorkManager 2.11.1 · Navigation Compose 2.9.7
- External APIs: Jikan v4 (MyAnimeList), chiaki.site (watch order, HTML scraping)
- Min SDK 26 · Target SDK 36
