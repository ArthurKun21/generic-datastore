import com.android.build.api.dsl.androidLibrary

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    `maven-publish`
    alias(libs.plugins.android.library)
}

kotlin {
    androidLibrary {
        namespace = "io.github.arthurkun.generic.datastore"
        compileSdk = libs.versions.compile.sdk.get().toInt()
        minSdk = libs.versions.min.sdk.get().toInt()

        optimization {
            consumerKeepRules.file("consumer-rules.pro")
        }

        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    jvm("desktop") {
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
        commonMain {
            dependencies {
                implementation(libs.datastore.preferences.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.junit4)
                implementation(libs.coroutines.test)
            }
        }
        androidMain {

        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.datastore.preferences)
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

//android {
//
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro",
//            )
//        }
//    }
//    sourceSets {
//        getByName("androidTest") {
//            // Android test source set
//            java.srcDir("src/androidInstrumentedTest/kotlin")
//        }
//    }
//    publishing {
//        singleVariant("release") {
//            withSourcesJar()
//            withJavadocJar()
//        }
//    }
//}

publishing {
    publications {
        withType<MavenPublication> {
            groupId = "com.github.arthurkun"
            artifactId = "generic-datastore"
            version = project.version.toString()

            pom {
                name.set("Generic Datastore Library")
                description.set("A generic datastore library for Kotlin Multiplatform.")
                url.set("https://github.com/arthurkun/generic-datastore")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("arthurkun")
                        name.set("Arthur")
                        email.set("16458204+ArthurKun21@users.noreply.github.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/arthurkun/generic-datastore.git")
                    developerConnection.set("scm:git:ssh://github.com/arthurkun/generic-datastore.git")
                    url.set("https://github.com/arthurkun/generic-datastore")
                }
            }
        }
    }
}
