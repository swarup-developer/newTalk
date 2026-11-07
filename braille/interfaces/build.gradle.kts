plugins {
    id("newtalk.android.library")
}

android {
    namespace = "com.google.android.accessibility.braille.interfaces"
}

dependencies {
    implementation(project(":utils"))
}
