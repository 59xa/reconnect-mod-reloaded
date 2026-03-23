plugins {
    id("multiloader-base")
}

val configurationDesktopIntegrationJava: Configuration = configurations.create("commonDesktopIntegration") {
    isCanBeResolved = true
}

dependencies {
    configurationDesktopIntegrationJava(project(path = ":common", configuration = "commonDesktopIntegrationJava"))
}

tasks {
    processResources {
        inputs.property("version", version)

        filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) {
            expand(mapOf("version" to inputs.properties["version"]))
        }
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.FAIL
        from(rootDir.resolve("LICENSE"))
    }
}
