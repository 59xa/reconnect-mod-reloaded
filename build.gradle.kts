tasks.register("printVersion") {
    doLast {
        println(BuildConf.getVersionString(rootProject))
    }
}