plugins {
    id("gd.kmp.library")
    id("gd.kmp.library.test")
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
