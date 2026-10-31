plugins {
    id("multiloader-platform")
    id("dev.architectury.loom") version "1.17.493"
}

val target = BuildConf(project)
val commonProject = ":common:${target.minecraftVersion}"
base.archivesName = "reconnect-forge"

val eventCompatibility = if (target.forgeLoaderVersion.toInt() >= 56) "modern" else "legacy"
sourceSets.main { java.srcDir(rootDir.resolve("forge/compat/$eventCompatibility/java")) }

repositories { maven("https://maven.minecraftforge.net/") }

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
    forge("net.minecraftforge:forge:${target.minecraftVersion}-${target.forgeVersion}")
    commonJava(project(path = commonProject, configuration = "commonJava"))
    commonResources(project(path = commonProject, configuration = "commonResources"))
}

sourceSets.main {
    compileClasspath += commonJava
    runtimeClasspath += commonJava
}

loom {
    mixin { useLegacyMixinAp.set(false) }
    forge { mixinConfig("reconnect.mixins.json") }
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
    inputs.property("mixin_remapping", "inline")
    from(commonResources)
}
