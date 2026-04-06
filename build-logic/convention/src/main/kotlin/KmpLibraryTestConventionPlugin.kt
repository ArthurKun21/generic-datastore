import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import gd.buildlogic.library
import gd.buildlogic.libs
import gd.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.pluginId("kotlin-multiplatform"))
                apply(libs.pluginId("android-library"))
            }
            extensions.configure<KotlinMultiplatformExtension> {
                applyDefaultHierarchyTemplate()

                targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
                    withDeviceTestBuilder {
                        sourceSetTreeName = "test"
                    }.configure {
                        instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    }
                }

                jvm()

                sourceSets.apply {
                    commonTest.dependencies {
                        implementation(libs.library("kotlin-test"))
                        implementation(libs.library("coroutines-test"))
                    }

                    getByName("androidDeviceTest") {
                        dependencies {
                            implementation(libs.library("kotlin-test"))
                            implementation(libs.library("coroutines-test"))
                            implementation(libs.library("datastore-preferences"))
                            implementation(libs.library("junit4"))
                            implementation(libs.library("androidx-test-junit"))
                            implementation(libs.library("androidx-test-espresso"))
                        }
                    }

                    jvmTest.dependencies {
                        implementation(libs.library("junit5"))
                    }
                }
            }
        }
    }
}
