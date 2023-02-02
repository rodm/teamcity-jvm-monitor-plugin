
pluginManagement {
    plugins {
        id ("org.sonarqube") version "3.4.0.2513"
    }
}

rootProject.name = "teamcity-jvm-monitor-plugin"

includeBuild ("build-logic")

include ("monitor")
include ("common")
include ("agent")
include ("server")
include ("test")
