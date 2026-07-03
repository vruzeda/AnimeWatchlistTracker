import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

class KotlinLibraryConvention : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            pluginManager.apply("java-library")
            pluginManager.apply("jacoco")

            configure<org.gradle.api.plugins.JavaPluginExtension> {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }

            dependencies {
                "testImplementation"("org.junit.jupiter:junit-jupiter-api")
                "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine")
                "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
                "testImplementation"("com.google.truth:truth")
            }

            tasks.withType<Test> {
                useJUnitPlatform()
            }
        }
    }
}
