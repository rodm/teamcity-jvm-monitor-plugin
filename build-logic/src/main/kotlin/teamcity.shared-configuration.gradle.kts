
plugins {
    id ("org.gradle.java")
    id ("org.gradle.jacoco")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks {
    test {
        useJUnitPlatform()
        finalizedBy(named("jacocoTestReport"))
    }

    jacocoTestReport {
        reports {
            xml.required.set(true)
        }
    }
}
