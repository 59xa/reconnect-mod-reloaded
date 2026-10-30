plugins {
    id("multiloader-platform")
    id("net.minecraftforge.gradle") version "7.0.40"
    id("net.minecraftforge.renamer") version "1.0.14"
}

val target = BuildConf(project)
val commonProject = ":common:${target.minecraftVersion}"
evaluationDependsOn(commonProject)
val common = project(commonProject)
base.archivesName = "reconnect-forge"

sourceSets.main { java.srcDir(rootDir.resolve("forge/compat/legacy/java")) }

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
    mavenCentral()
}

minecraft {
    mappings("official", target.minecraftVersion)
    runs {
        configureEach { args("--mixin.config=reconnect.mixins.json") }
        register("client")
    }
}

val commonResources = configurations.create("commonResources") {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    implementation(minecraft.dependency("net.minecraftforge:forge:${target.minecraftVersion}-${target.forgeVersion}"))
    commonResources(project(path = commonProject, configuration = "commonResources"))
    annotationProcessor("org.spongepowered:mixin:0.8.7:processor")
}

tasks.compileJava { source(common.tasks.named<JavaCompile>("compileJava").map { it.source }) }

renamer.enableMixinRefmaps {
    config("reconnect.mixins.json")
    source(sourceSets.main.get()) { refMap.set("reconnect.refmap.json") }
    jar(tasks.jar)
}
renamer.mappings(minecraft.dependency.toSrg)
val reobfJar = renamer.classes(tasks.jar) {
    archiveClassifier.set("")
    output.set(rootProject.layout.buildDirectory.file("libs/${target.minecraftVersion}/reconnect-forge-${target.modVersion}.jar"))
    mappings(renamer.mixin.generatedMappings)
}

tasks.jar {
    inputs.property("refmap_name", "reconnect.refmap.json")
    archiveClassifier.set("dev")
    destinationDirectory.set(layout.buildDirectory.dir("devlibs"))
    manifest.attributes("MixinConfigs" to "reconnect.mixins.json")
}
tasks.assemble { dependsOn(reobfJar) }
tasks.processResources {
    inputs.property("mixin_remapping", "refmap")
    from(commonResources)
    filesMatching("reconnect.mixins.json") {
        filter { line: String ->
            if (line.contains("\"package\":")) "$line\n    \"refmap\": \"reconnect.refmap.json\"," else line
        }
    }
}
