
plugins {
    id ("teamcity.base")
    id ("org.sonarqube")
}

group = "com.github.rodm"
version = "1.0-SNAPSHOT"

extra["teamcityVersion"] = project.findProperty("teamcity.api.version") as String? ?: "2018.1"

teamcity {
    version = extra["teamcityVersion"] as String
}
