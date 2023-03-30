
plugins {
    id ("teamcity.java-tool")
}

val javaCompiler = javaToolchains.compilerFor { languageVersion.set(JavaLanguageVersion.of(8)) }
val javaHome = javaCompiler.get().metadata.installationPath.toString()
val toolsJar = "${javaHome}/lib/tools.jar"

dependencies {
    compileOnly (files(toolsJar))
    implementation (libs.bundles.log4j2)

    testImplementation (files(toolsJar))
}
