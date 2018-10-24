
import com.github.rodm.teamcity.TeamCityEnvironment
import com.github.rodm.teamcity.TeamCityPluginExtension

plugins {
    id ("org.gradle.java")
    id ("org.gradle.jacoco")
    id ("com.github.rodm.teamcity-server")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

base {
    archivesBaseName = "jvm-monitor-server"
}

extra["downloadsDir"] = project.findProperty("downloads.dir") ?: "${rootDir}/downloads"
extra["serversDir"] = project.findProperty("servers.dir") ?: "${rootDir}/servers"
extra["java8Home"] = project.findProperty("java8.home") ?: "/opt/jdk1.8.0_131"

dependencies {
    compile (project(":common"))

    agent (project(path = ":agent", configuration = "plugin"))
}

teamcity {
    server {
        archiveName = "jvm-monitor-${rootProject.version}.zip"

        descriptor {
            name = "jvm-monitor"
            displayName = "JVM Monitor"
            version = rootProject.version as String?
            description = "Collects JVM metrics for any JVM running during the build and publishes them as artifacts"
            vendorName = "Rod MacKenzie"
            vendorUrl = "https://github.com/rodm/teamcity-jvm-monitor-plugin"
            email = "rod.n.mackenzie@gmail.com"
            useSeparateClassloader = true
        }
    }

    environments {
        downloadsDir = extra["downloadsDir"] as String
        baseHomeDir = extra["serversDir"] as String
        baseDataDir = "${rootDir}/data"

        operator fun String.invoke(block: TeamCityEnvironment.() -> Unit) {
            environments.create(this, closureOf<TeamCityEnvironment>(block))
        }

        "teamcity10" {
            version = "10.0.5"
            javaHome = file(extra["java8Home"] as String)
        }

        "teamcity2017.1" {
            version = "2017.1.5"
            javaHome = file(extra["java8Home"] as String)
        }
    }
}

fun Project.teamcity(configuration: TeamCityPluginExtension.() -> Unit) {
    configure(configuration)
}
