plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    implementation(libs.spotless.gradle.plugin)
    compileOnly(libs.vanniktech.maven.publish.plugin)
}

gradlePlugin {
    plugins {
        register("spotless") {
            id = "gd.spotless"
            implementationClass = "SpotlessConventionPlugin"
        }
        register("kmpLibrary") {
            id = "gd.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("mavenPublish") {
            id = "gd.maven.publish"
            implementationClass = "MavenPublishConventionPlugin"
        }
        register("compose") {
            id = "gd.compose"
            implementationClass = "ComposeConventionPlugin"
        }
        register("kmpSample") {
            id = "gd.kmp.sample"
            implementationClass = "KmpSampleConventionPlugin"
        }
        register("androidSampleApp") {
            id = "gd.android.sample"
            implementationClass = "AndroidSampleAppConventionPlugin"
        }
    }
}
