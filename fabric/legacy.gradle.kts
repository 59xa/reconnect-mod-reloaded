plugins {
    id("multiloader-platform")
    id("fabric-loom") version "1.17.21"
}

val target = BuildConf(project)
val commonProject = ":common:${target.minecraftVersion}"

base.archivesName = "reconnect-fabric"

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
    modImplementation("net.fabricmc:fabric-loader:${target.fabricLoaderVersion}")
    commonJava(project(path = commonProject, configuration = "commonJava"))
    commonResources(project(path = commonProject, configuration = "commonResources"))

    listOf("fabric-api-base", "fabric-networking-api-v1", "fabric-command-api-v2").forEach {
        val module = fabricApi.module(it, target.fabricApiVersion)
        modImplementation(module)
        include(module)
    }
}

sourceSets.main {
    compileClasspath += commonJava
    runtimeClasspath += commonJava
}

tasks.jar { from(commonJava) }
tasks.remapJar { destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs/${target.minecraftVersion}")) }
tasks.processResources { from(commonResources) }

loom {
    mods {
        register("reconnect") {
            sourceSet(sourceSets.main.get())
            configuration(commonJava)
            configuration(commonResources)
        }
    }
}
