# AGENTS.md — Anime Watchlist Tracker

You are an expert Android engineer with deep knowledge of Kotlin, Jetpack Compose, and modern Android architecture. You write production-grade code that is clean, testable, and maintainable. You follow industry standards and never cut corners.

## Architecture

This project follows **Clean Architecture** with strict layer separation enforced at the **Gradle module level**. Each layer is its own independent library module, dependencies point inward only, and the build system guarantees layer boundaries cannot be violated.

Prefer **pure Kotlin library modules** (`java-library` + `kotlin` plugins) by default — tests run on the JVM without Robolectric or instrumentation. Only use an **Android library module** (`com.android.library`) when the code has a direct dependency on the Android framework or an annotation processor that generates Android code (Room, Hilt, Compose).

### Modules

All modules except `:app` live under `module/`. Package root: `com.vuzeda.animewatchlist.tracker.module.*`.

| Module | Type | Contains |
|--------|------|----------|
| `domain` | Pure Kotlin | Domain models only (plain data classes/sealed types). Zero framework annotations. The innermost layer — no dependencies on any other module. |
| `local-data-source` | Pure Kotlin | Local data source interfaces (`AnimeLocalDataSource`, `EpisodeLocalDataSource`, `SeasonLocalDataSource`, `UserPreferencesLocalDataSource`, `WatchedEpisodeLocalDataSource`), using domain models as their contract. |
| `local-data-source-room` | Android | Room `@Entity`/`@Dao` implementations of the `local-data-source` interfaces, `DataStore`-based `UserPreferencesDataStore`, the `Database` class, migrations, `RoomTransactionRunner`. Each `@Dao` maps `@Entity` ↔ domain model via `toDomainModel()`/`toEntity()` extensions defined alongside the entity. |
| `remote-data-source` | Pure Kotlin | `AnimeRemoteDataSource` and `FeedbackRemoteDataSource` interfaces. |
| `remote-data-source-retrofit` | Pure Kotlin | `AnimeRemoteDataSourceImpl`, Retrofit services (`JikanApiService`, `ChiakiService`/`ChiakiServiceImpl`), `RateLimitInterceptor`, DTOs, DTO mappers. Retrofit/Moshi are pure JVM, so no Android dependency needed. |
| `remote-data-source-firebase` | Android | `FirestoreFeedbackRemoteDataSource`. **`prod` flavor only** — `mock` flavor uses `MockFeedbackModule` instead. |
| `repository` | Pure Kotlin | Repository interfaces + implementations (`AnimeRepository`, `FeedbackRepository`, `SeasonRepository`, `UserPreferencesRepository`, `TransactionRunner`). The only layer that coordinates local + remote data sources. `AnimeRemoteDataSource` is never exposed past `AnimeRepositoryImpl`. |
| `use-case` | Pure Kotlin | All use cases — one business operation each, exposed via a single `operator fun invoke(...)`. Use cases must not call other use cases; compose them at the ViewModel level. |
| `analytics` | Pure Kotlin | `AnalyticsTracker` interface, `AnalyticsEvent` sealed type, `NoOpAnalyticsTracker`. |
| `analytics-firebase` | Android | `FirebaseAnalyticsTracker`. **`prod` flavor only** — `mock` flavor uses `NoOpAnalyticsTracker` via `MockAnalyticsModule`. |
| `notification` | Pure Kotlin | `AnimeUpdateNotifier` interface. |
| `notification-android` | Android | `NotificationHelper` (implements `AnimeUpdateNotifier`), `NotificationLaunchActivity`. |
| `scheduler` | Pure Kotlin | `AnimeUpdateScheduler` interface. |
| `scheduler-work` | Android | `AnimeUpdateWorker`, `AnimeUpdateWorkerScheduler`, `BackfillAiringSeasonWorker` — WorkManager-backed. |
| `design-system` | Android | Material 3 theme, design tokens, all reusable Compose components. Purely presentational — never touches ViewModels or business logic, never depends on `domain` or any other module. Every component gets a `@Preview` with realistic sample data. |
| `ui` | Android | Compose screens + ViewModels + navigation (MVVM). Screens are built exclusively from `design-system` components — never one-off styled components. Never depends on any `local-data-source*` or `remote-data-source*` module. |
| `:app` | Android app | Hilt entry point (`Application`, `MainActivity`), all cross-module DI wiring. The only module that sees every other module. No business logic, no UI, no data access. |

**Critical boundary rules:**
- `domain` has zero dependencies on Android or any other module.
- `ui` never imports `local-data-source*` or `remote-data-source*` modules.
- `design-system` never imports `domain` or any other module.
- Only `:app` wires cross-module Hilt bindings.
- `analytics-firebase` and `remote-data-source-firebase` are `prodImplementation` only.

### Product Flavors

