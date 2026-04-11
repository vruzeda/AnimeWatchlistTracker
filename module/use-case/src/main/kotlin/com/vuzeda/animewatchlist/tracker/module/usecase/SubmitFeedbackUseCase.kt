package com.vuzeda.animewatchlist.tracker.module.usecase

import com.vuzeda.animewatchlist.tracker.module.domain.Feedback
import com.vuzeda.animewatchlist.tracker.module.repository.FeedbackRepository
import javax.inject.Inject

/** Submits user feedback to the remote data store. */
class SubmitFeedbackUseCase @Inject constructor(
    private val feedbackRepository: FeedbackRepository
) {

    suspend operator fun invoke(feedback: Feedback): Result<Unit> =
        feedbackRepository.submitFeedback(feedback)
}
