package com.vuzeda.animewatchlist.tracker.module.repository

import com.vuzeda.animewatchlist.tracker.module.domain.Feedback

interface FeedbackRepository {
    suspend fun submitFeedback(feedback: Feedback): Result<Unit>
}
