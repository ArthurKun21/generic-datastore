import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

@Suppress("unused")
class SpotlessConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.diffplug.spotless")

            val libs = rootProject.extensions
                .getByType<VersionCatalogsExtension>()
                .named("libs")

            extensions.configure<SpotlessExtension> {
                kotlin {
                    target("**/*.kt", "**/*.kts")
                    targetExclude("**/build/**/*.kt")
                    ktlint(
                        libs.findLibrary("ktlint-core").get().get().version!!,
                    ).editorConfigOverride(
                        mapOf("ktlint_standard_annotation" to "disabled"),
                    )
                    trimTrailingWhitespace()
                    endWithNewline()
                }
                format("xml") {
                    target("**/*.xml")
                    trimTrailingWhitespace()
                    endWithNewline()
                }
            }
        }
    }
}
