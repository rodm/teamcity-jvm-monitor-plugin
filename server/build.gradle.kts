
import com.github.rodm.teamcity.TeamCityEnvironment

plugins {
    id ("org.gradle.java")
    id ("org.gradle.jacoco")
    id ("com.github.rodm.teamcity-server")
    id ("com.github.rodm.teamcity-environments")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

base {
    archivesBaseName = "jvm-monitor-server"
}

dependencies {
    implementation (project(":common"))
    implementation (group = "javax.json", name = "javax.json-api", version = "1.1.4")
    runtimeOnly (group = "org.glassfish", name = "javax.json", version = "1.1.4")

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
        downloadsDir = rootProject.extra["downloadsDir"] as String
        baseHomeDir = rootProject.extra["serversDir"] as String
        baseDataDir = "${rootDir}/data"

        operator fun String.invoke(block: TeamCityEnvironment.() -> Unit) = environments.create(this, closureOf(block))

        "teamcity2018.1" {
            version = "2018.1.4"
            serverOptions ("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005")
            agentOptions ("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006")
        }

        "teamcity2018.2" {
            version = "2018.2.4"
        }

        "teamcity2019.1" {
            version = "2019.1.1"
        }
    }
}
