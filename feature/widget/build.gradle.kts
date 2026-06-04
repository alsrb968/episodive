plugins {
    alias(libs.plugins.episodive.android.widget)
}

android {
    namespace = "io.jacob.episodive.feature.widget"
}

dependencies {
    implementation(libs.androidx.palette.ktx)
    implementation(libs.timber)
}
