
plugins {
    id ("org.gradle.java")
    id ("org.gradle.jacoco")
}

dependencies {
    testImplementation (platform("org.junit:junit-bom:6.1.0"))
    testImplementation ("org.junit.jupiter:junit-jupiter-api")
    testImplementation ("org.junit.jupiter:junit-jupiter-params")
    testImplementation ("org.hamcrest:hamcrest:3.0")
    testImplementation ("org.mockito:mockito-core:5.23.0")

    testRuntimeOnly ("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks {
    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport)
        jvmArgs ("-XX:+EnableDynamicAgentLoading")
    }

    jacocoTestReport {
        reports {
            xml.required = true
        }
    }
}
