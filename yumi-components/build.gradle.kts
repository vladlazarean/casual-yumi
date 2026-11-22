plugins {
    alias(libs.plugins.yumi.android.library)
    alias(libs.plugins.yumi.publishing)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "dev.yumi.components"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                // Robolectric's hardware renderer draws Compose drop/inner shadows; the default
                // software path renders pebbles flat and the screenshots would be useless.
                it.systemProperty("robolectric.pixelCopyRenderMode", "hardware")
            }
        }
    }
}

dependencies {
    api(project(":yumi-foundation"))
    implementation(libs.compose.animation.core)
    implementation(libs.activity.compose)

    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    debugImplementation(libs.compose.ui.test.manifest)
}
