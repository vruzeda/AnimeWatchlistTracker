package com.vuzeda.animewatchlist.tracker.module.remotedatasource

import com.vuzeda.animewatchlist.tracker.module.domain.Feedback

interface FeedbackRemoteDataSource {
    suspend fun submit(feedback: Feedback): Result<Unit>
}
