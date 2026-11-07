package newtalk.buildlogic

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

fun Project.configureCommonAndroid(extension: CommonExtension) {
    val testBuild = project.hasProperty("testBuild")
    val talkbackApplicationId = if (testBuild) "io.github.swarupdeveloper.newtalk.test" else "io.github.swarupdeveloper.newtalk"

    extension.compileSdk = 37

    extension.defaultConfig.minSdk = 26
    extension.defaultConfig.ndk.abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86_64"))
    extension.defaultConfig.buildConfigField("String", "APPLICATION_ID", "\"$talkbackApplicationId\"")
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
