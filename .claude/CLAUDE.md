# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Architecture Reference

See `AGENTS.md` for the comprehensive architecture guide, coding standards, and contribution rules. Follow it strictly.

## Commands

```bash
# Build
./gradlew build
./gradlew assembleRelease

# Run all unit tests
./gradlew :domain:test :data:api:test :data:repository:test :ui:test

# Run tests for a specific module
./gradlew :[module]:test

# Run a single test class
./gradlew :[module]:test --tests "com.fully.qualified.ClassName"
```

All test modules use JUnit 5 — `useJUnitPlatform()` is configured in each module's `build.gradle.kts`.

## Module Structure

7 Gradle modules with strict one-way dependency enforcement:

```
:app → :ui → :domain
           → :designsystem
:app → :data:repository → :data:api   → :domain
                        → :data:local  → :domain
```

| Module | Type | Contains |
|--------|------|----------|
| `:domain` | Pure Kotlin lib | Models, repository interfaces, use cases |
| `:data:api` | Pure Kotlin lib | Retrofit service interfaces, DTOs, Moshi |
| `:data:local` | Android lib | Room database, DAOs, entities |
| `:data:repository` | Pure Kotlin lib | Repository implementations, mappers |
| `:designsystem` | Android lib | Material 3 theme, reusable Compose components |
| `:ui` | Android lib | Compose screens, ViewModels, navigation (MVVM) |
| `:app` | Android app | Hilt entry point, DI wiring, WorkManager |

**Critical boundary rules:**
- `:domain` has zero dependencies on Android or other modules
- `:ui` never imports any `:data` module
- `:designsystem` never imports `:domain` or any `:data` module
- Only `:app` sees all modules (cross-module Hilt bindings live here)

## Key Architecture Patterns

**Use cases:** Single `operator fun invoke()`, no use-case-to-use-case calls — compose at ViewModel level.

**ViewModels:** Expose state via `StateFlow` (never `MutableStateFlow` publicly). Depend only on use cases. Observe database reactively via `Flow` — write operations update DB and let Flow deliver the new state (never manually reconstruct state after writes).

**Repositories:** Only layer that coordinates local (Room) and remote (Retrofit) sources. Map all DTOs/entities to domain models via extension functions (`toDomainModel()`, `toEntity()`, `toDto()`).

**Composables:** Stateless — receive state and lambda callbacks. Screens are built exclusively from `:designsystem` components; no one-off styled components in `:ui`.

**Error handling:** Use `Result`-based sealed types (`DataError`) throughout data/domain layers. ViewModels map domain errors to user-facing messages — never Composables.

## Testing Conventions

- Libraries: JUnit 5, MockK, Turbine (Flow/StateFlow), Truth, Kotlin Coroutines Test
- Class naming: `{ClassUnderTest}Test`
- Method naming: descriptive sentence in backticks, e.g., `` `returns empty list when watchlist is empty` ``
- Pattern: Arrange-Act-Assert with blank line separation
- Tests must be fast and deterministic — no network, no real DB (use fake DAOs / in-memory Room for DAO tests)

## Coding Standards (summary)

- No comments except KDoc on public use case classes and `TODO` markers
- Never use `!!` — handle nullability with `?.`, `?:`, `let`
- `suspend` for one-shot operations; `Flow` for data displayed on screen
- `_uiState` / `uiState` convention for private/public state flows
- Commit with conventional commits (`feat:`, `fix:`, `refactor:`, `test:`, `chore:`) at each working milestone — build and tests must pass before each commit

## Technology Stack

- Kotlin 2.3.10 · AGP 9.0.1 · Gradle 9.2.1
- Jetpack Compose BOM 2026.02.01 · Material 3
- Hilt 2.59.2 · Room 2.8.4 · DataStore 1.1.7
- Retrofit 3.0.0 · OkHttp 5.3.2 · Moshi 1.15.2
- Coil 2.7.0 · WorkManager 2.11.1 · Navigation Compose 2.9.7
- External APIs: Jikan v4 (MyAnimeList), chiaki.site (watch order, HTML scraping)
- Min SDK 26 · Target SDK 36
