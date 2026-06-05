plugins {
    id("gd.kmp.library")
    id("gd.kmp.library.test")
    id("gd.maven.publish")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "io.github.arthurkun.generic.datastore.proto"

        @Suppress("UnstableApiUsage")
        optimization {
            consumerKeepRules.file("consumer-rules.pro")
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":generic-datastore-core"))
            implementation(libs.coroutines.core)
            compileOnly(libs.datastore.core)
            api(libs.datastore.core.okio)
            api(libs.okio)
            implementation(libs.kotlinx.io.core)
            implementation(libs.kotlinx.serialization.json)
        }

        androidMain.dependencies {
            api(libs.datastore.android.core)
        }

        jvmMain.dependencies {
            api(libs.datastore.core)
        }

        iosMain.dependencies {
            api(libs.datastore.core)
        }
    }
}

mavenPublishing {
    coordinates(
        groupId = "com.github.ArthurKun21",
        artifactId = "generic-datastore-proto",
        version = version.toString(),
    )

    pom {
        name.set("Generic Datastore Proto")
        description.set("Proto support for Generic Datastore Library.")
    }
}
