plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data:api"))
    implementation(project(":data:local"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
    implementation(libs.retrofit)

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