`:app` defines an `environment` flavor dimension: `prod` (real `JikanApiService`, `ChiakiServiceImpl`, Firebase implementations) and `mock` (`FakeJikanApiService`, `FakeChiakiService`, `MockAnalyticsModule`, `MockFeedbackModule`, `MockMainActivity`, `ScreenshotSeeder`). The `mock` flavor is a first-class dev tool for screenshot testing and UI development without network access.

- Any new feature introducing an API service or external data source **must** get a fake in `app/src/mock/` satisfying the same interface.
- `MockMainActivity` seeds fakes via `ScreenshotSeeder` before launch, for consistent screenshot state.
- Never add real network calls, file I/O, or non-deterministic state to the `mock` source set.

### Dependency Injection

- Hilt is the DI framework; `@HiltAndroidApp` lives in `:app`.
- Cross-module bindings (repository impl → interface, etc.) are defined in `:app`'s DI modules only — this keeps the library modules decoupled from each other.
- `@Binds` for interface-to-impl mappings; `@Provides` only when construction logic is needed.
- `@Singleton` for repositories, database, network clients; `@ViewModelScoped` for ViewModel-specific deps.
- Constructor injection with `@Inject` everywhere — avoid field injection and `lateinit`.

## Commands

If `JAVA_HOME` is not set and `/usr/libexec/java_home` can't locate a JDK, use Android Studio's bundled JRE (`Contents/Contents/Home` under the app bundle, or locate it via `find /Applications -name "java" -path "*/jbr/*" -maxdepth 6`).

```bash
./gradlew build
./gradlew test                                # all unit tests
./gradlew jacocoTestCoverageVerification       # branch coverage ≥80%, all jacoco-enabled modules
./gradlew :module:<name>:test                  # single module
./gradlew :module:<name>:test --tests "com.fully.qualified.ClassName"
```

All test modules use JUnit 5 (`useJUnitPlatform()`).

## Coding Standards

### Naming

Naming is the primary documentation in this project — every name must be precise and self-explanatory.

- **Classes**: `PascalCase` with purpose-communicating suffixes (`AnimeRepository`, `GetWatchlistUseCase`, `HomeViewModel`, `AnimeEntity`, `AnimeDto`).
- **Functions**: `camelCase`, verb-first (`fetchAnimeById`, `updateEpisodeProgress`). Composables use `PascalCase`.
- **Variables**: `camelCase`, named for what they represent, not their type (`watchingAnimeList`, not `list`).
- **Constants**: `SCREAMING_SNAKE_CASE` in companion objects; `PascalCase` for top-level Compose constants.
- **State flows**: private mutable version gets an underscore prefix (`_uiState` / `uiState`). Never expose `MutableStateFlow` publicly.
- **Mappers**: `toDomainModel()`, `toEntity()`, `toDto()` — extension functions on the source type.
- **Booleans**: prefix with `is`, `has`, `should`, or `can`.

### Comments

Do not write comments. Achieve readability through precise naming, small single-responsibility functions, clear types, and well-structured packages. Acceptable exceptions: one-sentence KDoc on public use case classes, `TODO` markers, and justified suppression annotations. If you feel the need for a comment, refactor until it's unnecessary.

### Kotlin Style

- `data class` for models/entities/DTOs/UI state; `sealed interface` for state hierarchies and nav routes.
- Prefer `val` and immutable collections.
- `Result` or a custom sealed type for failable operations — never throw for expected failures.
- Coroutines + `Flow` for all async work, never callbacks. `suspend` for one-shot operations (writes, network); `Flow` for anything displayed on screen so the UI reacts to database changes automatically.
- Never use `!!` — handle nullability explicitly.
- Named arguments for calls with 3+ parameters. Prefer expression bodies for single-expression functions.

### Jetpack Compose

- Every screen has its own package: the screen Composable, its ViewModel, its UI state class.
- Screen Composables take state + lambda callbacks and don't touch the ViewModel directly — use a wrapper Composable to connect them.
- Build exclusively from `design-system` components; add new components there first if one is missing.
- `remember`/`derivedStateOf` where appropriate; never heavy computation in composition.
- `@Preview` every significant Composable with realistic sample data.
- `Modifier` as the first optional parameter, always passed down.

### Error Handling

- `Result`-based pattern through the data and domain layers; exceptions map to domain error types (e.g. `sealed interface DataError`).
- Error-to-message translation happens in the ViewModel, not in Composables.
- The app must never crash from expected error scenarios (network, database, validation).

## Localization

Supported locales: English (default), Brazilian Portuguese (`values-pt`), Latin American Spanish (`values-es`), French (`values-fr`).

- All user-visible strings live in `strings.xml` — never hardcode text.
- Every new string in `values/strings.xml` must be translated into all locale variants.
- ViewModels return structured data, not concatenated display strings — format with `stringResource` in the Composable.
- Each UI-owning module (`ui`, `design-system`, `notification-android`) keeps its own `res/values*/strings.xml`.

## Testing

Every component must have unit tests — non-negotiable.

