import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    `maven-publish`
    id("com.android.library")
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
        publishLibraryVariants("release")
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
//    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
//        target.binaries.framework {
//            baseName = project.name
//            isStatic = true
//        }
//    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.bundles.datastore)
                implementation(libs.bundles.library.compose)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.junit4)
                implementation(libs.coroutines.test)
            }
        }

        val androidMain by getting
        val androidInstrumentedTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.junit4)
                implementation(libs.coroutines.test)
                implementation(libs.androidx.test.junit)
                implementation(libs.androidx.test.espresso)
            }
        }

        val desktopMain by getting
        val desktopTest by getting {
            dependencies {
                // JVM-specific test dependencies
                implementation(libs.junit5) // For JVM tests
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        )
    }
}

android {
    namespace = "io.github.arthurkun.generic.datastore"
    compileSdk = libs.versions.compile.sdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.min.sdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro") // Restored for consumers of the library
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    sourceSets {
        getByName("androidTest") {
            // Android test source set
            java.srcDir("src/androidInstrumentedTest/kotlin")
        }
    }
}

version = "1.0.0"

publishing {
    publications {
        // Remove the old Android-specific publication
        // create<MavenPublication>("release") { ... }

        // Publications will be created automatically for each target by the KMP plugin.
        // You might need to configure them further, e.g., for Maven Central.
        // Example:
        // withType<MavenPublication> {
        //     groupId = "io.github.arthurkun"
        //     artifactId = "generic-datastore-${project.name.toLowerCase()}" // Or a fixed artifactId if preferred
        //     version = project.version.toString()

        //     pom {
        //         name.set("Generic Datastore Library")
        //         description.set("A generic datastore library for Kotlin Multiplatform.")
        //         url.set("https://github.com/arthurkun/generic-datastore")
        //         licenses {
        //             license {
        //                 name.set("The Apache License, Version 2.0")
        //                 url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        //             }
        //         }
        //         developers {
        //             developer {
        //                 id.set("arthurkun")
        //                 name.set("Arthur Kun")
        //                 email.set("your-email@example.com")
        //             }
        //         }
        //         scm {
        //             connection.set("scm:git:git://github.com/arthurkun/generic-datastore.git")
        //             developerConnection.set("scm:git:ssh://github.com/arthurkun/generic-datastore.git")
        //             url.set("https://github.com/arthurkun/generic-datastore")
        //         }
        //     }
        // }
    }
}