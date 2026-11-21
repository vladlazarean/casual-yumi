plugins {
    alias(libs.plugins.yumi.android.library)
    alias(libs.plugins.yumi.publishing)
}

android {
    namespace = "dev.yumi.tokens"
}

dependencies {
    // Pure values: Color, Dp, Shape, @Immutable. Kept free of android.* so the module ports to CMP as-is.
    api(libs.compose.runtime)
    api(libs.compose.ui.graphics)
    api(libs.compose.ui.unit)
    api(libs.compose.foundation) // RoundedCornerShape / CircleShape
}
