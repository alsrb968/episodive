plugins {
    alias(libs.plugins.episodive.android.library)
    alias(libs.plugins.episodive.android.library.jacoco)
    alias(libs.plugins.episodive.android.hilt)
    alias(libs.plugins.episodive.android.test)
}

android {
    namespace = "io.jacob.episodive.core.data"

    lint {
        // FIXME: Temporarily disabled, as it is causing issues with the build process.
        disable += setOf(
            "FlowOperatorInvokedInComposition",
            "CoroutineCreationDuringComposition"
        )
    }
}

dependencies {
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.network)
    implementation(projects.core.player)

    //----- Coil
    implementation(libs.coil.compose)

    //----- Palette
    implementation(libs.androidx.palette.ktx)

    //----- Paging
    implementation(libs.androidx.paging.runtime)

    //----- Room
    implementation(libs.androidx.room.runtime)
}