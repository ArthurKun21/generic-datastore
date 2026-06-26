plugins {
    id("gd.kmp.sample")
    alias(libs.plugins.wire)
}

kotlin {
    android {
        namespace = "io.github.arthurkun.generic.datastore.proto.app"
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":generic-datastore-proto"))
                implementation(project(":generic-datastore-compose"))
                implementation(libs.bundles.compose)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.wire.runtime)
                implementation(libs.filekit.dialogs.compose)
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

        nativeDistributions {
            packageName = "generic-datastore-proto-sample"

            linux {
                modules("jdk.security.auth")
            }
        }
    }
}
