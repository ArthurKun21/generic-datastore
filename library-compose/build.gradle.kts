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

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":library")) // Core library dependency
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

    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        )
    }
}

android {
    namespace = "io.github.arthurkun.generic.datastore.compose"
    compileSdk = libs.versions.compile.sdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.min.sdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
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
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

version = "1.0.0"

publishing {
    publications {
        withType<MavenPublication> {
            groupId = "com.github.arthurkun21"
            artifactId = "generic-datastore-compose"
            version = project.version.toString()

            pom {
                name.set("Generic Datastore Compose Extensions")
                description.set("Jetpack Compose extensions for Generic Datastore Library.")
                url.set("https://github.com/arthurkun21/generic-datastore")
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
                    connection.set("scm:git:git://github.com/arthurkun21/generic-datastore.git")
                    developerConnection.set("scm:git:ssh://github.com/arthurkun21/generic-datastore.git")
                    url.set("https://github.com/arthurkun21/generic-datastore")
                }
            }
        }
    }
}
