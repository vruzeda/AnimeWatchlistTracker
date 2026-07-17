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

tasks.withType<Test>().configureEach {
    extensions.configure(JacocoTaskExtension::class) {
        // Robolectric's sandbox classloader strips source locations; without this,
        // classes exercised only through Robolectric tests report zero coverage.
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

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
    val expectedClassDir = androidClassDir.get().asFile.absolutePath
    doFirst {
        check(!(this as JacocoCoverageVerification).classDirectories.asFileTree.isEmpty) {
            "jacocoTestCoverageVerification resolved zero class files under " +
                "$expectedClassDir — the AGP intermediates layout has changed and " +
                "coverage would silently pass with nothing measured"
        }
    }
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
