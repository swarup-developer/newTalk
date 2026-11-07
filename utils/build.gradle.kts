plugins {
    id("newtalk.android.library")
}

val testBuild = project.hasProperty("testBuild")
val talkbackAppId = if (testBuild) "io.github.swarupdeveloper.newtalk.test" else "io.github.swarupdeveloper.newtalk"

android {
    namespace = "com.google.android.accessibility.utils"
    defaultConfig {
        buildConfigField("String", "TALKBACK_APPLICATION_ID", "\"$talkbackAppId\"")
        buildConfigField("String", "IS_SYSTEM_PRELOAD", "\"False\"")
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(project(":proguard"))
    implementation(libs.gson)
    implementation(libs.protobuf.javalite)
    implementation(libs.flexbox)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
}
