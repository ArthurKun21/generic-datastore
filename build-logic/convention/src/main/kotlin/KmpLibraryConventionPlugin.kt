import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import gd.buildlogic.AndroidConfig
import gd.buildlogic.configureCommonKotlinCompileOptions
import gd.buildlogic.configureKmpLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("unused")
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.kotlin.multiplatform.library")
                apply("gd.spotless")
            }

            extensions.configure<KotlinMultiplatformExtension> {
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


            val kmpExtension =
                extensions.getByType(KotlinMultiplatformExtension::class.java)
            configureKmpLibrary(kmpExtension)
            configureCommonKotlinCompileOptions()
        }
    }
}
