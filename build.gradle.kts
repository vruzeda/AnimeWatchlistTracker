import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}

subprojects {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
        }
    }

    pluginManager.withPlugin("jacoco") {
        afterEvaluate {
            val verificationTask = tasks.findByName("jacocoTestCoverageVerification")
                as? JacocoCoverageVerification ?: return@afterEvaluate
            verificationTask.dependsOn(tasks.withType<Test>())
            verificationTask.violationRules {
                rule {
                    limit {
                        counter = "BRANCH"
                        value = "COVEREDRATIO"
                        minimum = 0.80.toBigDecimal()
                    }
                }
            }
        }
    }
}
