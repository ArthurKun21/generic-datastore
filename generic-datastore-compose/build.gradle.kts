plugins {
    id("gd.kmp.library")
    id("gd.kmp.library.test")
    id("gd.maven.publish")
    alias(libs.plugins.compose.compiler)
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
