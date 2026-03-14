package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.repository.AnimeRepository
import javax.inject.Inject

/** Deletes all anime and their seasons from the local database. */
class DeleteAllDataUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {

    suspend operator fun invoke() = animeRepository.deleteAllData()
}
