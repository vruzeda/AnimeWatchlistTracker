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
:module:domain                → Pure Kotlin library (models only)
:module:local-data-source     → Pure Kotlin library (local data source interfaces, plain record classes)
:module:local-data-source-room → Android library (Room entities, DAOs, DataStore)
:module:remote-data-source    → Pure Kotlin library (AnimeRemoteDataSource interface)
:module:remote-data-source-retrofit → Pure Kotlin library (Retrofit impl, DTOs, interceptors)
:module:repository            → Pure Kotlin library (repository interfaces + implementations, mappers)
:module:use-case              → Pure Kotlin library (use cases)
:module:design-system         → Android library (design tokens, theme, reusable Compose components)
:module:ui                    → Android library (Compose screens, ViewModels, navigation)
:app                          → Android application (Hilt entry point, wires all modules together)
```

### Layer Rules

**`:module:domain`** — Pure Kotlin Library
- The innermost layer. It has ZERO dependencies on Android, frameworks, or other modules.
- Contains only **domain models**: plain Kotlin data classes and sealed types.
- Models never contain annotations from Room, Retrofit, Moshi, or any framework.
- Package: `com.vuzeda.animewatchlist.tracker.module.domain`
- Dependencies: `kotlinx-coroutines-core` only.

**`:module:local-data-source`** — Pure Kotlin Library
- Contains: local data source interfaces (`AnimeLocalDataSource`, `SeasonLocalDataSource`, `UserPreferencesLocalDataSource`) and plain Kotlin record classes (`Anime`, `Season`) that mirror the stored shape of each entity without any Room or Android annotations.
- These types form the contract between `:module:repository` (consumer) and `:module:local-data-source-room` (provider).
- Package: `com.vuzeda.animewatchlist.tracker.module.localdatasource`
- Dependencies: `kotlinx-coroutines-core` only. No Android, no Room, no `:module:domain`.

**`:module:local-data-source-room`** — Android Library
- Contains: Room `@Entity` classes, `@Dao` abstract classes (implementing the `LocalDataSource` interfaces from `:module:local-data-source`), `DataStore`-based `UserPreferencesDataStore`, the Room `Database` class, type converters, migrations, and `RoomTransactionRunner`.
- Each `@Dao` is an abstract class that implements its corresponding `LocalDataSource` interface. Abstract Room-annotated methods operate on `@Entity` types; concrete override methods map between `@Entity` types and the plain record classes from `:module:local-data-source`.
- Package: `com.vuzeda.animewatchlist.tracker.module.localdatasource.room`
- Dependencies: `:module:local-data-source`, `:module:domain`, `:module:repository` (for `TransactionRunner` interface), Room, DataStore, KSP.

**`:module:remote-data-source`** — Pure Kotlin Library
- Contains: the single `AnimeRemoteDataSource` interface, which defines the remote data access contract.
- Package: `com.vuzeda.animewatchlist.tracker.module.remotedatasource`
- Dependencies: `:module:domain`, `kotlinx-coroutines-core`.

**`:module:remote-data-source-retrofit`** — Pure Kotlin Library
- Contains: `AnimeRemoteDataSourceImpl` (implements `AnimeRemoteDataSource`), Retrofit service interfaces (`JikanApiService`, `ChiakiService`, `ChiakiServiceImpl`), `RateLimitInterceptor`, DTOs, and DTO mappers.
- No Android dependencies. Retrofit and Moshi are pure JVM.
- Package: `com.vuzeda.animewatchlist.tracker.module.remotedatasource.retrofit`
- Dependencies: `:module:remote-data-source`, `:module:domain`, Retrofit, OkHttp, Moshi, KSP.

**`:module:repository`** — Pure Kotlin Library
- Contains: repository interfaces (`AnimeRepository`, `SeasonRepository`, `UserPreferencesRepository`, `TransactionRunner`), repository implementations (`AnimeRepositoryImpl`, `SeasonRepositoryImpl`, `UserPreferencesRepositoryImpl`), and entity mappers.
- `AnimeRepositoryImpl` delegates all remote-fetching operations to `AnimeRemoteDataSource` internally; `AnimeRemoteDataSource` is not exposed to consumers.
- Implementations are the only classes that coordinate between local and remote data sources.
- Package: `com.vuzeda.animewatchlist.tracker.module.repository`
- Dependencies: `:module:remote-data-source` (implementation), `:module:local-data-source`, `:module:domain`.

**`:module:use-case`** — Pure Kotlin Library
- Contains: all use cases. Each represents a single business operation with a single `operator fun invoke(...)` method.
- Use cases receive repository interfaces via constructor injection. They must not call other use cases — compose at the ViewModel level.
- Package: `com.vuzeda.animewatchlist.tracker.module.usecase`
- Dependencies: `:module:repository`, `:module:domain`.

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
- Dependencies: `:module:use-case`, `:module:design-system`, `:module:domain`. Never depend on any `:module:local-data-source*` or `:module:remote-data-source*` module.

**`:app`** — Android Application
- The entry point. Contains the `Application` class, `MainActivity`, and all Hilt wiring.
- This is the only module that knows about every other module.
- Contains Hilt `@Module` classes that bind implementations to interfaces: `RepositoryModule`, `DatabaseModule`, `NetworkModule`, `PreferencesModule`, `WorkManagerModule`, and `ClockModule` (provides `kotlin.time.Clock` as an injectable abstraction for testability).
- Also contains `notification/NotificationHelper` and `worker/AnimeUpdateWorker`.
- No business logic, no UI screens, no data access — only DI configuration and app-level setup.
- **Product flavors** (`environment` dimension): `prod` (real `JikanApiService` + `ChiakiServiceImpl` via `ApiServiceModule`) and `mock` (`FakeJikanApiService` + `FakeChiakiService` via `MockApiServiceModule`, plus `MockMainActivity` and `ScreenshotSeeder` for screenshot testing).
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
:module:local-data-source    — no module deps
:module:remote-data-source   → :module:domain
:module:repository           → :module:remote-data-source
                             → :module:local-data-source
                             → :module:domain
:module:use-case             → :module:repository
                             → :module:domain
:module:design-system        — no module deps
:module:ui                   → :module:use-case
                             → :module:design-system
                             → :module:domain
:module:local-data-source-room → :module:local-data-source
                               → :module:domain
                               → :module:repository (TransactionRunner)
:module:remote-data-source-retrofit → :module:remote-data-source
                                    → :module:domain
:app → :module:domain
     → :module:design-system
     → :module:ui
     → :module:use-case
     → :module:repository
     → :module:local-data-source
     → :module:local-data-source-room
     → :module:remote-data-source
     → :module:remote-data-source-retrofit
```

