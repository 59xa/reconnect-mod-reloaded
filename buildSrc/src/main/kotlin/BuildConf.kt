import org.gradle.api.Project
import java.util.Properties

class BuildConf(project: Project) {
    private val properties = Properties().apply {
        project.rootDir.resolve("versions/${project.name}/gradle.properties").inputStream().use(::load)
    }

    val minecraftVersion: String = project.name
    val javaVersion: Int = properties.getProperty("java_version").toInt()
    val resourcePackFormat: Int = properties.getProperty("resource_pack_format").toInt()
    val resourcePackMinor: Int = properties.getProperty("resource_pack_minor", "0").toInt()
    val dataPackFormat: Int = properties.getProperty("data_pack_format").toInt()
    val packMetadata: String = if (resourcePackFormat >= 65) {
        """{"description":"Reconnect resources","min_format":[$resourcePackFormat,$resourcePackMinor],"max_format":[$resourcePackFormat,$resourcePackMinor]}"""
    } else {
        """{"description":"Reconnect resources","pack_format":$resourcePackFormat}"""
    }
    val fabricLoaderVersion: String = properties.getProperty("fabric_loader_version")
    val fabricApiVersion: String = properties.getProperty("fabric_api_version")
    val neoForgeVersion: String = properties.getProperty("neoforge_version", "")
    val forgeVersion: String = properties.getProperty("forge_version", "")
    val forgeLoaderVersion: String = properties.getProperty("forge_loader_version", "")
    val forgeBytecodeVersion: Int = properties.getProperty("forge_bytecode_version", javaVersion.toString()).toInt()
    val neoForgeLoaderVersion: String = properties.getProperty("neoforge_loader_version", "4")
    val obfuscated: Boolean = minecraftVersion.startsWith("1.")
    val compatibility: String = when {
        minecraftVersion == "1.20.1" -> "1.20.1"
        javaVersion == 17 -> "1.20.2"
        obfuscated -> "legacy"
        minecraftVersion.startsWith("26.1") -> "26.1"
        else -> "26.2"
    }
    val realmsCompatibility: String = if (minecraftVersion == "1.20.1") "legacy" else "modern"
    val disconnectedScreenCompatibility: String = if (minecraftVersion == "1.20.1") "legacy" else "modern"
    val neoForgeCompatibility: String = when {
        !obfuscated -> "modern"
        minecraftVersion.startsWith("1.21.") && minecraftVersion.substringAfterLast('.').toInt() >= 9 -> "modern"
        else -> "legacy"
    }
    val modVersion: String = "${project.providers.gradleProperty("mod_version").get()}-$minecraftVersion"
}
