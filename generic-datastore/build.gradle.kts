plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.vanniktech.maven.publish)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    explicitApi()

    // Workaround for https://youtrack.jetbrains.com/projects/KTIJ/issues/KTIJ-34430/Incorrect-resolution-of-platform-specific-APIs-in-commonMain-in-a-KMP-with-the-JVM-target-and-the-pluggable-target
    // issue with kotlin serialization with android jvm target in Android studio.
    // https://dev.to/rsicarelli/the-hidden-cost-of-default-hierarchy-templates-in-kotlin-multiplatform-256a
    // applyDefaultHierarchyTemplate()
    applyHierarchyTemplate {
        common {
            withAndroidTarget()
            withJvm()
            withCompilations { compilation ->
                compilation.target.platformType == org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.androidJvm
            }
        }
    }

    androidLibrary {
        namespace = "io.github.arthurkun.generic.datastore"
        compileSdk = libs.versions.compile.sdk.get().toInt()
        minSdk = libs.versions.min.sdk.get().toInt()

        withJava()

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
            implementation(libs.datastore.preferences.core)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.coroutines.test)
                implementation(libs.datastore.preferences)
                implementation(libs.junit4)
                implementation(libs.androidx.test.junit)
                implementation(libs.androidx.test.espresso)
            }
        }
        named("desktopTest") {
            dependencies {
                implementation(libs.junit5)
            }
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
