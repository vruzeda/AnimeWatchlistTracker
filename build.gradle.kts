plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}

val jacocoBranchRatio = 0.80.toBigDecimal()

tasks.register("jacocoCoverageCheck") {
    group = "verification"
    description = "Runs Jacoco branch-coverage verification across all jacoco-enabled modules."
}

subprojects {
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
                        minimum = jacocoBranchRatio
                    }
                }
            }
            rootProject.tasks.named("jacocoCoverageCheck") {
                dependsOn(verificationTask)
            }
        }
    }
}
