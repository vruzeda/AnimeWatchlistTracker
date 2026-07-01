# AGENTS.md — Anime Watchlist Tracker

You are an expert Android engineer with deep knowledge of Kotlin, Jetpack Compose, and modern Android architecture. You write production-grade code that is clean, testable, and maintainable. You follow industry standards and never cut corners.

## Architecture

This project follows **Clean Architecture** with strict layer separation enforced at the **Gradle module level**. Each layer is its own independent library module. Dependencies point inward only, and the build system guarantees that layer boundaries cannot be violated.

### Module Types

Prefer **pure Kotlin library modules** (`java-library` + `kotlin` plugins) whenever possible. Only use **Android library modules** (`com.android.library`) when the code has a direct dependency on the Android framework (e.g., Room, Compose, Android Context). This maximizes build speed, testability, and portability.

- **Pure Kotlin module** — No Android dependencies. Tests run on the JVM without Robolectric or instrumentation. Use this by default.
- **Android library module** — Required when the code uses Android APIs, annotation processors that generate Android code (Room, Hilt), or Jetpack Compose.

### Module Overview

All modules except `:app` live under the `module/` root directory.

```
:module:domain                      → Pure Kotlin library (domain models)
:module:local-data-source           → Pure Kotlin library (local data source interfaces)
:module:local-data-source-room      → Android library (Room entities, DAOs, DataStore)
:module:remote-data-source          → Pure Kotlin library (remote data source interfaces)
:module:remote-data-source-retrofit → Pure Kotlin library (Retrofit impl, DTOs, interceptors)
:module:remote-data-source-firebase → Android library (Firebase Firestore impl; prod flavor only)
:module:repository                  → Pure Kotlin library (repository interfaces + implementations)
:module:use-case                    → Pure Kotlin library (use cases)
:module:analytics                   → Pure Kotlin library (AnalyticsTracker interface, NoOpAnalyticsTracker)
:module:analytics-firebase          → Android library (FirebaseAnalyticsTracker; prod flavor only)
:module:notification                → Pure Kotlin library (AnimeUpdateNotifier interface)
:module:notification-android        → Android library (Android notification implementation)
:module:scheduler                   → Pure Kotlin library (AnimeUpdateScheduler interface)
:module:scheduler-work              → Android library (WorkManager scheduler implementation)
:module:design-system               → Android library (design tokens, theme, reusable Compose components)
:module:ui                          → Android library (Compose screens, ViewModels, navigation)
:app                                → Android application (Hilt entry point, wires all modules together)
```

### Layer Rules

**`:module:domain`** — Pure Kotlin Library
- The innermost layer. It has ZERO dependencies on Android, frameworks, or other modules.
- Contains only **domain models**: plain Kotlin data classes and sealed types.
- Models never contain annotations from Room, Retrofit, Moshi, or any framework.
- Package: `com.vuzeda.animewatchlist.tracker.module.domain`
- Dependencies: `kotlinx-coroutines-core` only.

**`:module:local-data-source`** — Pure Kotlin Library
- Contains: local data source interfaces (`AnimeLocalDataSource`, `EpisodeLocalDataSource`, `SeasonLocalDataSource`, `UserPreferencesLocalDataSource`, `WatchedEpisodeLocalDataSource`). These interfaces use domain models directly as their parameter and return types.
- These interfaces form the contract between `:module:repository` (consumer) and `:module:local-data-source-room` (provider).
- Package: `com.vuzeda.animewatchlist.tracker.module.localdatasource`
- Dependencies: `:module:domain`, `kotlinx-coroutines-core`. No Android, no Room.

**`:module:local-data-source-room`** — Android Library
- Contains: Room `@Entity` classes, `@Dao` abstract classes (implementing the `LocalDataSource` interfaces from `:module:local-data-source`), `DataStore`-based `UserPreferencesDataStore`, the Room `Database` class, type converters, migrations, and `RoomTransactionRunner`.
- Each `@Dao` is an abstract class that implements its corresponding `LocalDataSource` interface. Abstract Room-annotated methods operate on `@Entity` types; concrete override methods map between `@Entity` types and domain models via `toDomainModel()`/`toEntity()` extension functions defined alongside the entity classes.
- Package: `com.vuzeda.animewatchlist.tracker.module.localdatasource.room`
- Dependencies: `:module:local-data-source`, `:module:domain`, `:module:repository` (for `TransactionRunner` interface), Room, DataStore, KSP.

