plugins {
    alias(libs.plugins.yumi.android.library)
    alias(libs.plugins.yumi.publishing)
}

android {
    namespace = "dev.yumi.foundation"
}

dependencies {
    api(project(":yumi-tokens"))
    api(libs.compose.material3)
    api(libs.compose.foundation)
    implementation(libs.compose.animation.core)
    // Android-only: downloadable fonts. Isolated in YumiTypography.kt so a CMP port swaps one file.
    implementation(libs.compose.ui.text.google.fonts)
}
