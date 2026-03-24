plugins {
    id("multiloader-base")
    id("java-library")

    id("net.fabricmc.fabric-loom") version ("1.15.5")
}

base {
    archivesName = "reconnect-common"
}

sourceSets {
    val main = getByName("main")

    main.apply {
        compileClasspath += main.compileClasspath
    }

    create("desktop")
}

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = BuildConf.MINECRAFT_VERSION)

    compileOnly("io.github.llamalad7:mixinextras-common:0.5.0")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.0")

    compileOnly("net.fabricmc:sponge-mixin:0.13.2+mixin.0.8.5")
    compileOnly("net.fabricmc:fabric-loader:${BuildConf.FABRIC_LOADER_VERSION}")
}

fun exportSourceSetJava(name: String, sourceSet: SourceSet) {
    val configuration = configurations.create("${name}Java") {
        isCanBeResolved = true
        isCanBeConsumed = true
    }

    val compileTask = tasks.getByName<JavaCompile>(sourceSet.compileJavaTaskName)
    artifacts.add(configuration.name, compileTask.destinationDirectory) {
        builtBy(compileTask)
    }
}

fun exportSourceSetSources(name: String, sourceSet: SourceSet) {
    val configuration = configurations.create("${name}Sources") {
        isCanBeResolved = true
        isCanBeConsumed = true
    }

    val compileTask = tasks.register<Copy>(sourceSet.getTaskName("process", "sources")) {
        from(sourceSet.allSource)
        into(file(project.layout.buildDirectory).resolve("sources").resolve(sourceSet.name))
    }.get()
    artifacts.add(configuration.name, compileTask.destinationDir) {
        builtBy(compileTask)
    }
}

fun exportSourceSetResources(name: String, sourceSet: SourceSet) {
    val configuration = configurations.create("${name}Resources") {
        isCanBeResolved = true
        isCanBeConsumed = true
    }

    val compileTask = tasks.getByName<ProcessResources>(sourceSet.processResourcesTaskName)
    compileTask.apply {
        exclude("**/README.txt")
        exclude("/*.accesswidener")
    }

    artifacts.add(configuration.name, compileTask.destinationDir) {
        builtBy(compileTask)
    }
}

fun exportSourceSet(name: String, sourceSet: SourceSet) {
    exportSourceSetJava(name, sourceSet)
    exportSourceSetSources(name, sourceSet)
    exportSourceSetResources(name, sourceSet)
}

exportSourceSet("commonMain", sourceSets["main"])
exportSourceSet("commonDesktop", sourceSets["desktop"])

tasks.jar {
    enabled = false
    archiveBaseName.set("reconnect-common")
}