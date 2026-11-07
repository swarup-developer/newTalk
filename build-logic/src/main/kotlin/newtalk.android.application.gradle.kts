import newtalk.buildlogic.configureCommonAndroid
import com.android.build.api.dsl.ApplicationExtension

plugins {
    id("com.android.application")
}

configure<ApplicationExtension> {
    configureCommonAndroid(this)
}
