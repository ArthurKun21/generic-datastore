package generic.datastore.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

internal fun Project.configureKmpLibrary(
    kmpExtension: KotlinMultiplatformExtension,
) {
    kmpExtension.apply {
        explicitApi()

        applyDefaultHierarchyTemplate()

        targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
            compileSdk = AndroidConfig.COMPILE_SDK
            minSdk = AndroidConfig.MIN_SDK
        }

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
