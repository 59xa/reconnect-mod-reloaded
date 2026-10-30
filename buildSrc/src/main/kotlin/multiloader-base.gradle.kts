plugins {
    id("java-library")
    id("idea")
}

val target = BuildConf(project)
group = "io.xa59.reconnect.${project.parent!!.name}"
version = target.modVersion

java.toolchain.languageVersion = JavaLanguageVersion.of(target.javaVersion)

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(target.javaVersion)
}

tasks.withType<Jar>().configureEach {
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs/${target.minecraftVersion}"))
}

if (project.parent?.name == "common") {
    val verifyReconnectSchedule = tasks.register<JavaExec>("verifyReconnectSchedule") {
        dependsOn(tasks.compileJava)
        classpath = files(sourceSets.main.get().output.classesDirs)
        mainClass.set(rootDir.resolve("scripts/tests/RetryScheduleTest.java").absolutePath)
    }
    tasks.check { dependsOn(verifyReconnectSchedule) }
}

tasks.processResources {
    val metadata = mapOf(
        "version" to target.modVersion,
        "minecraft_version" to target.minecraftVersion,
        "java_version" to target.javaVersion,
        "fabric_loader_version" to target.fabricLoaderVersion,
        "neoforge_version" to target.neoForgeVersion,
        "forge_version" to target.forgeVersion,
        "forge_loader_version" to target.forgeLoaderVersion,
        "neoforge_loader_version" to target.neoForgeLoaderVersion,
        "resource_pack_format" to target.resourcePackFormat,
        "data_pack_format" to target.dataPackFormat,
        "pack_metadata" to target.packMetadata
    )
    inputs.properties(metadata)
    filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml", "META-INF/mods.toml", "reconnect.mixins.json", "pack.mcmeta")) {
        expand(metadata)
    }
}
