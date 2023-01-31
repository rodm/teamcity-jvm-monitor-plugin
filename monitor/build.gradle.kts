
plugins {
    id ("org.gradle.java-library")
    id ("org.gradle.jacoco")
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

base {
    archivesName.set("jvm-monitor-tool")
}

val javaHome = System.getProperty("java.home")

dependencies {
    compileOnly (files("${javaHome}/../lib/tools.jar"))
    implementation (group = "org.apache.logging.log4j", name = "log4j-1.2-api", version = "2.12.4")
    implementation (group = "org.apache.logging.log4j", name = "log4j-api", version = "2.12.4")
    implementation (group = "org.apache.logging.log4j", name = "log4j-core", version = "2.12.4")

    testImplementation (files("${javaHome}/../lib/tools.jar"))
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
        finalizedBy (named("jacocoTestReport"))
    }

    jacocoTestReport {
        reports {
            xml.required.set(true)
        }
    }
}
