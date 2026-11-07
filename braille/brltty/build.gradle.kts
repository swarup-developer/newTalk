plugins {
    id("newtalk.android.library")
}

android {
    namespace = "com.google.android.accessibility.braille.brltty"
    ndkVersion = libs.versions.ndk.get()
    externalNativeBuild {
        ndkBuild {
            path = file("src/phone/jni/Android.mk")
        }
    }
}

dependencies {
    implementation(project(":brailleinterfaces"))
    implementation(project(":brailletranslate"))
    implementation(project(":braillecommon"))
    implementation(project(":proguard"))
    implementation(project(":utils"))
}

val createBrlttyKeyTablesZip = tasks.register<Zip>("createBrlttyKeyTablesZip") {
    archiveFileName.set("keytables.zip")
    destinationDirectory.set(layout.projectDirectory.dir("src/phone/res/raw"))
    from("src/phone/tables")
}

tasks.named("preBuild") {
    dependsOn(createBrlttyKeyTablesZip)
}
