pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "newtalk"

include(":talkback")
include(":utils")
include(":proguard")

// Braille modules
include(":braillecommon"); project(":braillecommon").projectDir = file("braille/common")
include(":brailleinterfaces"); project(":brailleinterfaces").projectDir = file("braille/interfaces")
include(":brailleime"); project(":brailleime").projectDir = file("braille/brailleime")
include(":brailleimeanalytics"); project(":brailleimeanalytics").projectDir = file("braille/brailleimeanalytics")
include(":brailletranslate"); project(":brailletranslate").projectDir = file("braille/translate")
include(":brailledisplay"); project(":brailledisplay").projectDir = file("braille/brailledisplay")
include(":brailledisplayanalytics"); project(":brailledisplayanalytics").projectDir = file("braille/brailledisplayanalytics")
include(":braillebrltty"); project(":braillebrltty").projectDir = file("braille/brltty")

// Scripting
include(":quickjs"); project(":quickjs").projectDir = file("scripting/quickjs")

// Material
include(":materialflags"); project(":materialflags").projectDir = file("material/flags")
include(":materialtheme"); project(":materialtheme").projectDir = file("material/theme")
include(":materialpreference"); project(":materialpreference").projectDir = file("material/preference")
include(":materialpreferencecompose"); project(":materialpreferencecompose").projectDir = file("material/preference/compose")
