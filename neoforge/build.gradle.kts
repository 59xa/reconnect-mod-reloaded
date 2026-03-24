plugins {
    id("multiloader-platform")

    id("net.neoforged.moddev") version("2.0.140")
}

base {
    archivesName = "reconnect-neoforge"
}

repositories {
    maven("https://maven.neoforged.net/releases/")
//    maven {
//        name = "Maven for PR #pr2879" // https://github.com/neoforged/NeoForge/pull/2879
//        url = uri("https://prmaven.neoforged.net/NeoForge/pr2879")
//        content {
//            includeModule("net.neoforged", "neoforge")
//            includeModule("net.neoforged", "testframework")
//        }
//    }
}

val configurationCommonModJava: Configuration = configurations.create("commonJava") {
    isCanBeResolved = true
}

dependencies {
    implementation(project(":common"))

    configurationCommonModJava(project(path = ":common", configuration = "commonMainJava"))

    jarJar(project(":neoforge", "main"))
}

val mainJar = tasks.register<Jar>("mainJar") {
    from(configurationCommonModJava)

    from(sourceSets["main"].output)

    from(rootDir.resolve("LICENSE"))

    filesMatching(listOf("META-INF/neoforge.mods.toml")) {
        expand(mapOf("version" to inputs.properties["version"]))
    }

    archiveClassifier = "main"
}

val configurationMod: Configuration = configurations.create("main") {
    isCanBeConsumed = true
    isCanBeResolved = true

    outgoing {
        artifact(mainJar)
    }
}

sourceSets["main"].resources.srcDirs += project(":common").sourceSets["main"].resources.srcDirs
sourceSets["main"].resources.srcDirs(
    project(":common").sourceSets["main"].resources.srcDirs
)

sourceSets {
    create("mod") {
        compileClasspath = sourceSets["main"].compileClasspath
        runtimeClasspath = sourceSets["main"].runtimeClasspath

        compileClasspath += configurationCommonModJava
        runtimeClasspath += configurationCommonModJava
    }
}

neoForge {
    version = BuildConf.NEOFORGE_VERSION

    runs {
        create("Client") {
            client()
            ideName = "NeoForge/Client"
        }
    }

    mods {
        create("reconnect") {
            sourceSet(sourceSets["main"])
            sourceSet(project(":common").sourceSets["main"])
        }
    }
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<Jar>("jar") {
    dependsOn(project(":common").tasks.named("processResources"))
}

tasks.named<ProcessResources>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks {
    jar {
        destinationDirectory.set(file(rootProject.layout.buildDirectory).resolve("main"))

        from(sourceSets.getByName("main").output.resourcesDir!!.resolve("META-INF/neoforge.mods.toml")) {
            into("META-INF")
        }

        from(project(":common").sourceSets.main.get().output.resourcesDir!!.resolve("icon.png")) {}
    }

    getByName<ProcessResources>("processModResources") {
        eachFile {
            println(path)
        }

        filesMatching(listOf("META-INF/neoforge.mods.toml")) {
            expand(mapOf("version" to BuildConf.getVersionString(rootProject)))
        }
    }

    processResources {
        from(project(":common").sourceSets["main"].resources) {
            include("reconnect.mixins.json")
        }
    }
}