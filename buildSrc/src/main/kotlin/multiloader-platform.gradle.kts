plugins {
    id("multiloader-base")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(rootDir.resolve("LICENSE"))
}
