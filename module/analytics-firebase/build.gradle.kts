plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.vuzeda.animewatchlist.tracker.module.analytics.firebase"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.androidMinSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":module:analytics"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
}
