pluginManagement {
    includeBuild("build-logic")
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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

val isJitPack = System.getenv("JITPACK") == "true"

rootProject.name = "GenericDataStore"
include(":generic-datastore")
include(":generic-datastore-compose")
if (!isJitPack) {
    include(":benchmark-generic-datastore")
    include(":benchmark-generic-datastore-app")
    include(":samples:preferenceAndroidApp")
    include(":samples:preferenceComposeApp")
    include(":samples:protoComposeApp")
}
