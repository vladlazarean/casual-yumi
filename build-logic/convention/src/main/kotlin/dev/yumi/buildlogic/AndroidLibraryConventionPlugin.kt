package dev.yumi.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * `yumi.android.library`: a published Casual Yumi module.
 *
 * Applies AGP library + Compose compiler, explicit API mode, ABI validation and the shared
 * SDK levels. Each module only declares its namespace and dependencies.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<LibraryExtension> {
            compileSdk = libs.versionInt("compileSdk")
            defaultConfig {
                minSdk = libs.versionInt("minSdk")
            }
            compileOptions {
                sourceCompatibility = YumiJavaVersion
                targetCompatibility = YumiJavaVersion
            }
            buildFeatures {
                compose = true
            }
            // Library resources are merged into the consumer's R; prefix them to avoid clashes.
            resourcePrefix = "yumi_"
            testOptions {
                unitTests.isIncludeAndroidResources = true
            }
        }

        configureKotlin(publicLibrary = true)
        configureComposeDependencies(publicLibrary = true)
    }
}
