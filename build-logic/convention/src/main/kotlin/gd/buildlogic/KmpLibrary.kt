package gd.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

internal fun Project.configureAndroid(commonExtension: CommonExtension) {
    commonExtension.apply {
        compileSdk = AndroidConfig.COMPILE_SDK

        defaultConfig.apply {
            minSdk = AndroidConfig.MIN_SDK
        }

        compileOptions.apply {
            sourceCompatibility = AndroidConfig.JavaVersion
            targetCompatibility = AndroidConfig.JavaVersion
        }
    }
}

internal fun Project.configureCommonKotlinCompileOptions() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(AndroidConfig.JvmTarget)
            freeCompilerArgs.addAll(
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlin.time.ExperimentalTime",
            )
        }
    }
}

internal fun Project.configureKmpLibrary() {

    tasks.withType(Test::class.java).configureEach {
        useJUnitPlatform()
    }

    tasks.withType(KotlinNativeCompile::class.java).configureEach {
        compilerOptions {
            optIn.add("kotlinx.cinterop.ExperimentalForeignApi")
        }
    }
}
