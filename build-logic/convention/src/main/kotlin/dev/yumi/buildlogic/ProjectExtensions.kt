package dev.yumi.buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.versionInt(alias: String): Int =
    findVersion(alias).get().requiredVersion.toInt()

internal val YumiJavaVersion = JavaVersion.VERSION_17

/**
 * Kotlin settings shared by every module. AGP 9 compiles Kotlin itself ("built-in Kotlin"),
 * so we configure the `kotlin {}` extension it registers instead of applying
 * `org.jetbrains.kotlin.android`.
 */
internal fun Project.configureKotlin(publicLibrary: Boolean) {
    extensions.configure(KotlinAndroidProjectExtension::class.java) {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            allWarningsAsErrors.set(providers.gradleProperty("yumi.warningsAsErrors").map(String::toBoolean).orElse(false))
        }
        if (publicLibrary) {
            // Every public symbol must declare visibility + return type: no accidental API.
            explicitApi()
            // ABI validation (KGP `abiValidation {}`) is off for now: under AGP 9 built-in Kotlin
            // the Android target exposes no compilations, so the dump tasks have no classes and
            // fail. Re-enable once KGP supports it; until then explicit API mode + review is the guard.
        }
    }
}

/**
 * Compose BOM + tooling. Previews live in `src/debug`, so they never ship in the release AAR.
 *
 * Libraries put the BOM on `api` so the published metadata carries versions for the
 * version-less Compose `api` dependencies; the app only needs `implementation`.
 */
internal fun Project.configureComposeDependencies(publicLibrary: Boolean) {
    dependencies {
        val bom = libs.findLibrary("compose-bom").get()
        add(if (publicLibrary) "api" else "implementation", platform(bom))
        add("testImplementation", platform(bom))
        add("debugImplementation", libs.findLibrary("compose-ui-tooling-preview").get())
        add("debugImplementation", libs.findLibrary("compose-ui-tooling").get())
        add("testImplementation", libs.findLibrary("junit").get())
    }
}
