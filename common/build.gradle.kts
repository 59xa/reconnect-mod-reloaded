plugins {
    id("multiloader-base")
    id("net.fabricmc.fabric-loom") version "1.17.21"
}

val target = BuildConf(project)

base.archivesName = "reconnect-common"

sourceSets.main { java.srcDir(rootDir.resolve("common/compat/${target.compatibility}/java")) }
sourceSets.main { java.srcDir(rootDir.resolve("common/compat/realms/${target.realmsCompatibility}/java")) }
sourceSets.main { java.srcDir(rootDir.resolve("common/compat/disconnect/${target.disconnectedScreenCompatibility}/java")) }

dependencies {
    minecraft("com.mojang:minecraft:${target.minecraftVersion}")
    compileOnly("net.fabricmc:fabric-loader:${target.fabricLoaderVersion}")
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
