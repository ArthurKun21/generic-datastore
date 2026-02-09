plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.vanniktech.maven.publish)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    explicitApi()

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

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm("desktop") {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.coroutines.core)
            api(libs.datastore.preferences.core)
            api(libs.datastore.core)
            api(libs.kotlinx.io.core)
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

mavenPublishing {
    coordinates("com.github.ArthurKun21", "generic-datastore", version.toString())

    pom {
        name.set("Generic Datastore Library")
        description.set("A generic datastore library for Kotlin Multiplatform.")
        url.set("https://github.com/ArthurKun21/generic-datastore")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("ArthurKun21")
                name.set("Arthur")
                email.set("16458204+ArthurKun21@users.noreply.github.com")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/ArthurKun21/generic-datastore.git")
            developerConnection.set("scm:git:ssh://github.com/ArthurKun21/generic-datastore.git")
            url.set("https://github.com/ArthurKun21/generic-datastore")
        }
    }
}
