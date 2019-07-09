
plugins {
    id ("org.gradle.java")
    id ("org.gradle.jacoco")
    id ("com.github.rodm.teamcity-common")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

base {
    archivesBaseName = "jvm-monitor-common"
}
