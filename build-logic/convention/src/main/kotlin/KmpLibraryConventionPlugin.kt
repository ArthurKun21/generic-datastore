import generic.datastore.buildlogic.configureKmpLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            pluginManager.apply("com.android.kotlin.multiplatform.library")

            val kmpExtension =
                extensions.getByType(KotlinMultiplatformExtension::class.java)
            configureKmpLibrary(kmpExtension)
        }
    }
}
