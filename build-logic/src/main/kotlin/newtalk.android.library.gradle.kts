import newtalk.buildlogic.configureCommonAndroid
import com.android.build.api.dsl.LibraryExtension

plugins {
    id("com.android.library")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

configure<LibraryExtension> {
    configureCommonAndroid(this)
}

dependencies {
    // Google common
    add("compileOnly", libs.findLibrary("mlkit-text-recognition").get())
    add("testImplementation", libs.findLibrary("mlkit-text-recognition").get())
    add("implementation", libs.findLibrary("material").get())

    // Support library
    add("implementation", libs.findLibrary("androidx-annotation").get())
    add("implementation", libs.findLibrary("androidx-appcompat").get())
    add("implementation", libs.findLibrary("androidx-collection").get())
    add("implementation", libs.findLibrary("androidx-core").get())
    add("implementation", libs.findLibrary("androidx-core-i18n").get())
    add("implementation", libs.findLibrary("androidx-fragment").get())
    add("implementation", libs.findLibrary("androidx-lifecycle-service").get())
    add("implementation", libs.findLibrary("androidx-savedstate").get())
    add("implementation", libs.findLibrary("androidx-localbroadcastmanager").get())
    add("implementation", libs.findLibrary("androidx-preference").get())
    add("implementation", libs.findLibrary("androidx-recyclerview").get())
    add("implementation", libs.findLibrary("androidx-viewpager2").get())

    // Guava and annotations
    add("implementation", libs.findLibrary("guava").get())
    add("implementation", libs.findLibrary("checker-qual").get())
    add("implementation", libs.findLibrary("errorprone-annotations").get())

    // Auto-value & annotations
    add("compileOnly", libs.findLibrary("auto-value-annotations").get())
    add("annotationProcessor", libs.findLibrary("auto-value").get())
    add("implementation", libs.findLibrary("javax-annotation-api").get())
}
