plugins {
    id("gd.kmp.sample")
}

kotlin {
    android {
        namespace = "io.github.arthurkun.generic.datastore.compose.app"

        @Suppress("UnstableApiUsage")
        optimization {
            consumerKeepRules.file("consumer-rules.pro")
        }

        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":generic-datastore"))
                implementation(project(":generic-datastore-compose"))
                implementation(libs.bundles.compose)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.activity.compose)
            }
        }

        val jvmCommon by creating {
            dependsOn(commonMain)
        }

        androidMain.dependsOn(jvmCommon)
        named("desktopMain") {
            dependsOn(jvmCommon)
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.github.arthurkun.generic.datastore.compose.app.MainKt"

        nativeDistributions {
            packageName = "generic-datastore-sample"
        }
    }
}
