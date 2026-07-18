package com.vuzeda.animewatchlist.tracker

import com.google.firebase.crashlytics.FirebaseCrashlytics

internal fun initializeFirebase() {
    FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
}
