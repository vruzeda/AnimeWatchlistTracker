plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    jacoco
}

android {
    namespace = "com.vuzeda.animewatchlist.tracker.module.localdatasource.room"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            all { test ->
                test.useJUnitPlatform()
            }
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(project(":module:local-data-source"))
    implementation(project(":module:domain"))
    implementation(project(":module:repository"))

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.truth)
}

val jacocoExclude = listOf(
    "**/R.class", "**/R\$*.class", "**/BuildConfig.*", "**/Manifest*.*",
    "**/dao/**",
    "**/database/AnimeDatabase*",
    "**/database/Migrations*",
    "**/database/RoomTransactionRunner*",
    "**/preferences/**"
)

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn("testDebugUnitTest")
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")) {
            exclude(jacocoExclude)
        }
    )
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("**/*.exec", "**/*.ec")
        }
    )
}
