
plugins {
    id ("teamcity.base")
    id ("org.sonarqube")
}

group = "com.github.rodm"
version = "1.1-SNAPSHOT"

extra["teamcityVersion"] = project.findProperty("teamcity.api.version") as String? ?: "2018.1"

teamcity {
    version = extra["teamcityVersion"] as String
}

sonarqube {
    properties {
        property("sonar.projectKey", "${project.group}:teamcity-jvm-monitor-plugin")
        property("sonar.projectName", "teamcity-jvm-monitor-plugin")
        property("sonar.issue.ignore.multicriteria", "e1")
        property("sonar.issue.ignore.multicriteria.e1.ruleKey", "java:S1191")
        property("sonar.issue.ignore.multicriteria.e1.resourceKey", "**/*.java")
    }
}
