plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.vuzeda.animewatchlist.tracker.module.analytics.firebase"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
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
