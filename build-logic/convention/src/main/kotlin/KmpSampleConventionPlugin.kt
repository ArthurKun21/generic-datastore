import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import gd.buildlogic.AndroidConfig
import gd.buildlogic.configureCommonKotlinCompileOptions
import gd.buildlogic.library
import gd.buildlogic.libs
import gd.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.compose.desktop.DesktopExtension
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("unused")
class KmpSampleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.pluginId("kotlin-multiplatform"))
                apply(libs.pluginId("android-library"))
                apply(libs.pluginId("kotlin-serialization"))
                apply("gd.compose")
                apply("gd.spotless")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                applyDefaultHierarchyTemplate()

                targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
                    compileSdk = AndroidConfig.COMPILE_SDK
                    minSdk = AndroidConfig.MIN_SDK
                }

                jvm("desktop")

                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }

                sourceSets.named("desktopMain") {
                    dependencies {
                        implementation(libs.library("coroutines-swing"))
                        @Suppress("DEPRECATION")
                        implementation(ComposePlugin.DesktopDependencies.currentOs)
                    }
                }
            }

            extensions.configure<ComposeExtension> {
                extensions.configure<DesktopExtension> {
                    application {
                        buildTypes {
                            release {
                                proguard {
                                    configurationFiles.from(project.file("proguard-rules.pro"))
                                }
                            }
                        }

                        nativeDistributions {
                            modules("jdk.unsupported")
                            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                            packageVersion = "1.0.0"
                        }
                    }
                }
            }

            configureCommonKotlinCompileOptions()

            tasks.withType<Test>().configureEach {
                useJUnitPlatform()
            }

            tasks.withType<Jar>().configureEach {
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE

                exclude("META-INF/AL2.0")
                exclude("META-INF/LGPL2.1")
                exclude("META-INF/MANIFEST.MF")
            }
        }
    }
}
