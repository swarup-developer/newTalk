package newtalk.buildlogic

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import java.util.Properties

fun Project.configureCommonAndroid(extension: CommonExtension) {
    val testBuild = project.hasProperty("testBuild")
    val talkbackApplicationId = if (testBuild) "io.github.swarupdeveloper.newtalk.test" else "io.github.swarupdeveloper.newtalk"

    val versionProps = Properties().apply {
        val propsFile = rootProject.file("version.properties")
        if (propsFile.exists()) {
            propsFile.inputStream().use { load(it) }
        }
    }
    val githubRepo = (project.findProperty("githubRepo") as? String)
        ?: versionProps.getProperty("githubRepo", "swarup-developer/newTalk")

    extension.compileSdk = 37

    extension.defaultConfig.minSdk = 26
    extension.defaultConfig.ndk.abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86_64"))
    extension.defaultConfig.buildConfigField("String", "APPLICATION_ID", "\"$talkbackApplicationId\"")
    extension.defaultConfig.buildConfigField("String", "GITHUB_REPO", "\"$githubRepo\"")
    extension.defaultConfig.resValue("string", "app_id", talkbackApplicationId)
    extension.defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    extension.compileOptions.sourceCompatibility = JavaVersion.VERSION_17
    extension.compileOptions.targetCompatibility = JavaVersion.VERSION_17

    extension.flavorDimensions.add("target")
    extension.productFlavors.create("phone") {
        dimension = "target"
    }
    extension.productFlavors.create("wear") {
        dimension = "target"
    }

    extension.buildFeatures.buildConfig = true
    extension.buildFeatures.resValues = true
}

fun Project.configureCompose(extension: CommonExtension) {
    extension.buildFeatures.compose = true
}
