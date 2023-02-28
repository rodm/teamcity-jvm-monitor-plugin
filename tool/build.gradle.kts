
plugins {
    id ("teamcity.java-tool")
}

repositories {
    mavenCentral()
}

val javaCompiler = javaToolchains.compilerFor { languageVersion.set(JavaLanguageVersion.of(8)) }
val javaHome = javaCompiler.get().metadata.installationPath.toString()

dependencies {
    compileOnly (files("${javaHome}/lib/tools.jar"))
    implementation (libs.bundles.log4j2)

    testImplementation (files("${javaHome}/lib/tools.jar"))
}

tasks {
    compileJava {
        sourceCompatibility = "1.7"
        targetCompatibility = "1.7"
    }
}
