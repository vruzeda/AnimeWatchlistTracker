plugins {
    id("kotlin-library")
}

dependencies {
    implementation(project(":module:domain"))
    implementation(libs.kotlinx.coroutines.core)
}
