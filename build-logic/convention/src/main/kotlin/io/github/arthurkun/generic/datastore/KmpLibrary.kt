package io.github.arthurkun.generic.datastore

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

internal fun Project.configureKmpLibrary(
    kmpExtension: KotlinMultiplatformExtension,
) {
    kmpExtension.apply {
        explicitApi()

        applyDefaultHierarchyTemplate()

        iosX64()
        iosArm64()
        iosSimulatorArm64()

        jvm()

        compilerOptions {
            freeCompilerArgs.addAll(
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xexpect-actual-classes",
            )
        }
    }

    tasks.withType(Test::class.java).configureEach {
        useJUnitPlatform()
    }

    tasks.withType(KotlinNativeCompile::class.java).configureEach {
        compilerOptions {
            optIn.add("kotlinx.cinterop.ExperimentalForeignApi")
        }
    }
}
