plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}

val coverageModules = listOf(
    ":module:domain",
    ":module:remote-data-source-retrofit",
    ":module:repository",
    ":module:local-data-source-room",
    ":module:use-case",
)

tasks.register("jacocoCoverageCheck") {
    group = "verification"
    description = "Runs Jacoco branch-coverage verification (≥80%) across all covered modules."
    dependsOn(coverageModules.map { "$it:jacocoTestCoverageVerification" })
}
