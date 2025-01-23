
plugins {
    id ("teamcity.environments")
}

val plugins by configurations.creating

dependencies {
    plugins (project(path = ":jvm-monitor-server", configuration = "plugin"))
}

teamcity {
    environments {
        val java11Home = project.findProperty("java11.home") as String? ?: "/opt/jdk-11.0.2"
        val serverDebugOptions = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
        val agentDebugOptions = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006"

        register("teamcity2018.1") {
            version = "2018.1.5"
            plugins = configurations["plugins"]
            serverOptions (serverDebugOptions)
            agentOptions (agentDebugOptions)
        }

        register("teamcity2024.12") {
            version = "2024.12.1"
            javaHome = java11Home
            plugins = configurations["plugins"]
            serverOptions (serverDebugOptions)
            agentOptions (agentDebugOptions)
        }
    }
}
