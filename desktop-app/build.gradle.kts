import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    id("org.jetbrains.compose") version "1.7.1"
}

group = "io.github.arthurkun.generic.datastore"
version = "1.0.0"

dependencies {
    implementation(project(":library"))
    implementation(project(":library-compose"))

    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    implementation(libs.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)

    // Coroutines for Swing/Desktop
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:${libs.versions.coroutines.get()}")
}

tasks {
    withType<KotlinCompile> {
        compilerOptions.freeCompilerArgs.addAll(
            "-opt-in=kotlin.time.ExperimentalTime",
        )
    }
}

compose.desktop {
    application {
        mainClass = "io.github.arthurkun.generic.datastore.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "GenericDataStore-Desktop"
            packageVersion = "1.0.0"

            description = "Desktop sample application for GenericDataStore library"
            vendor = "ArthurKun21"

            windows {
                menuGroup = "GenericDataStore"
                upgradeUuid = "F9C78E1D-3B2A-4E5C-9D6F-A1B2C3D4E5F6"
            }

            macOS {
                bundleID = "io.github.arthurkun.generic.datastore.desktop"
            }

            linux {
                packageName = "generic-datastore-desktop"
                debMaintainer = "arthurkun21@github.com"
                menuGroup = "Office"
            }
        }
    }
}
