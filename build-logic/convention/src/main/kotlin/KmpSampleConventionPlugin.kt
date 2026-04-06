import generic.datastore.buildlogic.configureCommonKotlinCompileOptions
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
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")

            extensions.configure<KotlinMultiplatformExtension> {
                applyDefaultHierarchyTemplate()

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
