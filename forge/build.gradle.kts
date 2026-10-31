plugins {
    id("multiloader-platform")
    id("dev.architectury.loom-no-remap") version "1.17.493"
}

val target = BuildConf(project)
val commonProject = ":common:${target.minecraftVersion}"
evaluationDependsOn(commonProject)
val common = project(commonProject)
base.archivesName = "reconnect-forge"

val eventCompatibility = if (target.forgeLoaderVersion.toInt() >= 56) "modern" else "legacy"
sourceSets.main { java.srcDir(rootDir.resolve("forge/compat/$eventCompatibility/java")) }

repositories { maven("https://maven.minecraftforge.net/") }

val commonResources = configurations.create("commonResources") {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    minecraft("com.mojang:minecraft:${target.minecraftVersion}")
    forge("net.minecraftforge:forge:${target.minecraftVersion}-${target.forgeVersion}")
    commonResources(project(path = commonProject, configuration = "commonResources"))
}

tasks.compileJava {
    options.release.set(target.forgeBytecodeVersion)
    source(common.tasks.named<JavaCompile>("compileJava").map { it.source })
}

loom {
    forge { mixinConfig("reconnect.mixins.json") }
    mods {
        register("reconnect") {
            sourceSet(sourceSets.main.get())
            configuration(commonResources)
        }
    }
}

tasks.jar {
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs/${target.minecraftVersion}"))
    manifest.attributes("MixinConfigs" to "reconnect.mixins.json")
}
tasks.processResources {
    inputs.property("bytecode_version", target.forgeBytecodeVersion)
    from(commonResources)
    filesMatching("reconnect.mixins.json") {
        filter { line: String -> line.replace("JAVA_${target.javaVersion}", "JAVA_${target.forgeBytecodeVersion}") }
    }
}
