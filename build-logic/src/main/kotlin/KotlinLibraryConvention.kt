import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

class KotlinLibraryConvention : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        plugins.apply("kotlin")
        plugins.apply("java-library")
        plugins.apply("jacoco")

        java {
            sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
            targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
        }

        extensions.configure<KotlinProjectExtension> {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            }
        }

        dependencies {
            "testImplementation"(libs.junit5.api)
            "testRuntimeOnly"(libs.junit5.engine)
            "testRuntimeOnly"(libs.junit.platform.launcher)
            "testImplementation"(libs.truth)
        }

        tasks.withType(org.gradle.api.tasks.testing.Test::class.java) {
            useJUnitPlatform()
        }
    }
}
