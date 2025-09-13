//import java.util.Properties

//val localProperties = Properties()
//val localPropertiesFile = rootProject.file("local.properties")
//if (localPropertiesFile.exists()) {
//    localProperties.load(localPropertiesFile.inputStream())
//}

plugins {
    alias(libs.plugins.episodive.android.library)
    alias(libs.plugins.episodive.android.library.jacoco)
    alias(libs.plugins.episodive.android.room)
    alias(libs.plugins.episodive.android.hilt)
    alias(libs.plugins.episodive.android.test)
}

android {
    namespace = "io.jacob.episodive.core.data"

    buildTypes {
        all {
//            buildConfigField("String", "TOURAPI_SERVICE_KEY", "\"${localProperties["tourapi.serviceKey"]}\"")
//            buildConfigField("String", "TOURAPI_OS", "\"AND\"")
//            buildConfigField("String", "TOURAPI_APP_NAME", "\"${rootProject.name}\"")
        }
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.model)

    implementation(libs.androidx.media3.exoplayer)

    //----- Paging
    implementation(libs.androidx.paging.runtime)

    //----- Retrofit
    implementation(libs.squareup.retrofit2.retrofit)
    implementation(libs.squareup.retrofit2.converter.gson)
    implementation(libs.squareup.okhttp3.logging.interceptor)
    implementation(libs.google.gson)

    //----- Test
    testImplementation(libs.squareup.okhttp3.mockwebserver)
}