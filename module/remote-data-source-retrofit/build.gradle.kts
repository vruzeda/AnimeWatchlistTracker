plugins {
    id("kotlin-library")
    alias(libs.plugins.ksp)
    jacoco
}

dependencies {
    implementation(project(":module:remote-data-source"))
    implementation(project(":module:domain"))

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)

    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    classDirectories.setFrom(
        fileTree(layout.buildDirectory) {
            include("**/classes/kotlin/main/**/*.class")
            exclude(
                // Moshi KSP-generated JSON adapters — machine-generated code with no hand-written branches
                "**/*JsonAdapter.class",
                // ChiakiServiceImpl.fetchWatchOrder runs inside withContext(Dispatchers.IO) with a
                // real OkHttpClient; its suspension branches cannot be exercised in unit tests
                "**/ChiakiServiceImpl\$fetchWatchOrder*.class"
            )
        }
    )
}
