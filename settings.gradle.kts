
pluginManagement {
    plugins {
        id ("io.github.rodm.teamcity-server") version "1.5"
        id ("org.sonarqube") version "3.4.0.2513"
    }
}

rootProject.name = "teamcity-jvm-monitor-plugin"

include ("monitor")
include ("common")
include ("agent")
include ("server")
