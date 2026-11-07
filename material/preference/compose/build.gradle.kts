plugins {
    id("newtalk.android.library")
    id("newtalk.android.compose")
}

android {
    namespace = "com.google.android.accessibility.material.preference.compose"
}

dependencies {
    implementation(project(":proguard"))
    implementation(project(":materialflags"))
    "wearImplementation"(project(":materialtheme"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.wear.compose.material3)
}
