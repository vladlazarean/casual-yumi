plugins {
    alias(libs.plugins.yumi.android.application)
}

android {
    namespace = "dev.yumi.catalog"
    defaultConfig {
        applicationId = "dev.yumi.catalog"
        versionCode = 1
        versionName = libs.versions.yumi.get()
    }
}

dependencies {
    implementation(project(":yumi-components"))
    implementation(libs.activity.compose)
}
