plugins {
    id("gd.android.sample")
}

android {
    namespace = "io.github.arthurkun.generic.datastore.benchmark.app"

    defaultConfig {
        applicationId = "io.github.arthurkun.generic.datastore.benchmark.app"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    implementation(project(":generic-datastore"))
    implementation(project(":generic-datastore-compose"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.bundles.compose)

    debugImplementation(libs.compose.ui.tooling)
}