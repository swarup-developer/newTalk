plugins {
    id("newtalk.android.library")
}

android {
    namespace = "com.google.android.accessibility.scripting.quickjs"
    ndkVersion = libs.versions.ndk.get()
    defaultConfig {
        externalNativeBuild {
            cmake {
                targets.add("backtalkquickjs")
                arguments.add("-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON")
            }
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}
