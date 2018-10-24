
import com.github.rodm.teamcity.TeamCityPluginExtension
import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.RecordingCopyTask

plugins {
    id ("com.github.rodm.teamcity-server") version "1.1.1"
    id ("com.jfrog.bintray") version "1.8.4"
    id ("org.sonarqube") version "2.6.2"
}

group = "com.github.rodm"
version = "1.0-SNAPSHOT"

extra["teamcityVersion"] = project.findProperty("teamcity.api.version") as String? ?: "10.0"
extra["downloadsDir"] = project.findProperty("downloads.dir") as String? ?: "$rootDir/downloads"
extra["serversDir"] = project.findProperty("servers.dir") as String? ?: "$rootDir/servers"
extra["java8Home"] = project.findProperty("java8.home") as String? ?: "/opt/jdk1.8.0_192"

teamcity {
    version = extra["teamcityVersion"] as String
}

bintray {
    user = findProperty("bintray.user") as String?
    key = findProperty("bintray.key") as String?

    filesSpec(closureOf<RecordingCopyTask> {
        from ("${project(":server").buildDir}/distributions")
        into ("jvm-monitor")
        include ("*.zip")
    })

    dryRun = false
    publish = true
    override = false

    pkg(closureOf<BintrayExtension.PackageConfig> {
        repo = "teamcity-plugins-generic"
        name = "jvm-monitor"
        desc = "A TeamCity plugin that provides a build feature to collect JVM metrics during a build"
        websiteUrl = "https://github.com/rodm/teamcity-jvm-monitor-plugin"
        issueTrackerUrl = "https://github.com/rodm/teamcity-jvm-monitor-plugin/issues"
        vcsUrl = "https://github.com/rodm/teamcity-jvm-monitor-plugin.git"
        setLicenses("Apache-2.0")
        setLabels("teamcity", "plugin", "jvm", "monitor")

        version(closureOf<BintrayExtension.VersionConfig> {
            name = project.version as String
        })
    })
}

fun Project.teamcity(configuration: TeamCityPluginExtension.() -> Unit) {
    configure(configuration)
}

fun Project.bintray(configuration: BintrayExtension.() -> Unit) {
    configure(configuration)
}
