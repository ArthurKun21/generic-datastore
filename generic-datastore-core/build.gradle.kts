plugins {
    id("gd.kmp.library")
    id("gd.kmp.library.test")
    id("gd.maven.publish")
}

kotlin {
    android {
        namespace = "io.github.arthurkun.generic.datastore.core"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.coroutines.core)
            implementation(libs.okio)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

mavenPublishing {
    coordinates(
        groupId = "com.github.ArthurKun21",
        artifactId = "generic-datastore-core",
        version = version.toString(),
    )

    pom {
        name.set("Generic Datastore Core")
        description.set("Core functionality for Generic Datastore Library.")
    }
}
