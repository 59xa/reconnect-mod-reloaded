plugins {
    id("multiloader-platform")
    id("net.neoforged.moddev.legacyforge") version "2.0.148"
}

val target = BuildConf(project)
val commonProject = ":common:${target.minecraftVersion}"
evaluationDependsOn(commonProject)
val common = project(commonProject)
val isNeoForge = project.parent!!.name == "neoforge"

base.archivesName = "reconnect-${if (isNeoForge) "neoforge" else "forge"}"

sourceSets.main {
    java.setSrcDirs(listOf(rootDir.resolve("forge/compat/legacy/java")))
    resources.setSrcDirs(listOf(rootDir.resolve("forge/src/main/resources")))
}

val commonResources = configurations.create("commonResources") {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    commonResources(project(path = commonProject, configuration = "commonResources"))
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

legacyForge {
    enable {
        if (isNeoForge) neoForgeVersion = target.neoForgeVersion
        else forgeVersion = "${target.minecraftVersion}-${target.forgeVersion}"
    }
    runs {
        create("client") { client() }
    }
    mods {
        create("reconnect") { sourceSet(sourceSets.main.get()) }
    }
}

tasks.compileJava {
    source(common.tasks.named<JavaCompile>("compileJava").map { it.source })
}

mixin {
    add(sourceSets.main.get(), "reconnect.refmap.json")
    config("reconnect.mixins.json")
}

tasks.jar {
    manifest.attributes("MixinConfigs" to "reconnect.mixins.json")
}

tasks.processResources {
    from(commonResources)
    filesMatching("reconnect.mixins.json") {
        filter { line: String ->
            if (line.contains("\"package\":")) "$line\n    \"refmap\": \"reconnect.refmap.json\"," else line
        }
    }
    if (isNeoForge) {
        filesMatching("META-INF/mods.toml") {
            filter { line: String ->
                when {
                    line.startsWith("loaderVersion") -> "loaderVersion = \"[47,)\""
                    line.startsWith("versionRange = \"[${target.forgeVersion},") -> "versionRange = \"[47.1,)\""
                    else -> line
                }
            }
        }
    }
}

tasks.named<net.neoforged.moddevgradle.legacyforge.tasks.RemapJar>("reobfJar") {
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs/${target.minecraftVersion}"))
}
