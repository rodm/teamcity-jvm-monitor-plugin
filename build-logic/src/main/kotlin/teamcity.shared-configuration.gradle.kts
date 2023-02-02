
plugins {
    id ("org.gradle.java")
    id ("org.gradle.jacoco")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    testImplementation (platform("org.junit:junit-bom:5.5.2"))
    testImplementation (group = "org.junit.jupiter", name = "junit-jupiter-api")
    testImplementation (group = "org.junit.jupiter", name = "junit-jupiter-params")
    testImplementation (group = "org.hamcrest", name = "hamcrest", version = "2.2")
    testImplementation (group = "org.mockito", name = "mockito-core", version = "3.7.7")

    testRuntimeOnly (group = "org.junit.jupiter", name = "junit-jupiter-engine")
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
