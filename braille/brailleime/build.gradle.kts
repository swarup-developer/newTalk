plugins {
    id("newtalk.android.library")
}

android {
    namespace = "com.google.android.accessibility.brailleime"

    sourceSets {
        named("test") {
            java.directories.clear()
            kotlin.directories.clear()
        }
        named("testPhone") {
            java.directories.add("src/test/java")
        }
    }
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":braillecommon"))
    implementation(project(":brailleinterfaces"))
    implementation(project(":brailleimeanalytics"))
    implementation(project(":brailletranslate"))
    testImplementation(libs.junit)
}
