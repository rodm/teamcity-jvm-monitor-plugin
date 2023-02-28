
plugins {
    id ("teamcity.server-plugin")
}

dependencies {
    implementation (project(":jvm-monitor-common"))
    implementation (libs.json.api)
    runtimeOnly (libs.json.impl)

    agent (project(path = ":jvm-monitor-agent", configuration = "plugin"))
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
}
