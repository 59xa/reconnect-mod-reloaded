plugins {
    id("multiloader-platform")
    id("dev.architectury.loom") version "1.17.493"
}

val target = BuildConf(project)
val commonProject = ":common:${target.minecraftVersion}"
base.archivesName = "reconnect-neoforge"
sourceSets.main { java.srcDir(rootDir.resolve("neoforge/compat/${target.neoForgeCompatibility}/java")) }

repositories { maven("https://maven.neoforged.net/releases/") }

val commonJava = configurations.create("commonJava") {
    isCanBeResolved = true
    isCanBeConsumed = false
}
val commonResources = configurations.create("commonResources") {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    minecraft("com.mojang:minecraft:${target.minecraftVersion}")
    mappings(loom.officialMojangMappings())
    neoForge("net.neoforged:neoforge:${target.neoForgeVersion}")
    commonJava(project(path = commonProject, configuration = "commonJava"))
    commonResources(project(path = commonProject, configuration = "commonResources"))
}

sourceSets.main {
    compileClasspath += commonJava
    runtimeClasspath += commonJava
}

loom {
    runs.named("client") { programArg("--mixin.config=reconnect.mixins.json") }
    mods {
        register("reconnect") {
            sourceSet(sourceSets.main.get())
            configuration(commonJava)
            configuration(commonResources)
        }
    }
}

tasks.jar {
    from(commonJava)
    manifest.attributes("MixinConfigs" to "reconnect.mixins.json")
}
tasks.remapJar { destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs/${target.minecraftVersion}")) }
tasks.processResources {
    from(commonResources)
    if (target.javaVersion == 17) {
        filesMatching("META-INF/neoforge.mods.toml") {
            name = "mods.toml"
            filter { line: String -> if (line == "type = \"required\"") "mandatory = true" else line }
        }
    }
}
