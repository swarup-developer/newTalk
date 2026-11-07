plugins {
    id("newtalk.android.library")
}

android {
    namespace = "com.google.android.accessibility.brailleime.analytics"
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":braillecommon"))
    implementation(project(":brailleinterfaces"))
}
