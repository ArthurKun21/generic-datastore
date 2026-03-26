plugins {
    id("generic-datastore.kmp-library")
    id("generic-datastore.maven-publish")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "io.github.arthurkun.generic.datastore"
        compileSdk = libs.versions.compile.sdk.get().toInt()
        minSdk = libs.versions.min.sdk.get().toInt()

        withJava()

        optimization {
            consumerKeepRules.file("consumer-rules.pro")
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.coroutines.core)
            api(libs.datastore.preferences.core)
            api(libs.datastore.core)
            api(libs.kotlinx.io.core)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.coroutines.test)
                implementation(libs.datastore.preferences)
                implementation(libs.junit4)
                implementation(libs.androidx.test.junit)
                implementation(libs.androidx.test.espresso)
            }
        }
        named("jvmTest") {
            dependencies {
                implementation(libs.junit5)
            }
        }
    }
}

mavenPublishing {
    coordinates("com.github.ArthurKun21", "generic-datastore", version.toString())

    pom {
        name.set("Generic Datastore Library")
        description.set("A generic datastore library for Kotlin Multiplatform.")
    }
}
