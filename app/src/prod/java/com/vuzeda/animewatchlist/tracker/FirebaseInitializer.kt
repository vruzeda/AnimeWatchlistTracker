package com.vuzeda.animewatchlist.tracker

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal fun initializeFirebase() {
    FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    CoroutineScope(Dispatchers.IO).launch {
        FirebaseAuth.getInstance().signInAnonymously()
    }
}
