import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.reload.gradle.ComposeHotRun
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    kotlin("multiplatform")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.hot.reload)
}

kotlin {
    androidTarget()

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(project(":library"))
            implementation(libs.datastore.preferences)
            implementation(libs.lifecycle.multiplatform.viewmodel.compose)

            implementation(libs.bundles.compose)
            implementation(libs.kotlinx.serialization.json)
        }

        androidMain.dependencies {
            implementation(libs.appcompat)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.activity.compose)

            implementation(libs.compose.material.icons.core)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

android {
    namespace = "io.github.arthurkun.generic.datastore.app"
    compileSdk = libs.versions.compile.sdk.get().toInt()

    defaultConfig {
        applicationId = "io.github.arthurkun.generic.datastore.app"
        minSdk = libs.versions.min.sdk.get().toInt()
        targetSdk = libs.versions.target.sdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

tasks {
    withType<KotlinCompile> {
        compilerOptions.freeCompilerArgs.addAll(
            "-opt-in=kotlin.time.ExperimentalTime",
        )
    }
}

//dependencies {
//    testImplementation(libs.junit4)
//    androidTestImplementation(libs.androidx.test.junit)
//    androidTestImplementation(libs.androidx.test.espresso)
//    androidTestImplementation(libs.compose.multiplatform.ui.test.junit4)
//    debugImplementation(libs.compose.multiplatform.ui.tooling)
//    debugImplementation(libs.compose.ui.test.manifest)
//    debugImplementation(libs.leak.canary)
//}

compose.desktop {
    application {
        mainClass = "arthurkun.datastore.desktop.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "arthurkun.datastore.desktop"
            packageVersion = "1.0.0"
        }
    }
}

tasks.withType<ComposeHotRun>().configureEach {
    mainClass.set("arthurkun.datastore.desktop.MainKt")
}

configurations {
    getByName("desktopMainApi").exclude(
        group = "org.jetbrains.kotlinx",
        module = "kotlinx-coroutines-android",
    )
}
