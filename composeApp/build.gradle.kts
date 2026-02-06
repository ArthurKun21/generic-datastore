import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    androidLibrary {
        namespace = "io.github.arthurkun.generic.datastore.compose.app"
        compileSdk = libs.versions.compile.sdk.get().toInt()
        minSdk = libs.versions.min.sdk.get().toInt()

        withJava()

        optimization {
            consumerKeepRules.file("consumer-rules.pro")
        }

        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    jvm("desktop") {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":generic-datastore"))
            implementation(project(":generic-datastore-compose"))
            implementation(libs.bundles.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.datastore.preferences)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.activity.compose)
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.coroutines.swing)
                implementation(compose.desktop.currentOs)
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xexpect-actual-classes",
        )
    }
}

compose.desktop {
    application {
        mainClass = "io.github.arthurkun.generic.datastore.compose.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "generic-datastore-sample"
            packageVersion = "1.0.0"
        }
    }
}