**`:module:remote-data-source`** — Pure Kotlin Library
- Contains: `AnimeRemoteDataSource` and `FeedbackRemoteDataSource` interfaces, which define the remote data access contracts.
- Package: `com.vuzeda.animewatchlist.tracker.module.remotedatasource`
- Dependencies: `:module:domain`, `kotlinx-coroutines-core`.

**`:module:remote-data-source-retrofit`** — Pure Kotlin Library
- Contains: `AnimeRemoteDataSourceImpl` (implements `AnimeRemoteDataSource`), Retrofit service interfaces (`JikanApiService`, `ChiakiService`, `ChiakiServiceImpl`), `RateLimitInterceptor`, DTOs, and DTO mappers.
- No Android dependencies. Retrofit and Moshi are pure JVM.
- Package: `com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit`
- Dependencies: `:module:remote-data-source`, `:module:domain`, Retrofit, OkHttp, Moshi, KSP.

**`:module:remote-data-source-firebase`** — Android Library
- Contains: `FirestoreFeedbackRemoteDataSource` (implements `FeedbackRemoteDataSource` using Firebase Firestore and Firebase Installations).
- Used in the `prod` product flavor only — the `mock` flavor provides a stub via `MockFeedbackModule`.
- Package: `com.vuzeda.animewatchlist.tracker.module.remotedatasource.firebase`
- Dependencies: `:module:remote-data-source`, `:module:domain`, Firebase Firestore, Firebase Auth, Firebase Installations, Hilt.

**`:module:repository`** — Pure Kotlin Library
- Contains: repository interfaces (`AnimeRepository`, `FeedbackRepository`, `SeasonRepository`, `UserPreferencesRepository`, `TransactionRunner`) and repository implementations (`AnimeRepositoryImpl`, `FeedbackRepositoryImpl`, `SeasonRepositoryImpl`, `UserPreferencesRepositoryImpl`).
- `AnimeRepositoryImpl` delegates all remote-fetching operations to `AnimeRemoteDataSource` internally; `AnimeRemoteDataSource` is not exposed to consumers.
- Implementations are the only classes that coordinate between local and remote data sources.
- Package: `com.vuzeda.animewatchlist.tracker.module.repository`
- Dependencies: `:module:remote-data-source`, `:module:local-data-source`, `:module:domain`, `:module:notification`, `:module:scheduler`.

**`:module:use-case`** — Pure Kotlin Library
- Contains: all use cases. Each represents a single business operation with a single `operator fun invoke(...)` method.
- Use cases receive repository interfaces via constructor injection. They must not call other use cases — compose at the ViewModel level.
- Package: `com.vuzeda.animewatchlist.tracker.module.usecase`
- Dependencies: `:module:repository`, `:module:domain`.

**`:module:analytics`** — Pure Kotlin Library
- Contains: `AnalyticsTracker` interface, `AnalyticsEvent` sealed type, and `NoOpAnalyticsTracker` (used in the `mock` flavor and as a default safe implementation).
- Consumers (e.g., `:module:ui`) call `AnalyticsTracker.track(event)` without knowing the underlying implementation.
- Package: `com.vuzeda.animewatchlist.tracker.module.analytics`
- Dependencies: none.

**`:module:analytics-firebase`** — Android Library
- Contains: `FirebaseAnalyticsTracker` (implements `AnalyticsTracker` using Firebase Analytics).
- Used in the `prod` product flavor only — the `mock` flavor provides `NoOpAnalyticsTracker` via `MockAnalyticsModule`.
- Package: `com.vuzeda.animewatchlist.tracker.module.analytics.firebase`
- Dependencies: `:module:analytics`, Firebase Analytics BOM.

**`:module:notification`** — Pure Kotlin Library
- Contains: `AnimeUpdateNotifier` interface, which defines the contract for displaying anime update notifications.
- Package: `com.vuzeda.animewatchlist.tracker.module.notification`
- Dependencies: `:module:domain`.

**`:module:notification-android`** — Android Library
- Contains: `NotificationHelper` (implements `AnimeUpdateNotifier` using the Android notification system) and `NotificationLaunchActivity`.
- Package: `com.vuzeda.animewatchlist.tracker.module.notification.android`
- Dependencies: `:module:notification`, `:module:domain`, Hilt, AndroidX Core.

