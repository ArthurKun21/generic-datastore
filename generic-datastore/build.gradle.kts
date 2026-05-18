plugins {
    id("gd.kmp.library")
    id("gd.kmp.library.test")
    id("gd.maven.publish")
}

kotlin {
    android {
        namespace = "io.github.arthurkun.generic.datastore"

        withJava()

        @Suppress("UnstableApiUsage")
        optimization {
            consumerKeepRules.file("consumer-rules.pro")
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":generic-datastore-preferences"))
            api(project(":generic-datastore-proto"))
            api(libs.kotlinx.serialization.json)
        }
    }
}

mavenPublishing {
    coordinates(
        groupId = "com.github.ArthurKun21",
        artifactId = "generic-datastore",
        version = version.toString(),
    )

    pom {
        name.set("Generic Datastore Library")
        description.set("A generic datastore library for Kotlin Multiplatform.")
    }
}
