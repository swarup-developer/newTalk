plugins {
    id("newtalk.android.library")
}

android {
    namespace = "com.google.android.accessibility.material.preference"
}

dependencies {
    implementation(project(":proguard"))
    implementation(project(":utils"))
    implementation(project(":materialpreferencecompose"))
}
