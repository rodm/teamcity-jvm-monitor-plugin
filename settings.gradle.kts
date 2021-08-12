
pluginManagement {
    plugins {
        id ("com.github.rodm.teamcity-server") version "1.4"
        id ("org.sonarqube") version "3.3"
    }
}

rootProject.name = "teamcity-jvm-monitor-plugin"

include ("common")
include ("agent")
include ("server")
