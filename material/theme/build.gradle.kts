plugins {
    id("newtalk.android.library")
    id("newtalk.android.compose")
}

android {
    namespace = "com.google.android.accessibility.material.theme"
}

dependencies {
    implementation(project(":proguard"))
    "wearImplementation"(libs.compose.runtime)
    "wearImplementation"(libs.wear.compose.material3)
}
