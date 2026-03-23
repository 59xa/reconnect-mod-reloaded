plugins {
    id("multiloader-platform")

    id("net.fabricmc.fabric-loom") version ("1.15.5")
}

base {
    archivesName = "reconnect-fabric"
}

val configurationCommonModJava: Configuration = configurations.create("commonJava") {
    isCanBeResolved = true
}

dependencies {
    implementation(project(":common"))
    configurationCommonModJava(project(path = ":common", configuration = "commonMainJava"))
}

sourceSets["main"].compileClasspath += project(":common").sourceSets["main"].output
sourceSets["main"].runtimeClasspath += project(":common").sourceSets["main"].output

sourceSets.apply {
    main {
        compileClasspath += configurationCommonModJava
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${BuildConf.MINECRAFT_VERSION}")

    implementation("net.fabricmc:fabric-loader:${BuildConf.FABRIC_LOADER_VERSION}")

    fun addEmbeddedFabricModule(name: String) {
        val module = fabricApi.module(name, BuildConf.FABRIC_API_VERSION)
        implementation(module)
        include(module)
    }

    addEmbeddedFabricModule("fabric-api-base")
    addEmbeddedFabricModule("fabric-networking-api-v1")
    addEmbeddedFabricModule("fabric-command-api-v2")
}

tasks.named<Jar>("jar") {
    from(project(":common").sourceSets["main"].output)
}