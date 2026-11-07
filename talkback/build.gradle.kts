import java.util.Properties

plugins {
    id("newtalk.android.library")
    id("newtalk.android.compose")
}

apply(from = "$rootDir/version.gradle.kts")

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}
val geminiApiKey = localProperties.getProperty("gemini.api.key", "")
val geminiModel = localProperties.getProperty("gemini.model", "gemini-3.5-flash")

val testBuild = project.hasProperty("testBuild")
val talkbackAppId = if (testBuild) "io.github.swarupdeveloper.newtalk.test" else "io.github.swarupdeveloper.newtalk"
val commitHash = (rootProject.extra["gitCommitHash"] as? String) ?: ""
val commitCount = (rootProject.extra["gitCommitCount"] as? Int) ?: 1
val bundledOcr = project.hasProperty("bundledOcr")

android {
    namespace = "com.google.android.accessibility.talkback"
    defaultConfig {
        buildConfigField("String", "APPLICATION_ID", "\"$talkbackAppId\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
        buildConfigField("String", "GEMINI_MODEL", "\"$geminiModel\"")
        buildConfigField("String", "COMMIT_HASH", "\"$commitHash\"")
        buildConfigField("int", "COMMIT_COUNT", commitCount.toString())
        buildConfigField("boolean", "BUNDLED_OCR", bundledOcr.toString())
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    buildFeatures {
        aidl = true
    }
}

dependencies {
    compileOnly(libs.mlkit.text.recognition)
    testImplementation(libs.mlkit.text.recognition)

    implementation(project(":proguard"))
    implementation(project(":utils"))
    implementation(project(":brailleime"))
    "phoneImplementation"(project(":braillecommon"))
    implementation(project(":brailleinterfaces"))
    implementation(project(":brailledisplay"))
    implementation(project(":materialflags"))
    implementation(project(":materialpreference"))
    "wearImplementation"(project(":materialpreferencecompose"))
    "wearImplementation"(project(":materialtheme"))
    "phoneImplementation"(project(":quickjs"))

    // Wear libraries
    implementation(libs.androidx.wear)
    implementation(libs.androidx.wear.remote.interactions)
    implementation(libs.wearable.support)
    implementation(libs.zxing.core)

    // Compose
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.runtime)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.compose.ui)
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.material3)
    implementation(libs.compose.material.icons.core)

    // Services & utilities
    implementation(libs.litertlm)
    implementation(libs.volley)
    implementation(libs.gson)
    implementation(libs.protobuf.javalite)
    implementation(libs.flexbox)
    implementation(libs.commons.text)

    testImplementation(libs.junit)
    testImplementation(libs.json)
    testImplementation(libs.robolectric)
}
