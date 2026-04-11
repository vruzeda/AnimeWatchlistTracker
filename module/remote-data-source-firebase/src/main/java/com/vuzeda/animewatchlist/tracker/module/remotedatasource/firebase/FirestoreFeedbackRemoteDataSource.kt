package com.vuzeda.animewatchlist.tracker.module.remotedatasource.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations
import com.vuzeda.animewatchlist.tracker.module.domain.Feedback
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.FeedbackRemoteDataSource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreFeedbackRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : FeedbackRemoteDataSource {

    override suspend fun submit(feedback: Feedback): Result<Unit> = runCatching {
        val installationId = FirebaseInstallations.getInstance().id.await()
        val document = mapOf(
            "category"       to feedback.category.name,
            "message"        to feedback.message,
            "appVersion"     to feedback.appVersion,
            "timestamp"      to feedback.timestamp,
            "deviceModel"    to feedback.deviceModel,
            "androidVersion" to feedback.androidVersion,
            "installationId" to installationId,
            "titleLanguage"  to feedback.titleLanguage,
            "homeViewMode"   to feedback.homeViewMode
        )
        firestore.collection("feedback").add(document).await()
        Unit
    }
}
