import org.gradle.jvm.tasks.Jar
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("gd.kmp.sample")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "io.github.arthurkun.generic.datastore.compose.app"

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

    jvm("desktop") {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
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

        val desktopMain by getting {
            dependencies {
                implementation(libs.coroutines.swing)
                implementation(compose.desktop.currentOs)
            }
        }

        val jvmCommon by creating {
            dependsOn(commonMain)
        }

        androidMain.dependsOn(jvmCommon)
        desktopMain.dependsOn(jvmCommon)
    }
}

compose.desktop {
    application {
        mainClass = "io.github.arthurkun.generic.datastore.compose.app.MainKt"

        buildTypes {
            release {
                proguard {
                    configurationFiles.from(project.file("proguard-rules.pro"))
                }
            }
        }

        nativeDistributions {
            modules("jdk.unsupported")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "generic-datastore-sample"
            packageVersion = "1.0.0"
        }
    }
}

tasks.withType<Jar>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    exclude("META-INF/AL2.0")
    exclude("META-INF/LGPL2.1")

    exclude("META-INF/MANIFEST.MF")
}
