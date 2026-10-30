plugins {
    id("multiloader-base")
    id("fabric-loom") version "1.17.21"
}

val target = BuildConf(project)

base.archivesName = "reconnect-common"

sourceSets.main { java.srcDir(rootDir.resolve("common/compat/${target.compatibility}/java")) }
sourceSets.main { java.srcDir(rootDir.resolve("common/compat/realms/${target.realmsCompatibility}/java")) }
sourceSets.main { java.srcDir(rootDir.resolve("common/compat/disconnect/${target.disconnectedScreenCompatibility}/java")) }

dependencies {
    minecraft("com.mojang:minecraft:${target.minecraftVersion}")
    mappings(loom.officialMojangMappings())
    compileOnly("net.fabricmc:fabric-loader:${target.fabricLoaderVersion}")
    compileOnly("net.fabricmc:sponge-mixin:0.17.5+mixin.0.8.7")
}

val commonJava = configurations.create("commonJava") {
    isCanBeResolved = false
    isCanBeConsumed = true
}

artifacts.add(commonJava.name, tasks.compileJava.flatMap { it.destinationDirectory }) {
    builtBy(tasks.compileJava)
}

val commonResources = configurations.create("commonResources") {
    isCanBeResolved = false
    isCanBeConsumed = true
}

artifacts.add(commonResources.name, tasks.processResources.map { it.destinationDir }) {
    builtBy(tasks.processResources)
}

tasks.jar { enabled = false }
tasks.remapJar { enabled = false }
