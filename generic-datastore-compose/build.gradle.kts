plugins {
    id("gd.kmp.library")
    id("gd.kmp.library.test")
    id("gd.maven.publish")
    id("gd.compose")
}

kotlin {
    android {
        namespace = "io.github.arthurkun.generic.datastore.compose"

        @Suppress("UnstableApiUsage")
        optimization {
            consumerKeepRules.file("consumer-rules.pro")
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":generic-datastore"))
            implementation(libs.compose.runtime)
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
    }
}

mavenPublishing {
    coordinates(
        groupId = "com.github.ArthurKun21",
        artifactId = "generic-datastore-compose",
        version = version.toString(),
    )

    pom {
        name.set("Generic Datastore Compose Extensions")
        description.set("Jetpack Compose extensions for Generic Datastore Library.")
    }
}
