plugins {
    id("java-library")
    id("idea")
}

group = "io.xa59.reconnect"
version = BuildConf.getVersionString(project)

java.toolchain.languageVersion = JavaLanguageVersion.of(25)

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(25)
}

repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }

        filter {
            includeGroup("maven.modrinth")
        }
    }
}