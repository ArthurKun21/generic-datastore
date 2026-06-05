plugins {
    id("gd.kmp.library")
    id("gd.kmp.library.test")
    id("gd.maven.publish")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "io.github.arthurkun.generic.datastore.preferences"

        @Suppress("UnstableApiUsage")
        optimization {
            consumerKeepRules.file("consumer-rules.pro")
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":generic-datastore-core"))
            implementation(libs.coroutines.core)
            compileOnly(libs.datastore.preferences.core)
            api(libs.okio)
            implementation(libs.kotlinx.io.core)
            implementation(libs.kotlinx.serialization.json)
        }

        androidMain.dependencies {
            api(libs.datastore.preferences)
        }

        jvmMain.dependencies {
            api(libs.datastore.preferences.core)
        }

        iosMain.dependencies {
            api(libs.datastore.preferences.core)
        }
    }
}

mavenPublishing {
    coordinates(
        groupId = "com.github.ArthurKun21",
        artifactId = "generic-datastore-preferences",
        version = version.toString(),
    )

    pom {
        name.set("Generic Datastore Preferences")
        description.set("Preferences support for Generic Datastore Library.")
    }
}
