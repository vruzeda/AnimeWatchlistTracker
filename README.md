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
- **Rating System** — Rate anime on a 1–10 scale (MAL-style)
- **Search & Filter** — Quickly find anime in your list by title, genre, or status
- **Anime Search** — Look up anime info using the Jikan API (unofficial MyAnimeList API)
- **Offline First** — All data stored locally on your device
- **Material You** — Modern, dynamic theming that adapts to your wallpaper (Android 12+)

## Screenshots

_Coming soon_

## Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM + Clean Architecture |
| **Local Database** | Room (SQLite) |
| **Dependency Injection** | Hilt |
| **Networking** | Retrofit + OkHttp |
| **Image Loading** | Coil |
| **Navigation** | Jetpack Navigation Compose |
| **Async** | Kotlin Coroutines + Flow |
| **API** | Jikan v4 (MyAnimeList unofficial API) |
| **Min SDK** | API 26 (Android 8.0) |
| **Target SDK** | API 35 (Android 15) |

## Architecture

The project follows **MVVM (Model-View-ViewModel)** with **Clean Architecture** principles:

```
app/
├── data/
│   ├── local/          # Room database, DAOs, entities
│   ├── remote/         # Retrofit API service, DTOs
│   └── repository/     # Repository implementations
├── domain/
│   ├── model/          # Domain models
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Business logic use cases
├── di/                 # Hilt dependency injection modules
└── ui/
    ├── components/     # Reusable Compose components
    ├── navigation/     # Navigation graph
    ├── screens/        # Screen composables + ViewModels
    │   ├── home/
    │   ├── detail/
    │   ├── search/
    │   └── settings/
    └── theme/          # Material 3 theme, colors, typography
```

## API

This app uses the [Jikan API v4](https://jikan.moe/) to fetch anime information, cover art, and metadata. Jikan is a free, open-source, unofficial API for MyAnimeList.

- No API key required
- Rate limited to ~3 requests/second
- Used only for search and fetching anime details — all user data stays local

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) Ladybug (2024.2.1) or newer
- JDK 17+
- Android SDK with API 35

### Build & Run

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/AnimeWatchlistTracker.git
   ```

2. Open the project in Android Studio

3. Sync Gradle and run on an emulator or physical device

### Build Release APK

```bash
./gradlew assembleRelease
```

## Roadmap

- [x] Project setup and architecture
- [ ] Local database with Room
- [ ] Home screen with watchlist tabs
- [ ] Add/edit anime screen
- [ ] Anime search via Jikan API
- [ ] Detail screen with episode tracking
- [ ] Rating and notes
- [ ] Search and filter within local list
- [ ] Settings screen (theme, data export/import)
- [ ] Widget for currently watching
- [ ] Notifications for airing episodes

## Contributing

This is a personal project, but contributions are welcome! Feel free to open issues or submit pull requests.

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

Built with ❤️ and way too much anime.
