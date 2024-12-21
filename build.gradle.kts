
plugins {
    id ("org.sonarqube")
}

group = "com.github.rodm"
version = "1.2-SNAPSHOT"

sonarqube {
    properties {
        property("sonar.projectKey", "${project.group}:teamcity-jvm-monitor-plugin")
        property("sonar.projectName", "teamcity-jvm-monitor-plugin")
        property("sonar.issue.ignore.multicriteria", "e1")
        property("sonar.issue.ignore.multicriteria.e1.ruleKey", "java:S1191")
        property("sonar.issue.ignore.multicriteria.e1.resourceKey", "**/*.java")
    }
}
