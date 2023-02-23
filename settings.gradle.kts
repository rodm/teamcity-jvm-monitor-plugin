
pluginManagement {
    plugins {
        id ("org.sonarqube") version "3.5.0.2730"
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