**Critical boundary rules:**
- `:module:domain` has zero dependencies on Android or other modules
- `:module:ui` never imports any `:module:local-data-source*` or `:module:remote-data-source*` module
- `:module:design-system` never imports `:module:domain` or any other module
- Only `:app` sees all modules (cross-module Hilt bindings live here)

### Module Gradle Configuration

- Pure Kotlin modules apply `java-library` and `org.jetbrains.kotlin.jvm` plugins only.
  Current pure Kotlin modules: `:module:domain`, `:module:local-data-source`, `:module:remote-data-source`, `:module:remote-data-source-retrofit`, `:module:repository`, `:module:use-case`.
- Android library modules apply `com.android.library` and related plugins.
  Current Android library modules: `:module:local-data-source-room`, `:module:design-system`, `:module:ui`.
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

```
AnimeWatchlistTracker/
├── app/                          # :app — Android Application module
│   └── src/
│       ├── main/java/.../
│       │   ├── AnimeWatchlistApp.kt
│       │   ├── MainActivity.kt
│       │   ├── di/
│       │   │   ├── RepositoryModule.kt
│       │   │   ├── DatabaseModule.kt
│       │   │   ├── NetworkModule.kt
│       │   │   ├── PreferencesModule.kt
│       │   │   ├── WorkManagerModule.kt
│       │   │   └── ClockModule.kt
│       │   ├── notification/
│       │   │   └── NotificationHelper.kt
│       │   └── worker/
│       │       └── AnimeUpdateWorker.kt
│       ├── prod/java/.../
│       │   └── di/
│       │       └── ApiServiceModule.kt
│       └── mock/java/.../
│           ├── MockMainActivity.kt
│           ├── ScreenshotSeeder.kt
│           └── di/
│               └── MockApiServiceModule.kt
├── module/
│   ├── domain/                   # :module:domain — Pure Kotlin library
│   │   └── src/main/kotlin/.../module/domain/
│   │       └── (16 model files: Anime, AnimeFullDetails, AnimeSeason, ...)
│   ├── local-data-source/        # :module:local-data-source — Pure Kotlin library
│   │   └── src/main/kotlin/.../module/localdatasource/
│   │       ├── Anime.kt
│   │       ├── Season.kt
│   │       ├── AnimeLocalDataSource.kt
│   │       ├── SeasonLocalDataSource.kt
│   │       └── UserPreferencesLocalDataSource.kt
│   ├── local-data-source-room/   # :module:local-data-source-room — Android library
│   │   └── src/main/java/.../module/localdatasource/room/
│   │       ├── dao/
│   │       ├── entity/
│   │       ├── preferences/
│   │       └── database/
│   ├── remote-data-source/       # :module:remote-data-source — Pure Kotlin library
│   │   └── src/main/kotlin/.../module/remotedatasource/
│   │       └── AnimeRemoteDataSource.kt
│   ├── remote-data-source-retrofit/ # :module:remote-data-source-retrofit — Pure Kotlin library
│   │   └── src/main/kotlin/.../module/remotedatasource/retrofit/
│   │       ├── AnimeRemoteDataSourceImpl.kt
│   │       ├── dto/
│   │       ├── mapper/
│   │       ├── service/
│   │       └── interceptor/
│   ├── repository/               # :module:repository — Pure Kotlin library
│   │   └── src/main/kotlin/.../module/repository/
│   │       ├── AnimeRepository.kt
│   │       ├── SeasonRepository.kt
│   │       ├── UserPreferencesRepository.kt
│   │       ├── TransactionRunner.kt
│   │       ├── impl/
│   │       └── mapper/
│   ├── use-case/                 # :module:use-case — Pure Kotlin library
│   │   └── src/main/kotlin/.../module/usecase/
│   │       └── (27 use case files)
│   ├── design-system/            # :module:design-system — Android library
│   │   └── src/main/java/.../module/designsystem/
│   │       ├── theme/
│   │       └── component/
│   └── ui/                       # :module:ui — Android library
│       └── src/main/java/.../module/ui/
│           ├── navigation/
│           └── screens/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
    └── libs.versions.toml
```

