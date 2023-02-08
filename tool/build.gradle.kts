
plugins {
    id ("teamcity.java-tool")
}

repositories {
    mavenCentral()
}

base {
    archivesName.set("jvm-monitor-tool")
}

val javaCompiler = javaToolchains.compilerFor { languageVersion.set(JavaLanguageVersion.of(8)) }
val javaHome = javaCompiler.get().metadata.installationPath.toString()

dependencies {
    compileOnly (files("${javaHome}/lib/tools.jar"))
    implementation (group = "org.apache.logging.log4j", name = "log4j-1.2-api", version = "2.12.4")
    implementation (group = "org.apache.logging.log4j", name = "log4j-api", version = "2.12.4")
    implementation (group = "org.apache.logging.log4j", name = "log4j-core", version = "2.12.4")

    testImplementation (files("${javaHome}/lib/tools.jar"))
}

tasks {
    compileJava {
        sourceCompatibility = "1.7"
        targetCompatibility = "1.7"
    }
}
