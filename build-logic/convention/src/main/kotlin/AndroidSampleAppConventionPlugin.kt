import com.android.build.api.dsl.ApplicationExtension
import gd.buildlogic.configureAndroid
import gd.buildlogic.configureCommonKotlinCompileOptions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

@Suppress("unused")
class AndroidSampleAppConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<ApplicationExtension> {
                configureAndroid(this)

                defaultConfig {
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                buildFeatures {
                    compose = true
                }

                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }
            }

            configureCommonKotlinCompileOptions()
        }
    }
}
