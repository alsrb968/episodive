import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

plugins {
    alias(libs.plugins.episodive.android.library)
    alias(libs.plugins.episodive.android.library.jacoco)
    alias(libs.plugins.episodive.android.hilt)
    alias(libs.plugins.episodive.android.test)
}

android {
    namespace = "io.jacob.episodive.core.network"

    buildTypes {
        all {
            buildConfigField("String", "API_KEY", "\"${localProperties["podcastIndex.apiKey"]}\"")
            buildConfigField("String", "SECRET_KEY", "\"${localProperties["podcastIndex.secretKey"]}\"")
        }
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    //----- Retrofit
    implementation(libs.squareup.retrofit2.retrofit)
    implementation(libs.squareup.retrofit2.converter.gson)
    implementation(libs.squareup.okhttp3.logging.interceptor)
    implementation(libs.google.gson)

    //----- Test
    testImplementation(libs.squareup.okhttp3.mockwebserver)
}