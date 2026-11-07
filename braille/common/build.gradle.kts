plugins {
    id("newtalk.android.library")
}

android {
    namespace = "com.google.android.accessibility.braille.common"
}

dependencies {
    implementation(project(":brailleinterfaces"))
    implementation(project(":brailletranslate"))
    implementation(project(":proguard"))
    implementation(project(":utils"))
    implementation(libs.gson)
}
