plugins {
    alias(libs.plugins.kotlin.multiplatform)
    `maven-publish`
    id("com.android.library")
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
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
        val commonMain by getting {
            dependencies {
                api(project(":generic-datastore")) // Core library dependency
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
        val desktopMain by getting
    }
}

android {
    namespace = "io.github.arthurkun.generic.datastore.compose"
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
                "proguard-rules.pro",
            )
        }
    }
    sourceSets {
        getByName("androidTest") {
            // Android test source set
            java.srcDir("src/androidInstrumentedTest/kotlin")
        }
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            groupId = "com.github.arthurkun"
            artifactId = "generic-datastore-compose"
            version = project.version.toString()

            pom {
                name.set("Generic Datastore Compose Extensions")
                description.set("Jetpack Compose extensions for Generic Datastore Library.")
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
