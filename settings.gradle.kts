pluginManagement {
    repositories {
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.architectury.dev/")
        gradlePluginPortal()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "reconnect"

val supportedVersions = listOf("1.20.1", "1.20.2", "1.20.3", "1.20.4", "1.20.5", "1.20.6", "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11", "26.1", "26.1.1", "26.1.2", "26.2", "26.3")
val requestedVersions = providers.gradleProperty("build_versions").orNull?.split(",")?.map(String::trim)
require(requestedVersions == null || requestedVersions.isNotEmpty() && requestedVersions.all { it in supportedVersions }) {
    "build_versions must contain supported Minecraft versions: ${supportedVersions.joinToString()}"
}
val selectedVersions = requestedVersions ?: supportedVersions
val selectedLoaders = providers.gradleProperty("build_loaders").orNull?.split(",")?.map(String::trim)
    ?: listOf("fabric", "neoforge", "forge")
require(selectedLoaders.isNotEmpty() && selectedLoaders.all { it in listOf("fabric", "neoforge", "forge") }) {
    "build_loaders must contain fabric, neoforge or forge"
}
val forgeVersions = selectedVersions.filter { target ->
    file("versions/$target/gradle.properties").readLines().any { it.startsWith("forge_version=") }
}
require(selectedVersions.all { "fabric" in selectedLoaders || "neoforge" in selectedLoaders || it in forgeVersions }) {
    "Forge is not published for the requested Minecraft version; use Fabric or NeoForge"
}

stonecutter {
    create(rootProject) {
        branch("common") {
            selectedVersions.forEach { version(it).buildscript(if (it.startsWith("1.")) "legacy.gradle.kts" else "build.gradle.kts") }
        }
        if ("fabric" in selectedLoaders) {
            branch("fabric") {
                selectedVersions.forEach { version(it).buildscript(if (it.startsWith("1.")) "legacy.gradle.kts" else "build.gradle.kts") }
            }
        }
        if ("neoforge" in selectedLoaders) {
            branch("neoforge") {
                selectedVersions.forEach {
                    version(it).buildscript(when {
                        it == "1.20.1" -> "../forge/legacy.gradle.kts"
                        it.startsWith("1.20.") -> "loom.gradle.kts"
                        else -> "build.gradle.kts"
                    })
                }
            }
        }
        forgeVersions.takeIf { "forge" in selectedLoaders && it.isNotEmpty() }?.let { targets ->
            branch("forge") {
                targets.forEach {
                    version(it).buildscript(when {
                        it == "1.20.1" -> "legacy.gradle.kts"
                        it == "1.20.3" -> "srg.gradle.kts"
                        it.startsWith("1.") -> "obfuscated.gradle.kts"
                        else -> "build.gradle.kts"
                    })
                }
            }
        }
        vcsVersion = if ("26.3" in selectedVersions) "26.3" else selectedVersions.last()
    }
}

gradle.beforeProject {
    if (parent?.name == "neoforge" && name.startsWith("1.20.") && name != "1.20.1") {
        extensions.extraProperties.set("loom.platform", "neoforge")
    } else if (parent?.name == "forge" && name !in listOf("1.20.1", "1.20.3")) {
        extensions.extraProperties.set("loom.platform", "forge")
    }
}
