import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import gd.buildlogic.AndroidConfig
import gd.buildlogic.configureCommonKotlinCompileOptions
import gd.buildlogic.configureKmpLibrary
import gd.buildlogic.libs
import gd.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("unused")
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureCommonKotlinCompileOptions()

            with(pluginManager) {
                apply(libs.pluginId("kotlin-multiplatform"))
                apply(libs.pluginId("android-library"))
                apply("gd.spotless")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                configureKmpLibrary(this)
                explicitApi()

                applyDefaultHierarchyTemplate()

                targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
                    compileSdk = AndroidConfig.COMPILE_SDK
                    minSdk = AndroidConfig.MIN_SDK
                }

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
        }
    }
}
