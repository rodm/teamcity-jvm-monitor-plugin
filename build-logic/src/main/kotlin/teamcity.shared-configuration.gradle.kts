
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
    testImplementation (platform("org.junit:junit-bom:5.11.4"))
    testImplementation (group = "org.junit.jupiter", name = "junit-jupiter-api")
    testImplementation (group = "org.junit.jupiter", name = "junit-jupiter-params")
    testImplementation (group = "org.hamcrest", name = "hamcrest", version = "3.0")
    testImplementation (group = "org.mockito", name = "mockito-core", version = "4.11.0")

    testRuntimeOnly (group = "org.junit.platform", name = "junit-platform-launcher")
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