## Coding Standards

### Naming Conventions

Naming is the primary documentation in this project. Every name must be precise and self-explanatory.

- **Classes**: `PascalCase`. Use suffixes that communicate purpose: `AnimeRepository`, `GetWatchlistUseCase`, `HomeViewModel`, `AnimeEntity`, `AnimeDto`, `AnimeDetailScreen`.
- **Functions**: `camelCase`. Use verb-first names that describe the action: `fetchAnimeById`, `updateEpisodeProgress`, `deleteFromWatchlist`. Composable functions use `PascalCase` per Compose convention.
- **Variables and properties**: `camelCase`. Name them for what they represent, not their type: `watchingAnimeList` not `list`, `selectedStatus` not `status`.
- **Constants**: `SCREAMING_SNAKE_CASE` inside companion objects, `PascalCase` for top-level Compose constants.
- **State flows**: Name the private mutable version with an underscore prefix: `_uiState` / `uiState`.
- **Mapper functions**: `toDomainModel()`, `toLocalModel()`, `toEntity()`, `toDto()`. Always use extension functions on the source type. Use `toLocalModel()` when mapping domain models to the plain record classes in `:module:local-data-source`; use `toEntity()` when mapping to Room `@Entity` types within `:module:local-data-source-room`.
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
- **Timber** — Logging (debug builds only)

Do NOT introduce any dependency that is not listed above without explicit approval. Do not use experimental or alpha-stage libraries in production code.

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
2. Build the project — ensure it compiles without errors or warnings.
3. Run all existing unit tests — ensure they all pass.
4. If anything fails, fix it before proceeding.
5. Once the build and tests succeed, commit with a clear, descriptive message.
6. Continue to the next unit of work and repeat.

This ensures that every commit in the history represents a working, verified state of the project. Never commit code that does not compile or has failing tests.

## Before Submitting Code

1. All existing tests pass.
2. New code has corresponding unit tests.
3. No compiler warnings.
4. Code follows the naming and style conventions above.
5. No unnecessary comments in the code.
6. Architecture layer boundaries are respected — no cross-layer imports.
7. No hardcoded strings in the UI — use string resources.
8. `README.md` is updated to reflect any user-facing changes, new features, new dependencies, or architectural additions.
