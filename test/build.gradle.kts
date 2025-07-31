
plugins {
    id ("teamcity.environments")
}

val plugins by configurations.creating

dependencies {
    plugins (project(path = ":jvm-monitor-server", configuration = "plugin"))
}

teamcity {
    environments {
        val java21Home = project.findProperty("java21.home") as String? ?: "/opt/jdk-21.0.2"
        val serverDebugOptions = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
        val agentDebugOptions = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006"

        register("teamcity2018.1") {
            version = "2018.1.5"
            plugins = configurations["plugins"]
            serverOptions (serverDebugOptions)
            agentOptions (agentDebugOptions)
        }

        register("teamcity2025.07") {
            version = "2025.07"
            javaHome = java21Home
            plugins = configurations["plugins"]
            serverOptions (serverDebugOptions)
            agentOptions (agentDebugOptions)
        }
    }
}
