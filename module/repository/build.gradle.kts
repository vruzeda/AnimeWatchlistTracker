plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
    jacoco
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":module:remote-data-source"))
    implementation(project(":module:local-data-source"))
    implementation(project(":module:domain"))
    implementation(project(":module:scheduler"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)

    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

tasks.withType<Test> {
    useJUnitPlatform()
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
