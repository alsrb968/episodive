plugins {
    alias(libs.plugins.episodive.android.library)
    alias(libs.plugins.episodive.android.library.jacoco)
    alias(libs.plugins.episodive.android.hilt)
    alias(libs.plugins.episodive.android.test)
}

android {
    namespace = "io.jacob.episodive.core.data"
}

dependencies {
    implementation(projects.core.database)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.network)

    //----- Paging
    implementation(libs.androidx.paging.runtime)

    //----- Room
    implementation(libs.androidx.room.runtime)

    //----- Exoplayer
    implementation(libs.androidx.media3.exoplayer)
}