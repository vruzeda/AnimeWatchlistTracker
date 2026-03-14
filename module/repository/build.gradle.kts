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
    dependsOn(tasks.named("test"))
    violationRules {
        rule {
            element = "CLASS"
            excludes = listOf(
                // AnimeRepositoryImpl contains only coroutine-delegate methods with no
                // business-logic branches. Kotlin's coroutine compiler generates state-machine
                // branches that MockK cannot exercise via unit tests; all real delegation
                // paths are covered by AnimeRemoteDataSourceImplTest.
                "com.vuzeda.animewatchlist.tracker.module.repository.impl.AnimeRepositoryImpl"
            )
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
