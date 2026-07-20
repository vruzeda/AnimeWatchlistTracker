package com.vuzeda.animewatchlist.tracker.module.ui.screens

import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeDayOfWeek
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeSeason
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdateResult
import com.vuzeda.animewatchlist.tracker.module.domain.BroadcastTime
import com.vuzeda.animewatchlist.tracker.module.domain.EpisodeInfo
import com.vuzeda.animewatchlist.tracker.module.domain.NotificationType
import com.vuzeda.animewatchlist.tracker.module.domain.SearchResult
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.WatchStatus
import com.vuzeda.animewatchlist.tracker.module.ui.screens.animedetail.AnimeDetailUiState
import com.vuzeda.animewatchlist.tracker.module.ui.screens.developer.DeveloperUiState
import com.vuzeda.animewatchlist.tracker.module.ui.screens.home.HomeSeasonItem
import com.vuzeda.animewatchlist.tracker.module.ui.screens.home.HomeUiState
import com.vuzeda.animewatchlist.tracker.module.ui.screens.schedule.ScheduleUiState
import com.vuzeda.animewatchlist.tracker.module.ui.screens.search.SearchUiState
import com.vuzeda.animewatchlist.tracker.module.ui.screens.seasondetail.SeasonDetailUiState
import com.vuzeda.animewatchlist.tracker.module.ui.screens.seasons.SeasonsUiState
import com.vuzeda.animewatchlist.tracker.module.ui.screens.settings.SettingsUiState
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import kotlin.time.Instant

internal object ScreenPreviewSamples {

    private val frieren = Anime(
        id = 1,
        title = "Sousou no Frieren",
        titleEnglish = "Frieren: Beyond Journey's End",
        genres = listOf("Adventure", "Drama", "Fantasy"),
        status = WatchStatus.WATCHING,
        userRating = 9,
        notificationType = NotificationType.BOTH
    )

    private val steinsGate = Anime(
        id = 2,
        title = "Steins;Gate",
        genres = listOf("Drama", "Sci-Fi", "Thriller"),
        status = WatchStatus.COMPLETED,
        userRating = 10
    )

    private val frierenSeason = Season(
        id = 1,
        animeId = 1,
        malId = 52991,
        title = "Sousou no Frieren",
        titleEnglish = "Frieren: Beyond Journey's End",
        episodeCount = 28,
        watchedEpisodeCount = 12,
        status = WatchStatus.WATCHING,
        score = 9.3,
        airingStatus = "Finished Airing",
        broadcastInfo = "Fridays at 23:00 (JST)",
        broadcastTime = BroadcastTime(
            dayOfWeek = DayOfWeek.FRIDAY,
            time = LocalTime.of(23, 0),
            zoneId = ZoneId.of("Asia/Tokyo")
        ),
        airingSeasonName = "fall",
        airingSeasonYear = 2023
    )

    private val frierenSequel = Season(
        id = 2,
        animeId = 1,
        malId = 59978,
        title = "Sousou no Frieren 2nd Season",
        status = WatchStatus.PLAN_TO_WATCH,
        orderIndex = 1,
        airingStatus = "Currently Airing",
        airingSeasonName = "winter",
        airingSeasonYear = 2026
    )

    private val searchResults = listOf(
        SearchResult(
            malId = 52991,
            title = "Sousou no Frieren",
            titleEnglish = "Frieren: Beyond Journey's End",
            episodeCount = 28,
            score = 9.3,
            type = "TV",
            genres = listOf("Adventure", "Fantasy")
        ),
        SearchResult(
            malId = 9253,
            title = "Steins;Gate",
            episodeCount = 24,
            score = 9.1,
            type = "TV",
            genres = listOf("Sci-Fi", "Thriller")
        )
    )

    private val episodes = listOf(
        EpisodeInfo(
            number = 1,
            titleRomaji = null,
            titleEnglish = "The Journey's End",
            titleJapanese = null,
            aired = "2023-09-29",
            isFiller = false,
            isRecap = false
        ),
        EpisodeInfo(
            number = 2,
            titleRomaji = null,
            titleEnglish = "It Didn't Have to Be Magic",
            titleJapanese = null,
            aired = "2023-10-06",
            isFiller = false,
            isRecap = false
        ),
        EpisodeInfo(
            number = 3,
            titleRomaji = null,
            titleEnglish = "Killing Magic",
            titleJapanese = null,
            aired = "2023-10-13",
            isFiller = false,
            isRecap = false
        )
    )

    val homeUiState = HomeUiState(
        isLoading = false,
        animeList = listOf(frieren, steinsGate),
        seasonItems = listOf(HomeSeasonItem(season = frierenSeason))
    )

    val searchUiState = SearchUiState(
        query = "frieren",
        results = searchResults,
        hasSearched = true,
        addedMalIds = setOf(52991)
    )

    val settingsUiState = SettingsUiState(
        coverCacheSizeBytes = 42_000_000,
        isDeveloperOptionsEnabled = true
    )

    val seasonDetailUiState = SeasonDetailUiState(
        isLoading = false,
        season = frierenSeason,
        episodes = episodes,
        watchedEpisodes = setOf(1, 2),
        isEpisodeNotificationsEnabled = true,
        localBroadcastTime = BroadcastTime(
            dayOfWeek = DayOfWeek.FRIDAY,
            time = LocalTime.of(23, 0),
            zoneId = ZoneId.of("Asia/Tokyo")
        )
    )

    val animeDetailUiState = AnimeDetailUiState(
        isLoading = false,
        anime = frieren,
        seasons = listOf(frierenSeason, frierenSequel),
        notificationType = NotificationType.BOTH
    )

    val scheduleUiState = ScheduleUiState(
        selectedYear = 2026,
        selectedSeason = AnimeSeason.SUMMER,
        schedule = mapOf(AnimeDayOfWeek.FRIDAY to listOf(frierenSeason)),
        availableSeasons = listOf(2026 to AnimeSeason.SUMMER),
        isLoading = false
    )

    val seasonsUiState = SeasonsUiState(
        selectedYear = 2026,
        selectedSeason = AnimeSeason.SUMMER,
        currentYear = 2026,
        currentSeason = AnimeSeason.SUMMER,
        animeList = searchResults,
        hasNextPage = true,
        addedMalIds = setOf(52991)
    )

    val developerUiState = DeveloperUiState(
        lastAnimeUpdateRun = Instant.fromEpochMilliseconds(1_751_500_000_000),
        lastAnimeUpdateAttemptAt = Instant.fromEpochMilliseconds(1_751_543_200_000),
        lastAnimeUpdateAttemptResult = AnimeUpdateResult.Success,
        isNotificationDebugInfoEnabled = true
    )
}
