plugins {
    id("newtalk.android.library")
}

android {
    namespace = "com.google.android.accessibility.material.flags"
}

dependencies {
    implementation(project(":proguard"))
}
