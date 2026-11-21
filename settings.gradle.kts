pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "casual-yumi"

include(
    ":yumi-tokens",
    ":yumi-foundation",
    ":yumi-components",
    ":yumi-bom",
    ":catalog",
)
// Added in later milestones:
// include(":yumi-icons")       // optional
