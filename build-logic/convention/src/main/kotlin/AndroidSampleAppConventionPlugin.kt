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
            with(pluginManager) {
                apply("com.android.application")
                apply("gd.compose")
                apply("gd.spotless")
            }

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
