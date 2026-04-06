import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import gd.buildlogic.AndroidConfig
import gd.buildlogic.configureCommonKotlinCompileOptions
import gd.buildlogic.libs
import gd.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("unused")
class KmpSampleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply(libs.pluginId("android-library"))
                apply("gd.compose")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                applyDefaultHierarchyTemplate()

                targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
                    compileSdk = AndroidConfig.COMPILE_SDK
                    minSdk = AndroidConfig.MIN_SDK
                }

                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }

            configureCommonKotlinCompileOptions()

            tasks.withType<Test>().configureEach {
                useJUnitPlatform()
            }
        }
    }
}
