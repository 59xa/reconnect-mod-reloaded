plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active if (providers.gradleProperty("build_versions").isPresent) null else file("stonecutter.active")

tasks.register("build") {
    dependsOn(stonecutter.tasks.named("build"))
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
    dependsOn(stonecutter.tasks.named("clean"))
}

tasks.register("chiseledBuild") {
    group = "build"
    dependsOn(tasks.named("build"))
}

tasks.register("printVersion") {
    val modVersion = providers.gradleProperty("mod_version")
    doLast { println(modVersion.get()) }
}
