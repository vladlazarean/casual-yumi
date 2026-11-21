package dev.yumi.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.withType

/**
 * `yumi.publishing`: publishes the module as `<yumi.group>:<project-name>`.
 *
 * Works for Android libraries (release variant + sources jar) and the `java-platform` BOM.
 * Releases are served by JitPack, which builds a tag with `publishToMavenLocal` (see
 * `jitpack.yml`), so no remote repository or credentials are configured here.
 */
class PublishingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("maven-publish")

        group = providers.gradleProperty("yumi.group").get()
        version = libs.findVersion("yumi").get().requiredVersion

        pluginManager.withPlugin("com.android.library") {
            extensions.configure<LibraryExtension> {
                publishing {
                    singleVariant("release") {
                        withSourcesJar()
                    }
                }
            }
            // AGP registers the "release" component late; react to it instead of afterEvaluate.
            components.matching { it.name == "release" }.all {
                val component = this
                extensions.configure<PublishingExtension> {
                    publications.create<MavenPublication>("release") { from(component) }
                }
            }
        }

        pluginManager.withPlugin("java-platform") {
            components.matching { it.name == "javaPlatform" }.all {
                val component = this
                extensions.configure<PublishingExtension> {
                    publications.create<MavenPublication>("bom") { from(component) }
                }
            }
        }

        val repository = providers.gradleProperty("yumi.github.repository").get()
        extensions.configure<PublishingExtension> {
            publications.withType<MavenPublication>().configureEach {
                pom {
                    name.set(project.name)
                    description.set("Casual Yumi: matte, soft-touch Jetpack Compose components.")
                    url.set("https://github.com/$repository")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    scm {
                        url.set("https://github.com/$repository")
                    }
                }
            }
        }
    }
}
