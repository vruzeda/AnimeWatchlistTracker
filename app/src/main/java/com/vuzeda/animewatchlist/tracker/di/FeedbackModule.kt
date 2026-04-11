package com.vuzeda.animewatchlist.tracker.di

import com.google.firebase.firestore.FirebaseFirestore
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.FeedbackRemoteDataSource
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.firebase.FirestoreFeedbackRemoteDataSource
import com.vuzeda.animewatchlist.tracker.module.repository.FeedbackRepository
import com.vuzeda.animewatchlist.tracker.module.repository.impl.FeedbackRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FeedbackModule {

    @Binds
    @Singleton
    abstract fun bindFeedbackRemoteDataSource(
        impl: FirestoreFeedbackRemoteDataSource
    ): FeedbackRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindFeedbackRepository(impl: FeedbackRepositoryImpl): FeedbackRepository

    companion object {

        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    }
}