**`:module:scheduler`** — Pure Kotlin Library
- Contains: `AnimeUpdateScheduler` interface, which defines the contract for scheduling periodic and immediate anime update jobs.
- Package: `com.vuzeda.animewatchlist.tracker.module.scheduler`
- Dependencies: none.

**`:module:scheduler-work`** — Android Library
- Contains: `AnimeUpdateWorker`, `AnimeUpdateWorkerScheduler` (implements `AnimeUpdateScheduler`), and `BackfillAiringSeasonWorker`. Uses WorkManager for scheduling.
- Package: `com.vuzeda.animewatchlist.tracker.module.scheduler.work`
- Dependencies: `:module:scheduler`, `:module:notification`, `:module:use-case`, `:module:domain`, WorkManager, Hilt.

**`:module:design-system`** — Android Library
- The app's design system. All visual building blocks live here.
- Contains: Material 3 theme (colors, typography, shapes), design tokens, and all reusable Compose components (buttons, cards, dialogs, input fields, etc.).
- Components are purely presentational — they receive data and callbacks, never access ViewModels or business logic.
- Every component must be previewed with `@Preview` annotations using realistic sample data.
- The `:module:ui` module builds screens exclusively from components defined here. No ad-hoc styling or one-off components in `:module:ui`.
- Package: `com.vuzeda.animewatchlist.tracker.module.designsystem`
- Dependencies: Jetpack Compose BOM, Material 3, Coil (for image components). No dependency on `:module:domain` or any other module.

**`:module:ui`** — Android Library
- Follows the **MVVM** pattern exclusively.
- Contains: screens (Composable functions + ViewModels) and navigation. Screens are assembled from `:module:design-system` components.
- ViewModels expose UI state via `StateFlow` and handle user actions through clearly named methods. Never expose `MutableStateFlow` publicly.
- ViewModels depend on use cases only — never on repositories or data sources directly.
- UI state is modeled as a single sealed interface or data class per screen (e.g., `HomeUiState`). Avoid managing multiple independent state flows in a single ViewModel.
- ViewModels must observe local data reactively via `Flow` rather than performing one-shot fetches. Write operations should update the database and let the `Flow` deliver the new state — never manually reconstruct UI state after a write.
- Composable functions are stateless whenever possible. They receive state and callbacks as parameters.
- Package: `com.vuzeda.animewatchlist.tracker.module.ui`
- Dependencies: `:module:use-case`, `:module:design-system`, `:module:domain`, `:module:analytics`. Never depend on any `:module:local-data-source*` or `:module:remote-data-source*` module.

**`:app`** — Android Application
- The entry point. Contains the `Application` class, `MainActivity`, and all Hilt wiring.
- This is the only module that knows about every other module.
- Contains Hilt `@Module` classes that bind implementations to interfaces: `RepositoryModule`, `LocalDataSourceModule`, `RemoteDataSourceModule`, `PreferencesModule`, `NotificationModule`, `SchedulerModule`, `WorkManagerModule`, and `ClockModule` (provides `kotlin.time.Clock` as an injectable abstraction for testability).
- No business logic, no UI screens, no data access — only DI configuration and app-level setup.
- **Product flavors** (`environment` dimension): `prod` (real `JikanApiService` + `ChiakiServiceImpl` via `ApiServiceModule`, plus Firebase implementations via `analytics-firebase` and `remote-data-source-firebase`) and `mock` (`FakeJikanApiService` + `FakeChiakiService` via `MockApiServiceModule`, plus `MockAnalyticsModule`, `MockFeedbackModule`, `MockMainActivity`, and `ScreenshotSeeder` for screenshot testing).
- Dependencies: all modules.

### Dependency Injection Across Modules

- **Hilt** is the DI framework. The `@HiltAndroidApp` annotation lives in `:app`.
- Cross-module bindings (e.g., binding a repository implementation from `:module:repository` to a repository interface) are defined in `:app`'s DI modules. This keeps modules decoupled.
- Use `@Binds` for interface-to-implementation mappings. Use `@Provides` only when construction logic is needed.
- Use `@Singleton` scope for repositories, database, and network clients. Use `@ViewModelScoped` for ViewModel-specific dependencies.
- Every injectable class uses constructor injection with `@Inject`. Avoid field injection.

