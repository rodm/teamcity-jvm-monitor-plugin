
pluginManagement {
    plugins {
        id ("org.sonarqube") version "3.4.0.2513"
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
