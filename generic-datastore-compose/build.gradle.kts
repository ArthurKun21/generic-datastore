plugins {
    id("generic-datastore.kmp-library")
    id("generic-datastore.maven-publish")
    alias(libs.plugins.compose.compiler)
}

kotlin {
    android {
        namespace = "io.github.arthurkun.generic.datastore.compose"
        compileSdk = libs.versions.compile.sdk.get().toInt()
        minSdk = libs.versions.min.sdk.get().toInt()

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
            api(project(":generic-datastore"))
            implementation(libs.compose.runtime)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.lifecycle.runtime.compose)
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
    coordinates("com.github.ArthurKun21", "generic-datastore-compose", version.toString())

    pom {
        name.set("Generic Datastore Compose Extensions")
        description.set("Jetpack Compose extensions for Generic Datastore Library.")
    }
}