### Dependency Graph

```
:module:domain               — no module deps
:module:local-data-source    → :module:domain
:module:remote-data-source   → :module:domain
:module:analytics            — no module deps
:module:notification         → :module:domain
:module:scheduler            — no module deps
:module:repository           → :module:remote-data-source
                             → :module:local-data-source
                             → :module:domain
                             → :module:notification
                             → :module:scheduler
:module:use-case             → :module:repository
                             → :module:domain
:module:analytics-firebase   → :module:analytics
:module:notification-android → :module:notification
                             → :module:domain
:module:remote-data-source-firebase → :module:remote-data-source
                                    → :module:domain
:module:local-data-source-room → :module:local-data-source
                               → :module:domain
                               → :module:repository (TransactionRunner)
:module:remote-data-source-retrofit → :module:remote-data-source
                                    → :module:domain
:module:scheduler-work       → :module:scheduler
                             → :module:notification
                             → :module:use-case
                             → :module:domain
:module:design-system        — no module deps
:module:ui                   → :module:use-case
                             → :module:design-system
                             → :module:domain
                             → :module:analytics
:app → all modules
     (analytics-firebase and remote-data-source-firebase are prodImplementation only)
```

**Critical boundary rules:**
- `:module:domain` has zero dependencies on Android or other modules
- `:module:ui` never imports any `:module:local-data-source*` or `:module:remote-data-source*` module
- `:module:design-system` never imports `:module:domain` or any other module
- Only `:app` sees all modules (cross-module Hilt bindings live here)
- `:module:analytics-firebase` and `:module:remote-data-source-firebase` are `prodImplementation` only in `:app`

### Module Gradle Configuration

- Pure Kotlin modules apply `java-library` and `org.jetbrains.kotlin.jvm` plugins only.
  Current pure Kotlin modules: `:module:domain`, `:module:local-data-source`, `:module:remote-data-source`, `:module:remote-data-source-retrofit`, `:module:repository`, `:module:use-case`, `:module:analytics`, `:module:notification`, `:module:scheduler`.
- Android library modules apply `com.android.library` and related plugins.
  Current Android library modules: `:module:local-data-source-room`, `:module:remote-data-source-firebase`, `:module:analytics-firebase`, `:module:notification-android`, `:module:scheduler-work`, `:module:design-system`, `:module:ui`.
- The `:app` module applies `com.android.application`, `org.jetbrains.kotlin.android`, and `com.google.dagger.hilt.android`.
- Use version catalogs (`libs.versions.toml`) for all dependency versions.

### Mock Product Flavor

The `:app` module defines a `mock` product flavor (alongside `prod`) under the `environment` flavor dimension. The `mock` flavor is a first-class developer tool — it replaces all real API clients with deterministic in-memory fakes, enabling screenshot tests and UI development without network access.

**Rules:**
- Any new feature that introduces a new API service or external data source **must** include a corresponding fake implementation in the `mock` flavor.
- Fake implementations live in `app/src/mock/` and must satisfy the same interface as their real counterparts.
- `MockMainActivity` seeds the fake data store with `ScreenshotSeeder` before launching, ensuring a consistent visual state for screenshot tests.
- Never add real network calls, file I/O, or non-deterministic state to the `mock` source set.

## Project Structure

| Module | Type | Description |
|--------|------|-------------|
| `:module:domain` | Pure Kotlin lib | Domain models only |
| `:module:local-data-source` | Pure Kotlin lib | Local data source interfaces (using domain types) |
| `:module:local-data-source-room` | Android lib | Room entities, DAOs, DataStore, migrations |
| `:module:remote-data-source` | Pure Kotlin lib | Remote data source interfaces |
| `:module:remote-data-source-retrofit` | Pure Kotlin lib | Retrofit impl, DTOs, DTO mappers, interceptors |
| `:module:remote-data-source-firebase` | Android lib | Firebase Firestore impl (prod only) |
| `:module:repository` | Pure Kotlin lib | Repository interfaces + implementations |
| `:module:use-case` | Pure Kotlin lib | All use cases |
| `:module:analytics` | Pure Kotlin lib | AnalyticsTracker interface, NoOpAnalyticsTracker |
| `:module:analytics-firebase` | Android lib | FirebaseAnalyticsTracker (prod only) |
| `:module:notification` | Pure Kotlin lib | AnimeUpdateNotifier interface |
| `:module:notification-android` | Android lib | Android notification implementation |
| `:module:scheduler` | Pure Kotlin lib | AnimeUpdateScheduler interface |
| `:module:scheduler-work` | Android lib | WorkManager scheduler implementation |
| `:module:design-system` | Android lib | Material 3 theme, reusable Compose components |
| `:module:ui` | Android lib | Compose screens, ViewModels, navigation (MVVM) |
| `:app` | Android app | Hilt entry point, DI wiring |

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

