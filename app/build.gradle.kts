import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load(keystorePropertiesFile.inputStream())
    }
}

android {
    namespace = "com.vuzeda.animewatchlist.tracker"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.vuzeda.animewatchlist.tracker"
        minSdk = 26
        targetSdk = 36
        versionCode = 31
        versionName = "5.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    if (keystorePropertiesFile.exists()) {
        signingConfigs {
            create("release") {
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("prod") {
            isDefault = true
        }
        create("mock") {
            applicationIdSuffix = ".mock"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

afterEvaluate {
    tasks.matching { it.name.matches(Regex("process.*Mock.*GoogleServices")) }.configureEach {
        enabled = false
    }
}

dependencies {
    implementation(project(":module:domain"))
    implementation(project(":module:design-system"))
    implementation(project(":module:ui"))
    implementation(project(":module:use-case"))
    implementation(project(":module:repository"))
    implementation(project(":module:local-data-source"))
    implementation(project(":module:local-data-source-room"))
    implementation(project(":module:remote-data-source"))
    implementation(project(":module:remote-data-source-retrofit"))
    implementation(project(":module:notification"))
    implementation(project(":module:notification-android"))
    implementation(project(":module:scheduler"))
    implementation(project(":module:scheduler-work"))
    implementation(project(":module:analytics"))
    "prodImplementation"(project(":module:analytics-firebase"))
    "prodImplementation"(project(":module:remote-data-source-firebase"))

    "prodImplementation"(platform(libs.firebase.bom))
    "prodImplementation"(libs.firebase.analytics)
    "prodImplementation"(libs.firebase.auth)
    "prodImplementation"(libs.firebase.crashlytics)
    "prodImplementation"(libs.firebase.firestore)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    implementation(libs.room.runtime)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.moshi)

    implementation(libs.timber)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
