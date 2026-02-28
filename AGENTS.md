# AGENTS.md — Anime Watchlist Tracker

You are an expert Android engineer with deep knowledge of Kotlin, Jetpack Compose, and modern Android architecture. You write production-grade code that is clean, testable, and maintainable. You follow industry standards and never cut corners.

## Architecture

This project follows **Clean Architecture** with strict layer separation enforced at the **Gradle module level**. Each layer is its own independent library module. Dependencies point inward only, and the build system guarantees that layer boundaries cannot be violated.

### Module Types

Prefer **pure Kotlin library modules** (`java-library` + `kotlin` plugins) whenever possible. Only use **Android library modules** (`com.android.library`) when the code has a direct dependency on the Android framework (e.g., Room, Compose, Android Context). This maximizes build speed, testability, and portability.

- **Pure Kotlin module** — No Android dependencies. Tests run on the JVM without Robolectric or instrumentation. Use this by default.
- **Android library module** — Required when the code uses Android APIs, annotation processors that generate Android code (Room, Hilt), or Jetpack Compose.

### Module Overview

```
:domain          → Pure Kotlin library
:data:api        → Pure Kotlin library (Retrofit interfaces, DTOs, JSON parsing)
:data:local      → Android library (Room entities, DAOs, database)
:data:repository → Pure Kotlin library (repository implementations, mappers)
:designsystem    → Android library (design tokens, theme, reusable Compose components)
:ui              → Android library (Compose screens, ViewModels, navigation)
:app             → Android application (Hilt entry point, wires all modules together)
```

### Layer Rules

**`:domain`** — Pure Kotlin Library
- The innermost layer. It has ZERO dependencies on Android, frameworks, or other modules.
- Contains: models, repository interfaces, and use cases.
- Models are plain Kotlin data classes. They never contain annotations from Room, Retrofit, Moshi, or any framework.
- Repository interfaces define contracts. They never reference implementation details like DAOs, API services, or DTOs.
- Each use case represents a single business operation. Use cases receive repository interfaces via constructor injection and expose a single `operator fun invoke(...)` method. Use cases must not call other use cases — compose them at the ViewModel level if needed.
- Dependencies: `kotlin-stdlib`, `kotlinx-coroutines-core` only.

**`:data:api`** — Pure Kotlin Library
- Contains: Retrofit service interfaces, DTOs, and JSON serialization models.
- No Android dependencies. Retrofit interfaces and Moshi/Kotlin Serialization models are pure JVM.
- Dependencies: `:domain`, Retrofit, OkHttp, Moshi or Kotlin Serialization.

**`:data:local`** — Android Library
- Contains: Room database class, DAOs, and Room entities.
- This is an Android library module because Room requires Android annotation processing and the Android framework.
- Dependencies: `:domain`, Room.

**`:data:repository`** — Pure Kotlin Library
- Contains: repository implementations and mappers.
- Implements the repository interfaces defined in `:domain`.
- Depends on `:domain`, `:data:api`, and `:data:local` to coordinate between remote and local data sources.
- Every entity and DTO must have a corresponding mapper (extension function or mapper class) that converts to/from the domain model.
- Repository implementations are the only classes that know about both local and remote data sources.

**`:designsystem`** — Android Library
- The app's design system. All visual building blocks live here.
- Contains: Material 3 theme (colors, typography, shapes), design tokens, and all reusable Compose components (buttons, cards, dialogs, input fields, etc.).
- Components are purely presentational — they receive data and callbacks, never access ViewModels or business logic.
- Every component must be previewed with `@Preview` annotations using realistic sample data.
- The `:ui` module builds screens exclusively from components defined here. No ad-hoc styling or one-off components in `:ui`.
- Dependencies: Jetpack Compose BOM, Material 3, Coil (for image components). No dependency on `:domain` or any `:data` module.

**`:ui`** — Android Library
- Follows the **MVVM** pattern exclusively.
- Contains: screens (Composable functions + ViewModels) and navigation. Screens are assembled from `:designsystem` components.
- ViewModels expose UI state via `StateFlow` and handle user actions through clearly named methods. Never expose `MutableStateFlow` publicly.
- ViewModels depend on use cases only — never on repositories or data sources directly.
- UI state is modeled as a single sealed interface or data class per screen (e.g., `HomeUiState`). Avoid managing multiple independent state flows in a single ViewModel.
- Composable functions are stateless whenever possible. They receive state and callbacks as parameters.
- Dependencies: `:domain`, `:designsystem`. Never depend on any `:data` module.

**`:app`** — Android Application
- The entry point. Contains the `Application` class, `MainActivity`, and all Hilt wiring.
- This is the only module that knows about every other module.
- Contains Hilt `@Module` classes that bind implementations from `:data` modules to interfaces in `:domain`.
- No business logic, no UI screens, no data access — only DI configuration and app-level setup.
- Dependencies: all modules.

