plugins {
    id("newtalk.android.library")
}

android {
    namespace = "com.google.android.accessibility.braille.translate"
    ndkVersion = libs.versions.ndk.get()
    externalNativeBuild {
        ndkBuild {
            path = file("src/phone/jni/Android.mk")
        }
    }
}

dependencies {
    implementation(project(":brailleinterfaces"))
    implementation(project(":proguard"))
    implementation(project(":utils"))
}

val createTranslationTablesZip = tasks.register<Zip>("createTranslationTablesZip") {
    archiveFileName.set("translationtables.zip")
    destinationDirectory.set(layout.projectDirectory.dir("src/phone/res/raw"))
    from("src/phone/tables") {
        into("liblouis/tables")
    }
}

tasks.named("preBuild") {
    dependsOn(createTranslationTablesZip)
}
