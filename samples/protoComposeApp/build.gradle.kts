import org.gradle.jvm.tasks.Jar
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("gd.kmp.sample")
    alias(libs.plugins.wire)
}

kotlin {
    android {
        namespace = "io.github.arthurkun.generic.datastore.proto.app"
    }
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":generic-datastore"))
                implementation(project(":generic-datastore-compose"))
                implementation(libs.bundles.compose)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.wire.runtime)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.coroutines.swing)
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

wire {
    kotlin {
        javaInterop = true
    }
    sourcePath {
        srcDir("src/commonMain/proto")
    }
}

compose.desktop {
    application {
        mainClass = "io.github.arthurkun.generic.datastore.proto.app.MainKt"

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
            packageName = "generic-datastore-proto-sample"
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
