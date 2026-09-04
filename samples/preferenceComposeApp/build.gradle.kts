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
        commonMain {
            dependencies {
                implementation(project(":generic-datastore-preferences"))
                implementation(project(":generic-datastore-compose"))
                implementation(libs.bundles.compose)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.activity.compose)
            }
        }
        jvmMain.dependencies {
            implementation(libs.coroutines.swing)
            implementation(compose.desktop.currentOs)
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.github.arthurkun.generic.datastore.compose.app.MainKt"

        buildTypes {
            release {
                proguard {
                    isEnabled = false
                    configurationFiles.from("proguard-rules.pro")
                }
            }
        }

        nativeDistributions {
            packageName = "generic-datastore-sample"
        }
    }
}
