plugins {
    id("kotlin-library")
    jacoco
}

dependencies {
    implementation(project(":module:remote-data-source"))
    implementation(project(":module:local-data-source"))
    implementation(project(":module:domain"))
    implementation(project(":module:notification"))
    implementation(project(":module:scheduler"))

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
            // AnimeRepositoryImpl: coroutine-delegate methods only; Kotlin's state-machine
            // branches cannot be exercised by MockK unit tests.
            exclude("**/impl/AnimeRepositoryImpl*.class")
        }
    )
}
