import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("newtalk.android.application")
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

apply(from = "version.gradle.kts")

val buildTimestamp = SimpleDateFormat("yyyy_MM_dd_HHmm").format(Date())
val bundledOcr = project.hasProperty("bundledOcr")

val testBuild = project.hasProperty("testBuild")
val talkbackAppId = if (testBuild) "io.github.swarupdeveloper.newtalk.test" else "io.github.swarupdeveloper.newtalk"
val versionCodeBase = extra["backtalkVersionCodeBase"] as Int
val commitCount = extra["gitCommitCount"] as Int
val vName = extra["talkbackVersionName"] as String

dependencies {
    implementation(project(":talkback"))
    "phoneImplementation"(if (bundledOcr) libs.mlkit.text.recognition.bundled else libs.mlkit.text.recognition)
    "wearImplementation"(libs.mlkit.text.recognition)
}

android {
    namespace = "com.android.talkback"

    defaultConfig {
        applicationId = talkbackAppId
        versionCode = versionCodeBase + commitCount
        versionName = "$vName-$buildTimestamp"
        targetSdk = 36

        if (testBuild) {
            resValue("string", "talkback_title", "newTalk Test")
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = false
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            excludes.addAll(listOf("LICENSE", "README"))
        }
        jniLibs {
            // On-device AI only runs on arm64-v8a (see OnDeviceAiSettings), so its 26 MB library
            // would never load on x86_64.
            excludes.add("lib/x86_64/liblitertlm_jni.so")
        }
    }

    lint {
        checkReleaseBuilds = false
        // Lint the library modules too, where most of the code is, in one report.
        checkDependencies = true
        // The issues that were already there when lint was added to CI, for the phone and the
        // watch. Only new ones fail the build. After accepting new ones on purpose with
        // ./gradlew updateLintBaseline, check that :lintPhoneDebug and :lintWearDebug both pass.
        baseline = file("lint-baseline.xml")
        abortOnError = true
    }

    productFlavors {
        getByName("wear") {
            versionNameSuffix = "-wear"
        }
    }
}
