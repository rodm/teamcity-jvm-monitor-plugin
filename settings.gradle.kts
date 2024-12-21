
pluginManagement {
    plugins {
        id ("org.sonarqube") version "4.0.0.2929"
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("json-api", "jakarta.json:jakarta.json-api:2.1.3")
            library("json-impl", "org.eclipse.parsson:parsson:1.1.7")

            version("log4j", "1.2.17")
            library("log4j", "log4j", "log4j").versionRef("log4j")

            version("log4j2", "2.24.3")
            library("log4j2-api", "org.apache.logging.log4j", "log4j-api").versionRef("log4j2")
            library("log4j2-core", "org.apache.logging.log4j", "log4j-core").versionRef("log4j2")
            bundle("log4j2", listOf("log4j2-api", "log4j2-core"))
        }
    }
}

rootProject.name = "jvm-monitor"

includeBuild ("build-logic")

include ("tool")
include ("common")
include ("agent")
include ("server")
include ("test")

rootProject.children.forEach { project ->
    project.name = "${rootProject.name}-${project.name}"
}
