plugins {
    `kotlin-dsl`
}

group = "dev.yumi.buildlogic"

// Target JVM 17 bytecode but build with whatever JDK runs Gradle (17+), so contributors
// don't need a separate JDK 17 install.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = libs.plugins.yumi.android.library.get().pluginId
            implementationClass = "dev.yumi.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidApplication") {
            id = libs.plugins.yumi.android.application.get().pluginId
            implementationClass = "dev.yumi.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("publishing") {
            id = libs.plugins.yumi.publishing.get().pluginId
            implementationClass = "dev.yumi.buildlogic.PublishingConventionPlugin"
        }
    }
}
