package com.vuzeda.animewatchlist.tracker.module.remotedatasource.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.vuzeda.animewatchlist.tracker.module.domain.Feedback
import com.vuzeda.animewatchlist.tracker.module.remotedatasource.FeedbackRemoteDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreFeedbackRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : FeedbackRemoteDataSource {

    override suspend fun submit(feedback: Feedback): Result<Unit> = try {
        val document = mapOf(
            "category"       to feedback.category.name,
            "message"        to feedback.message,
            "appVersion"     to feedback.appVersion,
            "timestamp"      to feedback.timestamp,
            "deviceModel"    to feedback.deviceModel,
            "androidVersion" to feedback.androidVersion,
            "installationId" to feedback.installationId,
            "titleLanguage"  to feedback.titleLanguage,
            "homeViewMode"   to feedback.homeViewMode,
            "archived"       to false,
            "contactName"    to feedback.contactName,
            "contactEmail"   to feedback.contactEmail
        )
        firestore.collection("feedback").add(document).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
