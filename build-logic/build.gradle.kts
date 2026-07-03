plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("kotlin-library") {
            id = "kotlin-library"
            implementationClass = "KotlinLibraryConvention"
        }
    }
}
