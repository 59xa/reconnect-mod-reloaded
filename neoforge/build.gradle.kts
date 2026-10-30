plugins {
    id("multiloader-platform")
    id("net.neoforged.moddev") version "2.0.148"
}

val target = BuildConf(project)
val commonProject = ":common:${target.minecraftVersion}"
evaluationDependsOn(commonProject)

base.archivesName = "reconnect-neoforge"
sourceSets.main { java.srcDir(rootDir.resolve("neoforge/compat/${target.neoForgeCompatibility}/java")) }

val commonJava = configurations.create("commonJava") {
    isCanBeResolved = true
    isCanBeConsumed = false
}
val commonResources = configurations.create("commonResources") {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    commonJava(project(path = commonProject, configuration = "commonJava"))
    commonResources(project(path = commonProject, configuration = "commonResources"))
}

sourceSets.main {
    compileClasspath += commonJava
    runtimeClasspath += commonJava
}

neoForge {
    version = target.neoForgeVersion
    runs {
        create("client") {
            client()
        }
    }
    mods {
        create("reconnect") {
            sourceSet(sourceSets.main.get())
            sourceSet(project(commonProject).extensions.getByType<SourceSetContainer>().named("main").get())
        }
    }
}

tasks.jar {
    from(commonJava)
    if (target.javaVersion == 17) manifest.attributes("MixinConfigs" to "reconnect.mixins.json")
}
tasks.processResources {
    from(commonResources)
    if (target.javaVersion == 17) {
        filesMatching("META-INF/neoforge.mods.toml") {
            name = "mods.toml"
            filter { line: String -> if (line == "type = \"required\"") "mandatory = true" else line }
        }
    }
}
