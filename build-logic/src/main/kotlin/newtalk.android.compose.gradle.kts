import com.android.build.api.dsl.CommonExtension
import newtalk.buildlogic.configureCompose

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

configure<CommonExtension> {
    configureCompose(this)
}