### Dependency Injection Across Modules

- **Hilt** is the DI framework. The `@HiltAndroidApp` annotation lives in `:app`.
- Each module defines its own Hilt `@Module` for providing its internal dependencies (e.g., `:data:local` provides its Room database and DAOs, `:data:api` provides its Retrofit service).
- Cross-module bindings (e.g., binding a repository implementation from `:data:repository` to a repository interface from `:domain`) are defined in `:app`'s DI modules. This keeps modules decoupled — no module needs to know who consumes its bindings.
- Use `@Binds` for interface-to-implementation mappings. Use `@Provides` only when construction logic is needed.
- Use `@Singleton` scope for repositories, database, and network clients. Use `@ViewModelScoped` for ViewModel-specific dependencies.
- Every injectable class uses constructor injection with `@Inject`. Avoid field injection.

### Dependency Direction

```
:app → :ui → :designsystem
              → :domain
:app → :data:repository → :data:api    → :domain
                        → :data:local   → :domain
```

`:domain` depends on nothing. `:designsystem` depends on nothing (no business logic modules). `:ui` depends on `:domain` and `:designsystem`. The `:data` modules depend on `:domain`. Only `:app` sees everything.

### Module Gradle Configuration

- Pure Kotlin modules apply `java-library` and `org.jetbrains.kotlin.jvm` plugins only.
- Android library modules apply `com.android.library`, `org.jetbrains.kotlin.android`, and `com.google.dagger.hilt.android` (if they provide Hilt modules).
- The `:app` module applies `com.android.application`, `org.jetbrains.kotlin.android`, and `com.google.dagger.hilt.android`.
- Use a shared `buildSrc` or convention plugin for common configuration (compile SDK, JVM target, Kotlin options) to avoid duplication.
- Use version catalogs (`libs.versions.toml`) for all dependency versions.

## Project Structure

```
AnimeWatchlistTracker/
├── app/                          # :app — Android Application module
│   └── src/main/java/.../
│       ├── AnimeWatchlistApp.kt
│       ├── MainActivity.kt
│       └── di/
│           ├── RepositoryModule.kt
│           ├── DatabaseModule.kt
│           └── NetworkModule.kt
├── domain/                       # :domain — Pure Kotlin library
│   └── src/main/kotlin/.../
│       ├── model/
│       ├── repository/
│       └── usecase/
├── data/
│   ├── api/                      # :data:api — Pure Kotlin library
│   │   └── src/main/kotlin/.../
│   │       ├── service/
│   │       └── dto/
│   ├── local/                    # :data:local — Android library
│   │   └── src/main/java/.../
│   │       ├── dao/
│   │       ├── entity/
│   │       └── database/
│   └── repository/               # :data:repository — Pure Kotlin library
│       └── src/main/kotlin/.../
│           ├── mapper/
│           └── impl/
├── designsystem/                 # :designsystem — Android library
│   └── src/main/java/.../
│       ├── theme/
│       │   ├── Color.kt
│       │   ├── Type.kt
│       │   ├── Shape.kt
│       │   └── Theme.kt
│       └── component/
│           ├── AnimeCard.kt
│           ├── RatingBar.kt
│           ├── StatusChip.kt
│           └── ...
├── ui/                           # :ui — Android library
│   └── src/main/java/.../
│       ├── navigation/
│       └── screens/
│           ├── home/
│           ├── detail/
│           ├── search/
│           ├── addEdit/
│           └── settings/
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
- **Mapper functions**: `toDomainModel()`, `toEntity()`, `toDto()`. Always use extension functions on the source type.
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
- Use `suspend` functions in repositories and use cases. Use `Flow` only when observing data over time.
- Avoid `lateinit` — prefer constructor injection or `lazy`.
- Use named arguments when calling functions with more than two parameters.
- Prefer expression bodies for single-expression functions.
- Never use `!!`. Handle nullability explicitly with `?.`, `?:`, or `let`.

### Jetpack Compose

- Every screen has its own package containing: the screen Composable, its ViewModel, and its UI state class.
- Screen Composables receive state and lambda callbacks. They do not access ViewModels directly — use a wrapper Composable that connects the ViewModel to the stateless screen.
- Screens are built exclusively from components in `:designsystem`. Do not create one-off styled components in `:ui` — if a new component is needed, add it to `:designsystem` first.
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
- **Moshi** or **Kotlin Serialization** — JSON parsing
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

At each meaningful step during development, the project must be built and all unit tests must pass before committing. Follow this workflow:

1. Complete a logical unit of work (e.g., a new module, a feature, a layer of the architecture).
2. Build the project — ensure it compiles without errors or warnings.
3. Run all existing unit tests — ensure they all pass.
4. If anything fails, fix it before proceeding.
5. Once the build and tests succeed, commit with a clear, descriptive message.

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