## Coding Standards

### Naming Conventions

Naming is the primary documentation in this project. Every name must be precise and self-explanatory.

- **Classes**: `PascalCase`. Use suffixes that communicate purpose: `AnimeRepository`, `GetWatchlistUseCase`, `HomeViewModel`, `AnimeEntity`, `AnimeDto`, `AnimeDetailScreen`.
- **Functions**: `camelCase`. Use verb-first names that describe the action: `fetchAnimeById`, `updateEpisodeProgress`, `deleteFromWatchlist`. Composable functions use `PascalCase` per Compose convention.
- **Variables and properties**: `camelCase`. Name them for what they represent, not their type: `watchingAnimeList` not `list`, `selectedStatus` not `status`.
- **Constants**: `SCREAMING_SNAKE_CASE` inside companion objects, `PascalCase` for top-level Compose constants.
- **State flows**: Name the private mutable version with an underscore prefix: `_uiState` / `uiState`.
- **Mapper functions**: `toDomainModel()`, `toEntity()`, `toDto()`. Always use extension functions on the source type. Use `toEntity()` when mapping domain models to Room `@Entity` types within `:module:local-data-source-room`; use `toDomainModel()` for the reverse.
- **Boolean variables**: Prefix with `is`, `has`, `should`, or `can`: `isLoading`, `hasError`, `shouldRetry`.

### Comments Policy

**Do not write comments.** The code must be readable on its own through:
- Precise naming of classes, functions, variables, and parameters
- Small, single-responsibility functions
- Clear type signatures and return types
- Well-structured packages that communicate intent

The only acceptable exceptions are:
- KDoc on public use case classes (one sentence describing the business operation)
- `TODO` markers for known incomplete work
- Suppression annotations that need justification

If you feel the need to write a comment, refactor the code until the comment is unnecessary.

### Kotlin Style

- Use `data class` for models, entities, DTOs, and UI state.
- Use `sealed interface` for state hierarchies and navigation routes.
- Prefer `val` over `var`. Prefer immutable collections.
- Use `Result` or a custom sealed type for operations that can fail. Never throw exceptions for expected failure cases.
- Use Kotlin Coroutines and `Flow` for all async operations. Never use callbacks.
- Use `suspend` functions in repositories and use cases for one-shot operations (writes, network requests). Use `Flow` for any data that is displayed on screen, so the UI reacts automatically to database changes.
- Avoid `lateinit` — prefer constructor injection or `lazy`.
- Use named arguments when calling functions with more than two parameters.
- Prefer expression bodies for single-expression functions.
- Never use `!!`. Handle nullability explicitly with `?.`, `?:`, or `let`.

### Jetpack Compose

- Every screen has its own package containing: the screen Composable, its ViewModel, and its UI state class.
- Screen Composables receive state and lambda callbacks. They do not access ViewModels directly — use a wrapper Composable that connects the ViewModel to the stateless screen.
- Screens are built exclusively from components in `:module:design-system`. Do not create one-off styled components in `:module:ui` — if a new component is needed, add it to `:module:design-system` first.
- Use `remember` and `derivedStateOf` appropriately. Never perform heavy computation in composition.
- Preview every significant Composable with `@Preview` annotations using realistic sample data.
- Use `Modifier` as the first optional parameter of every Composable. Always pass modifiers down.

### Error Handling

- Use a `Result`-based pattern throughout the data and domain layers.
- Map all exceptions to meaningful domain error types (e.g., `sealed interface DataError`).
- The UI layer translates domain errors into user-facing messages. Error mapping lives in the ViewModel, not in Composables.
- Network errors, database errors, and validation errors must all be handled gracefully — the app should never crash from expected error scenarios.

## Localization

The app supports English (default), Brazilian Portuguese (`values-pt`), Latin American Spanish (`values-es`), and French (`values-fr`).

