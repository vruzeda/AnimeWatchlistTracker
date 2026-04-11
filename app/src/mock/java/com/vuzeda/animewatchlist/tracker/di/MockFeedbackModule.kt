package com.vuzeda.animewatchlist.tracker.di

import com.vuzeda.animewatchlist.tracker.module.domain.Feedback
import com.vuzeda.animewatchlist.tracker.module.repository.FeedbackRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MockFeedbackModule {

    @Provides
    @Singleton
    fun provideFeedbackRepository(): FeedbackRepository = object : FeedbackRepository {
        override suspend fun submitFeedback(feedback: Feedback): Result<Unit> = Result.success(Unit)
    }
}
