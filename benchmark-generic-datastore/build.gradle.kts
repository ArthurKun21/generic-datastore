import gd.buildlogic.AndroidConfig

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.androidx.baselineprofile)
    id("gd.spotless")
}

android {
    namespace = "io.github.arthurkun.generic.datastore.benchmark"
    compileSdk = AndroidConfig.COMPILE_SDK
    targetProjectPath = ":benchmark-generic-datastore-app"

    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK
        targetSdk = AndroidConfig.TARGET_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
    }

    compileOptions {
        sourceCompatibility = AndroidConfig.JavaVersion
        targetCompatibility = AndroidConfig.JavaVersion
    }

    testOptions {
        managedDevices {
            localDevices {
                create("pixel6api31") {
                    device = "Pixel 6"
                    apiLevel = 31
                    systemImageSource = "aosp"
                }
            }
        }
    }

    experimentalProperties["android.experimental.self-instrumenting"] = true
}

baselineProfile {
    managedDevices += "pixel6api31"
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.test.junit)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.junit4)
}
