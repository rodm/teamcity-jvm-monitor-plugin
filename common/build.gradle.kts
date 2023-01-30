
plugins {
    id ("org.gradle.java")
    id ("org.gradle.jacoco")
    id ("io.github.rodm.teamcity-common")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

base {
    archivesName.set("jvm-monitor-common")
}
