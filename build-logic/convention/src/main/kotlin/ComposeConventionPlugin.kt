import generic.datastore.buildlogic.libs
import generic.datastore.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.pluginId("compose-compiler"))
                apply(libs.pluginId("jetbrains-compose"))
            }
        }
    }
}
