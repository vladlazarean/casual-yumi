plugins {
    `java-platform`
    alias(libs.plugins.yumi.publishing)
}

// dev.yumi:yumi-bom: pins every Casual Yumi artifact to the same version.
dependencies {
    constraints {
        api(project(":yumi-tokens"))
        api(project(":yumi-foundation"))
        api(project(":yumi-components"))
    }
}
