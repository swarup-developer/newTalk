plugins {
    id("newtalk.android.library")
}

android {
    namespace = "com.google.android.accessibility.braille.brailledisplay.analytics"
}

dependencies {
    implementation(project(":braillecommon"))
}
