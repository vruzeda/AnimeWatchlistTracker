# 🎌 Anime Watchlist Tracker

A clean, offline-first Android app for tracking your anime journey. Organize what you're watching, what you've completed, and what's next — all from your pocket.

## About

Anime Watchlist Tracker is a personal anime management app built for fans who want a lightweight, fast, and private way to keep track of their anime library. No account required, no data collection — just you and your list.

### Key Features

- **Watchlist Management** — Add, edit, and remove anime from your personal library
- **Status Categories** — Organize anime by status:
  - 📺 Watching
  - ✅ Completed
  - 📋 Plan to Watch
  - ⏸️ On Hold
  - ❌ Dropped
- **Episode Progress** — Track which episode you're on for each show
- **Rating System** — Rate anime on a 1–10 star scale
- **Anime Search** — Look up anime info using the Jikan API (unofficial MyAnimeList API) and add to your watchlist
- **Notifications** — Enable per-anime notifications to get daily checks for new episodes and new seasons
- **Offline First** — All data stored locally on your device using Room
- **Material You** — Modern, dynamic theming that adapts to your wallpaper (Android 12+)

## Screenshots

_Coming soon_

## Tech Stack

- **Language** — Kotlin 2.0.21
- **UI Framework** — Jetpack Compose + Material 3
- **Architecture** — Clean Architecture (multi-module) + MVVM
- **Build System** — Gradle 9.2.1, AGP 9.0.1
- **Local Database** — Room 2.8.4
- **Dependency Injection** — Hilt 2.59.2
- **Networking** — Retrofit 2.11.0 + OkHttp 4.12.0
- **JSON Parsing** — Moshi 1.15.1
- **Image Loading** — Coil 2.7.0
- **Navigation** — Jetpack Navigation Compose 2.8.5
- **Background Work** — WorkManager 2.10.1
- **Async** — Kotlin Coroutines 1.9.0 + Flow
- **API** — Jikan v4 (MyAnimeList unofficial API)
- **Testing** — JUnit 5, MockK, Turbine, Truth
- **Min SDK** — API 26 (Android 8.0)
- **Target SDK** — API 36 (Android 16)

## Architecture

The project follows **Clean Architecture** with strict layer separation enforced at the **Gradle module level**, and **MVVM** in the UI layer. Each layer is its own independent module. Dependencies point inward only.

```
AnimeWatchlistTracker/
├── app/                  # :app — Android Application (Hilt wiring, DI modules)
├── domain/               # :domain — Pure Kotlin library (models, repository interfaces, use cases)
├── data/
│   ├── api/              # :data:api — Pure Kotlin library (Retrofit service, DTOs)
│   ├── local/            # :data:local — Android library (Room entities, DAOs, database)
│   └── repository/       # :data:repository — Android library (repository impls, mappers)
├── designsystem/         # :designsystem — Android library (theme, reusable Compose components)
└── ui/                   # :ui — Android library (screens, ViewModels, navigation)
```

### Module Dependency Graph

```
:app → :ui → :designsystem
              → :domain
:app → :data:repository → :data:api    → :domain
                        → :data:local   → :domain
```

`:domain` depends on nothing. `:designsystem` depends on nothing (no business logic modules). `:ui` depends on `:domain` and `:designsystem`. The `:data` modules depend on `:domain`. Only `:app` sees everything.

### Screens

- **Home** — Watchlist with scrollable status tabs (All, Watching, Completed, Plan to Watch, On Hold, Dropped)
- **Search** — Search anime via Jikan API and add results to your watchlist
- **Detail** — View anime details, edit status/episode progress/rating, toggle notifications, delete from watchlist

## API

This app uses the [Jikan API v4](https://jikan.moe/) to fetch anime information, cover art, and metadata. Jikan is a free, open-source, unofficial API for MyAnimeList.

- No API key required
- Rate limited to ~3 requests/second
- Used for search and daily notification checks (episode count + sequel detection via `/v4/anime/{id}/full`)
- All user data stays local

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) Meerkat (2025.1.1) or newer (AGP 9.0.1 required)
- JDK 17+
- Android SDK with API 36

### Build & Run

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/AnimeWatchlistTracker.git
   ```

2. Open the project in Android Studio

3. Sync Gradle and run on an emulator or physical device

### Run Tests

```bash
./gradlew :domain:test :data:repository:test :ui:test
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
- [x] Home screen with watchlist status tabs
- [x] Anime search via Jikan API
- [x] Detail screen with episode tracking and rating
- [x] DI wiring with Hilt
- [x] Per-anime notification toggle with daily update checks
- [ ] Settings screen (theme, data export/import)
- [ ] Search and filter within local list
- [ ] Widget for currently watching

## Contributing

This is a personal project, but contributions are welcome! Feel free to open issues or submit pull requests.

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

Built with ❤️ and way too much anime.
