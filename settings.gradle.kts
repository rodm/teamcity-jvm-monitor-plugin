
pluginManagement {
    plugins {
        id ("com.github.rodm.teamcity-server") version "1.3.2"
        id ("com.jfrog.bintray") version "1.8.5"
        id ("org.sonarqube") version "3.1.1"
    }
}

rootProject.name = "teamcity-jvm-monitor-plugin"

include ("common")
include ("agent")
include ("server")
