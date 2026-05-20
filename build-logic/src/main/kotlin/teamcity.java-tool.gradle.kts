
plugins {
    id ("org.gradle.java")
    id ("org.gradle.jacoco")
    id ("org.gradle.java-test-fixtures")
}

repositories {
    mavenCentral()
}

dependencies {
    // configure dependencies that are compatible with Java 8
    testImplementation (platform("org.junit:junit-bom:5.14.4"))
    testImplementation ("org.junit.jupiter:junit-jupiter-api")
    testImplementation ("org.junit.jupiter:junit-jupiter-params")
    testImplementation ("org.hamcrest:hamcrest:3.0")
    testImplementation ("org.mockito:mockito-core:4.11.0")

    testRuntimeOnly ("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

tasks {
    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport)
    }

    jacocoTestReport {
        reports {
            xml.required = true
        }
    }
}
