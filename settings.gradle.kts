pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Anime Watchlist Tracker"
include(":app")
include(":module:domain")
include(":module:local-data-source")
include(":module:local-data-source-room")
include(":module:remote-data-source")
include(":module:remote-data-source-retrofit")
include(":module:repository")
include(":module:use-case")
include(":module:design-system")
include(":module:ui")
include(":module:notification")
include(":module:notification-android")
include(":module:scheduler")
include(":module:scheduler-work")
include(":module:analytics")
include(":module:analytics-firebase")
include(":module:remote-data-source-firebase")
