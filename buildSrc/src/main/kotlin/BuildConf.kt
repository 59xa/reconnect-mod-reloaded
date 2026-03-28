import org.gradle.api.Project

object BuildConf {
    const val MINECRAFT_VERSION: String = "26.1"
    const val FABRIC_LOADER_VERSION: String = "0.18.4"
    const val FABRIC_API_VERSION: String = "0.144.0+26.1"
    const val NEOFORGE_VERSION: String = "26.1.0.1-beta"

    var MOD_VERSION: String = "1.4"

    fun getVersionString(project: Project): String {
        val builder = StringBuilder()

        builder.append(MOD_VERSION).append("-").append(MINECRAFT_VERSION)

        return builder.toString()
    }
}