**Rules:**
- All user-visible strings must live in `strings.xml` — never hardcode text in Kotlin or XML layout files.
- Every new string added to `values/strings.xml` must be translated in all four language files.
- ViewModels must not build display strings by concatenating literals — return structured data to the Composable and format with `stringResource` there.
- String modules: `:module:ui`, `:module:design-system`, `:module:notification-android` each have their own `res/values*/strings.xml` files — keep strings in the module that owns the UI.

## Dependencies

Use only well-established, Google-recommended, or widely-adopted industry-standard libraries:

- **Jetpack Compose BOM** — UI toolkit
- **Material 3** — Design system
- **Room** — Local database
- **Hilt** — Dependency injection
- **Retrofit + OkHttp** — Networking
- **Moshi** — JSON parsing (API DTOs)
- **Kotlinx Serialization** — Type-safe navigation route parameters
- **Coil** — Image loading (Compose-native)
- **Jetpack Navigation Compose** — Navigation
- **Kotlin Coroutines + Flow** — Async and reactive programming
- **Firebase** (Analytics, Firestore, Auth, Crashlytics) — Prod-flavor analytics, feedback, and crash reporting
- **WorkManager** — Background task scheduling
- **Timber** — Logging (debug builds only)
- **error_prone_annotations** — `compileOnly` in `:app`; required at compile time because Hilt/Dagger 2.60's generated `Dagger*Components` reference `@CanIgnoreReturnValue`

Do NOT introduce any dependency that is not listed above without explicit approval. Do not use experimental or alpha-stage libraries in production code.

## Technology Stack

- Kotlin 2.3.21 · AGP 9.2.0 · Gradle 9.5.0
- Jetpack Compose BOM 2026.04.01 · Material 3
- Hilt 2.59.2 · Room 2.8.4 · DataStore 1.2.1
- Retrofit 3.0.0 · OkHttp 5.3.2 · Moshi 1.15.2
- Coil 2.7.0 · WorkManager 2.11.2 · Navigation Compose 2.9.8
- External APIs: Jikan v4 (MyAnimeList), chiaki.site (watch order, HTML scraping)
- Min SDK 26 · Target SDK 37

## Testing

Every component must have unit tests. This is non-negotiable.

### Testing Strategy

**Use Cases**
- Test each use case in isolation with fake/mock repositories.
- Verify correct delegation to the repository.
- Verify error handling and edge cases.

**Repositories**
- Test with fake DAOs and fake API services.
- Verify correct mapping between entities/DTOs and domain models.
- Verify that local and remote data sources are coordinated correctly.

**ViewModels**
- Test with fake use cases.
- Verify that UI state transitions are correct for each user action.
- Verify error states and loading states.
- Use `Turbine` for testing `StateFlow` emissions.

**Mappers**
- Test every mapper function with representative data.
- Verify that all fields are mapped correctly, including edge cases like null or empty values.

**DAOs**
- Test with an in-memory Room database.
- Verify CRUD operations and query correctness.

### Testing Libraries

- **JUnit 5** — Test framework
- **MockK** — Mocking (preferred over Mockito for Kotlin)
- **Turbine** — StateFlow/Flow testing
- **Truth** or **Kotest Assertions** — Assertion library
- **Kotlin Coroutines Test** — `runTest`, `TestDispatcher`
- **Robolectric** — Android framework tests without a device (only if necessary)

### Testing Conventions

- Test class naming: `{ClassUnderTest}Test` (e.g., `GetWatchlistUseCaseTest`).
- Test function naming: `descriptive sentence with backticks` (e.g., `` `returns watchlist sorted by title when sort order is alphabetical` ``).
- Follow the Arrange-Act-Assert pattern. Keep each section visually separated with a blank line.
- One assertion per test when possible. Multiple assertions are acceptable only when verifying a single logical outcome.
- Tests must not depend on execution order or shared mutable state.
- Aim for fast, deterministic tests. No network calls, no real database access, no sleeps or delays.

## Git Conventions

- **Commit messages**: Use conventional commits — `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`.
- **Branch naming**: `feature/short-description`, `fix/short-description`, `refactor/short-description`.
- Keep commits small, focused, and atomic. One logical change per commit.

### Milestone-Based Commits

You MUST commit at each meaningful milestone during development — do NOT wait until the end or wait to be asked. At each meaningful step, the project must be built and all unit tests must pass before committing. Follow this workflow:

