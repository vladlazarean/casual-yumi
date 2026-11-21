package dev.yumi.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** `yumi.android.application`: the catalog app. No explicit API / ABI checks; it is not published. */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<ApplicationExtension> {
            compileSdk = libs.versionInt("compileSdk")
            defaultConfig {
                minSdk = libs.versionInt("minSdk")
                targetSdk = libs.versionInt("targetSdk")
            }
            compileOptions {
                sourceCompatibility = YumiJavaVersion
                targetCompatibility = YumiJavaVersion
            }
            buildFeatures {
                compose = true
            }
        }

        configureKotlin(publicLibrary = false)
        configureComposeDependencies(publicLibrary = false)
    }
}