- **Use cases**: fake/mock repositories; verify delegation, error handling, edge cases.
- **Repositories**: fake DAOs/API services; verify entity/DTO ↔ domain mapping and local/remote coordination.
- **ViewModels**: fake use cases; verify UI state transitions per action, loading/error states, via Turbine on the `StateFlow`.
- **Mappers**: representative data per mapper, including null/empty edge cases.
- **DAOs**: in-memory Room database; verify CRUD and query correctness.

Preferred libraries: JUnit 5, MockK, Turbine, Truth/Kotest Assertions, Kotlin Coroutines Test, Robolectric (only if unavoidable).

Conventions:
- Test class: `{ClassUnderTest}Test`. Test function: `` `descriptive sentence in backticks` ``.
- Arrange-Act-Assert with a blank line between sections.
- One assertion per test unless verifying a single logical outcome.
- No dependency on execution order or shared mutable state. No network calls, real DB access, sleeps, or delays.

## Git Conventions

- Conventional commits: `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`.
- Branch naming: `feature/short-description`, `fix/short-description`, `refactor/short-description`.
- Small, focused, atomic commits — one logical change each.

### Isolated Worktrees (mandatory before editing code)

Before making any code changes for a new topic, create a dedicated git worktree with `EnterWorktree` instead of editing the primary checkout directly — the user or other agents may be working there concurrently, and direct edits risk clobbering uncommitted work.

- One worktree per topic; don't reuse across unrelated tasks or share with concurrent work.
- Run the full Milestone Checklist inside the worktree, on its own branch.
- Skip for read-only investigation/research/planning that makes no file edits.
- When the topic is verified complete, merge the branch back into `main` and `ExitWorktree` (`remove` once merged cleanly, `keep` if work is left unfinished).

### Milestone Checklist (mandatory after every working change)

In order, every time:
1. `./gradlew test`
2. `./gradlew jacocoTestCoverageVerification` (≥80% branch coverage)
3. Commit — conventional commit describing *why*, not *what*.

Don't skip, defer, or batch multiple milestones before committing. Every commit must represent a working, verified state — never commit code that fails to compile or has failing tests.

### Changelog Maintenance

Every `feat:`/`fix:` commit that changes what a user experiences must also update the Play Store changelogs, in the same commit as the code. Skip this for docs, CI/CD, Fastlane config, build scripts, or test-only changes.

**Files:** `fastlane/metadata/android/{locale}/changelogs/{nextVersionCode}.txt` for `en-US`, `pt-BR`, `es-419`, `fr-FR`.

1. Read `versionCode` from `app/build.gradle.kts`; target filename is `{versionCode + 1}.txt`.
2. Append a new bullet (`• ...`) if the file already exists for this version, otherwise create it with one bullet.
3. Write a concise, user-facing description (not the raw commit message), translated accurately into all four languages.
4. Keep each file under 500 characters total (Google Play hard limit).
5. Stage all four files alongside the code changes in the same commit.

## Room Database Migrations

1. Write the migration in `Migrations.kt` (e.g. `MIGRATION_18_19`). Min SDK 26 lacks `ALTER TABLE … RENAME COLUMN` support (needs SQLite 3.25 / API 29+) — renames require full table recreation: create the new table, copy rows, drop the old table, rename the new one. Recreate foreign-key targets before the tables referencing them.
2. Register it in `AnimeDatabase.kt` — bump `version`, add to `.addMigrations(...)` in the Hilt module.
3. Generate the schema export by running `./gradlew :module:local-data-source-room:test` (KSP writes it during compilation) to `module/local-data-source-room/schemas/com.vuzeda.animewatchlist.tracker.module.localdatasource.room.AnimeDatabase/{version}.json`.
4. Commit the schema JSON alongside the migration code in the same commit — it's the authoritative record of that schema version.

## Before Submitting Code

1. All existing tests pass; new code has corresponding unit tests.
2. Branch coverage ≥80% via `./gradlew jacocoTestCoverageVerification`. Coverage enforcement is opt-in per module by applying the `jacoco` plugin — the root build injects the 80% rule automatically. Modules with generated/untestable code (Moshi adapters, Room DAOs, coroutine state machines) configure `classDirectories` exclusions rather than lowering the ratio. Android library modules must register `jacocoTestCoverageVerification` manually (AGP doesn't create it). Interface-only modules (`local-data-source`, `remote-data-source`) intentionally skip the plugin.
3. No compiler warnings.
4. Naming/style conventions followed; no unnecessary comments.
5. Architecture layer boundaries respected — no cross-layer imports.
6. `README.md` updated for any user-facing change, new feature, or architectural addition.

## Dependencies

Use only well-established, Google-recommended, or widely-adopted industry-standard libraries (Compose, Material 3, Room, Hilt, Retrofit/OkHttp, Moshi, Kotlinx Serialization, Coil, Navigation Compose, Coroutines/Flow, Firebase, WorkManager, Timber). Versions are pinned in `gradle/libs.versions.toml` — check there for current versions rather than assuming. Do not introduce a new dependency, or an experimental/alpha-stage library, without explicit approval.
