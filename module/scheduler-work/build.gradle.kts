plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.vuzeda.animewatchlist.tracker.module.scheduler.work"
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
    implementation(project(":module:scheduler"))
    implementation(project(":module:notification"))
    implementation(project(":module:use-case"))
    implementation(project(":module:domain"))

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    implementation(libs.javax.inject)
}
