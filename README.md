# 🎌 Anime Watchlist Tracker

[![CI](https://github.com/vruzeda/AnimeWatchlistTracker/actions/workflows/ci.yml/badge.svg)](https://github.com/vruzeda/AnimeWatchlistTracker/actions/workflows/ci.yml)
[![Coverage](https://codecov.io/gh/vruzeda/AnimeWatchlistTracker/graph/badge.svg)](https://codecov.io/gh/vruzeda/AnimeWatchlistTracker)
[![Release](https://img.shields.io/github/v/release/vruzeda/AnimeWatchlistTracker)](https://github.com/vruzeda/AnimeWatchlistTracker/releases/latest)
[![Min SDK](https://img.shields.io/badge/minSdk-26-brightgreen)](https://developer.android.com/about/versions/oreo)

A clean, offline-first Android app for tracking your anime journey. Organize what you're watching, what you've completed, and what's next — all from your pocket.

## About

Anime Watchlist Tracker is a personal anime management app built for fans who want a lightweight, fast, and private way to keep track of their anime library. No account required, no data collection — just you and your list.

### Key Features

- **Watchlist Management** — Add, edit, and remove anime from your personal library
- **Season Grouping** — Anime are organized as series with multiple seasons, resolved automatically via chiaki.site watch order data with progressive loading of prequels and sequels
- **Status Categories** — Organize anime by status:
  - 📺 Watching
  - ✅ Completed
  - 📋 Plan to Watch
  - ⏸️ On Hold
  - ❌ Dropped
- **Episode Progress** — Track which episode you're on for each season
- **Rating System** — Rate anime on a 1–10 star scale
- **Anime Search** — Look up anime info using the Jikan API (unofficial MyAnimeList API) and add to your watchlist
- **Search Result Browsing** — Tap any search result to view full details, or use the "+" button to add directly to your watchlist
- **Seasonal Browsing** — Browse anime by year and season (Winter, Spring, Summer, Fall) with pagination, sorting, and quick add to watchlist
- **Sorting** — Sort your watchlist, search results, and seasonal lists by title, score, or recently added, with reversible ascending/descending direction
- **Filtering** — Multi-dimensional filtering on the Home screen (by status and notification toggle) and search results (All, In Watchlist, Not in Watchlist)
- **Home View Mode** — Toggle the Home screen between Anime view (one card per series) and Season view (one card per individual season), configurable in Settings
- **Notifications** — Enable per-anime notifications to get daily checks for new episodes and new seasons
- **Title Language** — Choose to display anime titles in Romaji (default), English, or Japanese
- **Localization** — UI fully translated in English, Brazilian Portuguese, Latin American Spanish, and French
- **Settings** — Configure title language preference, Home view mode, and delete all data
- **Offline First** — All data stored locally on your device using Room, with user preferences in DataStore
- **Material You** — Modern, dynamic theming that adapts to your wallpaper (Android 12+)

## Screenshots

_Coming soon_

## Tech Stack

- **Language** — Kotlin 2.3.21
- **UI Framework** — Jetpack Compose (BOM 2026.04.01) + Material 3
- **Architecture** — Clean Architecture (multi-module) + MVVM
- **Build System** — Gradle 9.5.0, AGP 9.2.0
- **Local Database** — Room 2.8.4
- **Preferences** — DataStore 1.2.1
- **Dependency Injection** — Hilt 2.59.2
- **Networking** — Retrofit 3.0.0 + OkHttp 5.3.2
- **JSON Parsing** — Moshi 1.15.2
- **Serialization** — Kotlinx Serialization 1.11.0
- **Image Loading** — Coil 2.7.0
- **Navigation** — Jetpack Navigation Compose 2.9.8
- **Background Work** — WorkManager 2.11.2
- **Analytics & Crash Reporting** — Firebase Analytics, Firebase Crashlytics
- **Async** — Kotlin Coroutines 1.10.2 + Flow
- **Logging** — Timber 5.0.1
- **API** — Jikan v4 (MyAnimeList unofficial API), chiaki.site (watch order scraping)
- **Testing** — JUnit 5, MockK, Turbine, Truth
- **Min SDK** — API 26 (Android 8.0)
- **Target SDK** — API 37 (Android 15)

## Architecture

The project follows **Clean Architecture** with strict layer separation enforced at the **Gradle module level**, and **MVVM** in the UI layer. Each layer is its own independent module. Dependencies point inward only.

| Module | Type | Description |
|--------|------|-------------|
| `:module:domain` | Pure Kotlin lib | Domain models only |
| `:module:local-data-source` | Pure Kotlin lib | Local data source interfaces |
| `:module:local-data-source-room` | Android lib | Room entities, DAOs, DataStore, migrations |
| `:module:remote-data-source` | Pure Kotlin lib | Remote data source interfaces |
| `:module:remote-data-source-retrofit` | Pure Kotlin lib | Retrofit impl, DTOs, DTO mappers, interceptors |
| `:module:remote-data-source-firebase` | Android lib | Firebase Firestore impl (prod only) |
| `:module:repository` | Pure Kotlin lib | Repository interfaces + implementations |
| `:module:use-case` | Pure Kotlin lib | All use cases |
| `:module:analytics` | Pure Kotlin lib | Analytics interface + no-op impl |
| `:module:analytics-firebase` | Android lib | Firebase Analytics impl (prod only) |
| `:module:notification` | Pure Kotlin lib | Notification interface |
| `:module:notification-android` | Android lib | Android notification implementation |
| `:module:scheduler` | Pure Kotlin lib | Scheduler interface |
| `:module:scheduler-work` | Android lib | WorkManager scheduler implementation |
| `:module:design-system` | Android lib | Material 3 theme, reusable Compose components |
| `:module:ui` | Android lib | Compose screens, ViewModels, navigation (MVVM) |
| `:app` | Android app | Hilt entry point, DI wiring |

- `:module:ui` screens are built from `:module:design-system` components; ViewModels call use cases from `:module:use-case`.
- `:module:repository` is the only layer that coordinates local (Room) and remote (Retrofit/Firebase) data sources.
- `:module:domain` has zero dependencies — it is the innermost layer.
- `:app` is the only module that sees everything; all Hilt cross-module bindings live here.

### Screens

- **Home** — Watchlist with multi-dimensional filtering (by status and notification toggle), sortable by multiple criteria with reversible direction
- **Seasons** — Browse anime by year and season via Jikan API, with pagination, sorting, and quick add to watchlist
- **Search** — Search anime via Jikan API, filter by watchlist status, sort results, and browse or add to your watchlist
- **Settings** — Title language preference (Romaji/English/Japanese) and delete all data
- **Anime Detail** — View anime series details with grouped season list, edit status/rating, toggle notifications, progressive prequel/sequel resolution
- **Season Detail** — View individual season details with paginated episode list and episode progress tracking

## API

This app uses two external data sources:

### Jikan API v4
[Jikan](https://jikan.moe/) is a free, open-source, unofficial API for MyAnimeList.

- No API key required
- Rate limited to ~3 requests/second
- Used for search, seasonal browsing, detail viewing, episode lists, and daily notification checks
- Endpoints used: `/v4/anime` (search), `/v4/anime/{id}` (details), `/v4/anime/{id}/full` (full details with relations), `/v4/anime/{id}/episodes` (episode list), `/v4/seasons/{year}/{season}` (seasonal browsing)

### chiaki.site
[chiaki.site](https://chiaki.site/) provides anime watch order data.

- Used to resolve series structure (prequels, sequels, related entries) for season grouping
- HTML scraping — no official API
- All user data stays local

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) Meerkat (2025.1.1) or newer (AGP 9.2.0 required)
- JDK 17+
- Android SDK with API 37

### Build & Run

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/AnimeWatchlistTracker.git
   ```

2. Open the project in Android Studio

3. Sync Gradle and run on an emulator or physical device

### Run Tests

```bash
./gradlew test
```

### Build Release APK

```bash
./gradlew assembleRelease
```

## Roadmap

- [x] Multi-module Gradle setup
- [x] Domain layer (models, repository interfaces, use cases)
- [x] Local database with Room
- [x] API layer (Jikan v4 DTOs, Retrofit service)
- [x] Repository layer with mappers
- [x] Design system (theme, reusable components)
- [x] Home screen with multi-dimensional filtering and sorting
- [x] Anime search via Jikan API
- [x] Detail screen with episode tracking and rating
- [x] DI wiring with Hilt
- [x] Per-anime notification toggle with daily update checks
- [x] Search result filtering (All, In Watchlist, Not in Watchlist) and sorting
- [x] Reversible sort direction across all screens
- [x] View anime details directly from search (without adding to watchlist)
- [x] Harmonized card layout across all screens
- [x] Season grouping with chiaki.site watch order resolution
- [x] Progressive prequel/sequel loading in anime detail
- [x] Split detail into Anime Detail (series) and Season Detail (individual season + episodes)
- [x] Seasonal anime browsing (by year and season)
- [x] Settings screen with title language preference and delete all data
- [x] Title language preference (Romaji, English, Japanese)
- [ ] Data export/import
- [ ] Theme customization
- [ ] Widget for currently watching

## Contributing

This is a personal project, but contributions are welcome! Feel free to open issues or submit pull requests.

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

Built with ❤️ and way too much anime.
