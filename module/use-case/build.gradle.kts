plugins {
    id("kotlin-library")
    jacoco
}

dependencies {
    implementation(project(":module:repository"))
    implementation(project(":module:domain"))
    implementation(project(":module:notification"))
    implementation(project(":module:scheduler"))
    implementation(project(":module:remote-data-source-retrofit"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)

    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
