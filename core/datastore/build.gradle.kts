plugins {
    alias(libs.plugins.episodive.android.library)
    alias(libs.plugins.episodive.android.library.jacoco)
    alias(libs.plugins.episodive.android.hilt)
    alias(libs.plugins.episodive.android.test)
}

android {
    namespace = "io.jacob.episodive.core.datastore"
}

dependencies {
    implementation(projects.core.model)

    //----- DataStore
    implementation(libs.androidx.datastore.preferences)
}