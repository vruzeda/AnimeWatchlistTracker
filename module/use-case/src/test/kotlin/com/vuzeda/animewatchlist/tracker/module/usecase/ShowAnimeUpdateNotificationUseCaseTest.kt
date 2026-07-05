package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.Anime
import com.vuzeda.animewatchlist.tracker.module.domain.AnimeUpdate
import com.vuzeda.animewatchlist.tracker.module.domain.Season
import com.vuzeda.animewatchlist.tracker.module.domain.TitleLanguage
import com.vuzeda.animewatchlist.tracker.module.notification.AnimeUpdateNotifier
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ShowAnimeUpdateNotificationUseCaseTest {

    private val animeUpdateNotifier = mockk<AnimeUpdateNotifier>(relaxUnitFun = true)
    private val useCase = ShowAnimeUpdateNotificationUseCase(animeUpdateNotifier)

    @Test
    fun `creates notification channels before showing the update notification`() = runTest {
        val update = AnimeUpdate.NewEpisodes(
            anime = Anime(id = 1, title = "One Piece"),
            season = Season(malId = 10, title = "One Piece"),
            newEpisodeCount = 2
        )

        useCase(update = update, titleLanguage = TitleLanguage.DEFAULT)

        coVerifyOrder {
            animeUpdateNotifier.createNotificationChannels()
            animeUpdateNotifier.showUpdateNotification(update = update, titleLanguage = TitleLanguage.DEFAULT)
        }
    }

    @Test
    fun `delegates new season update to the notifier with the given title language`() = runTest {
        val update = AnimeUpdate.NewSeason(
            anime = Anime(id = 2, title = "Attack on Titan"),
            sequelMalId = 20,
            sequelTitle = "Attack on Titan: Final Season"
        )

        useCase(update = update, titleLanguage = TitleLanguage.ENGLISH)

        coVerify(exactly = 1) {
            animeUpdateNotifier.showUpdateNotification(update = update, titleLanguage = TitleLanguage.ENGLISH)
        }
    }
}
