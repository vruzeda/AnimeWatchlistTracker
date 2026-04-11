package com.vuzeda.animewatchlist.tracker.module.repository.impl

import com.vuzeda.animewatchlist.tracker.module.domain.Feedback
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.FeedbackRemoteDataSource
import com.vuzeda.animewatchlist.tracker.module.repository.FeedbackRepository
import javax.inject.Inject

class FeedbackRepositoryImpl @Inject constructor(
    private val remote: FeedbackRemoteDataSource
) : FeedbackRepository {

    override suspend fun submitFeedback(feedback: Feedback): Result<Unit> =
        remote.submit(feedback)
}
