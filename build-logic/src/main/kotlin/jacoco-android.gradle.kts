import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    jacoco
}

val jacocoAndroid = extensions.create("jacocoAndroid", JacocoAndroidExtension::class.java)

val defaultExcludes = listOf(
    "**/R.class", "**/R\$*.class", "**/BuildConfig.*", "**/Manifest*.*"
)

val androidClassDir = layout.buildDirectory.dir(
    "intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes"
)

val unitTestExecutionData = layout.buildDirectory.file(
    "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
)

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn("testDebugUnitTest")
    classDirectories.setFrom(
        provider {
            fileTree(androidClassDir) {
                exclude(defaultExcludes + jacocoAndroid.excludes.get())
            }
        }
    )
    executionData.setFrom(unitTestExecutionData)
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    classDirectories.setFrom(
        provider {
            fileTree(androidClassDir) {
                exclude(defaultExcludes + jacocoAndroid.excludes.get())
            }
        }
    )
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(unitTestExecutionData)
}
