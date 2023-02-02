
plugins {
    id ("io.github.rodm.teamcity-environments")
}

val plugins by configurations.creating

dependencies {
    plugins (project(path = ":server", configuration = "plugin"))
}

teamcity {
    environments {
        downloadsDir = rootProject.extra["downloadsDir"] as String
        baseHomeDir = rootProject.extra["serversDir"] as String
        baseDataDir = "${rootDir}/data"

        register("teamcity2018.1") {
            version = "2018.1.5"
            serverOptions ("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005")
            agentOptions ("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006")
            plugins = configurations["plugins"]
        }

        register("teamcity2020.2") {
            version = "2020.2.4"
            plugins = configurations["plugins"]
        }
    }
}
