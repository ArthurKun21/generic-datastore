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
            id = "generic-datastore.spotless"
            implementationClass = "SpotlessConventionPlugin"
        }
        register("kmpLibrary") {
            id = "generic-datastore.kmp-library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("mavenPublish") {
            id = "generic-datastore.maven-publish"
            implementationClass = "MavenPublishConventionPlugin"
        }
        register("compose") {
            id = "gd.compose"
            implementationClass = "ComposeConventionPlugin"
        }
        register("kmpSample") {
            id = "generic-datastore.kmp-sample"
            implementationClass = "KmpSampleConventionPlugin"
        }
        register("androidSampleApp") {
            id = "generic-datastore.android-sample-app"
            implementationClass = "AndroidSampleAppConventionPlugin"
        }
    }
}
