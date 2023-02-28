
pluginManagement {
    plugins {
        id ("org.sonarqube") version "3.5.0.2730"
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("json", "1.1.4")
            library("json-api", "javax.json", "javax.json-api").versionRef("json")
            library("json-impl", "org.glassfish", "javax.json").versionRef("json")

            version("log4j", "1.2.17")
            library("log4j", "log4j", "log4j").versionRef("log4j")

            version("log4j2", "2.12.4")
            library("log4j1-api", "org.apache.logging.log4j", "log4j-1.2-api").versionRef("log4j2")
            library("log4j2-api", "org.apache.logging.log4j", "log4j-api").versionRef("log4j2")
            library("log4j2-core", "org.apache.logging.log4j", "log4j-core").versionRef("log4j2")
            bundle("log4j2", listOf("log4j1-api", "log4j2-api", "log4j2-core"))
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
