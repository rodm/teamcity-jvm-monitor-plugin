
pluginManagement {
    plugins {
        id ("org.sonarqube") version "6.2.0.5505"
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
