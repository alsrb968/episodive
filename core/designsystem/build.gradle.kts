plugins {
    alias(libs.plugins.episodive.android.library)
    alias(libs.plugins.episodive.android.library.compose)
    alias(libs.plugins.episodive.android.library.jacoco)
    alias(libs.plugins.episodive.android.test)
}

android {
    namespace = "io.jacob.episodive.core.designsystem"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.testing)
    implementation(projects.core.model)

    //----- Coil
    implementation(libs.coil.compose)

    //----- Paging
    implementation(libs.androidx.paging.compose)
}