plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.vanniktech.maven.publish)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    explicitApi()

    applyDefaultHierarchyTemplate()

    androidLibrary {
        namespace = "io.github.arthurkun.generic.datastore.compose"
        compileSdk = libs.versions.compile.sdk.get().toInt()
        minSdk = libs.versions.min.sdk.get().toInt()

        optimization {
            consumerKeepRules.file("consumer-rules.pro")
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    jvm("desktop") {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":generic-datastore")) // Core library dependency
            implementation(libs.bundles.library.compose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
        }
    }

    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        )
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            groupId = "com.github.arthurkun"
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
