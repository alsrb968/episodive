plugins {
    alias(libs.plugins.episodive.android.widget)
}

android {
    namespace = "io.jacob.episodive.feature.widget"
}

dependencies {
    implementation(libs.androidx.palette.ktx)
    implementation(libs.timber)

    implementation(libs.androidx.glance.preview)
    debugImplementation(libs.androidx.glance.appwidget.preview)
}