1. Complete a logical unit of work (e.g., a new module, a feature, a layer of the architecture).
2. Build the project and run all unit tests:
   ```bash
   ./gradlew :module:domain:test :module:remote-data-source-retrofit:test :module:repository:test :module:use-case:test :module:ui:test
   ```
3. Run coverage verification:
   ```bash
   ./gradlew jacocoTestCoverageVerification
   ```
4. If anything fails, fix it before proceeding.
5. Once the build and tests succeed, commit with a clear, descriptive message.
6. Continue to the next unit of work and repeat.

This ensures that every commit in the history represents a working, verified state of the project. Never commit code that does not compile or has failing tests.

### Changelog Maintenance

Every `feat:` or `fix:` commit must also update the Play Store changelogs. Include the changelog changes in the **same commit** as the code — do not create a separate changelog commit.

**Files:** `fastlane/metadata/android/{locale}/changelogs/{nextVersionCode}.txt`
Locales: `en-US`, `pt-BR`, `es-419`, `fr-FR`

**Steps:**
1. Read `versionCode` from `app/build.gradle.kts`. The target filename is `{versionCode + 1}.txt`.
2. If `{versionCode + 1}.txt` already exists in a locale directory, append a new bullet (`• ...`) for this change.
3. If it does not exist yet, create it with a single bullet point.
4. Write a concise, user-facing description — not the raw commit message. Translate it accurately into all four languages.
5. Keep each file under 500 characters total (Google Play hard limit).
6. Stage all four changelog files alongside the code changes in the same commit.

Only include changes that affect what a user experiences in the app. Skip changelog updates for changes to documentation (`AGENTS.md`, `README.md`), CI/CD workflows (`.github/`), Fastlane configuration, build scripts, or test code — regardless of the commit type prefix.

## Room Database Migrations

When introducing a new database version:

1. **Write the migration** in `Migrations.kt` (e.g., `MIGRATION_18_19`). SQLite on min SDK 26 does not support `ALTER TABLE … RENAME COLUMN` (requires SQLite 3.25 / API 29+), so column renames require full table recreation: create the new table, copy all rows, drop the old table, rename the new one. Recreate foreign-key targets before the tables that reference them.
2. **Register the migration** in `AnimeDatabase.kt` — bump `version` and add the new migration to `.addMigrations(...)` in the Hilt module.
3. **Generate the schema export** by running the tests (KSP runs during compilation and writes the JSON automatically):
   ```bash
   ./gradlew :module:local-data-source-room:test
   ```
   Room writes the export to `module/local-data-source-room/schemas/com.vuzeda.animewatchlist.tracker.module.localdatasource.room.AnimeDatabase/{version}.json`.
4. **Commit the schema JSON alongside the migration code** in the same commit — the JSON file is the authoritative record of the schema at that version and must not be left uncommitted.

## Before Submitting Code

1. All existing tests pass.
2. New code has corresponding unit tests.
3. **Branch coverage ≥ 80%** in every tested module — run the aggregate Jacoco verification task and fix any violations before committing:
   ```bash
   ./gradlew jacocoTestCoverageVerification
   ```
   Coverage enforcement is opt-in per module: any module that applies the `jacoco` Gradle plugin is automatically discovered by the root, which injects the 80% violation rule into its `jacocoTestCoverageVerification` task. Gradle's task-name resolution runs the task across all subprojects that have it — no custom aggregate task is needed. To add a new JVM module to coverage enforcement, apply the `jacoco` plugin in that module's `build.gradle.kts`; no other changes are needed. Modules that need to exclude generated or untestable classes (e.g., Moshi adapters, Room DAOs, coroutine state-machine code) configure only `classDirectories` in their `jacocoTestCoverageVerification` task — they do not set the ratio. Android library modules additionally register the `jacocoTestCoverageVerification` task manually because AGP does not create it automatically. Modules with interface-only code (`:module:local-data-source`, `:module:remote-data-source`) have no testable implementation and intentionally do not apply the `jacoco` plugin.
4. No compiler warnings.
5. Code follows the naming and style conventions above.
6. No unnecessary comments in the code.
7. Architecture layer boundaries are respected — no cross-layer imports.
8. `README.md` is updated to reflect any user-facing changes, new features, new dependencies, or architectural additions.
