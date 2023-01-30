
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
    compileOnly (group = "log4j", name = "log4j", version = "1.2.17")

    testImplementation (files("${javaHome}/../lib/tools.jar"))
    testImplementation (platform("org.junit:junit-bom:5.5.2"))
    testImplementation (group = "org.junit.jupiter", name = "junit-jupiter-api")
    testImplementation (group = "org.junit.jupiter", name = "junit-jupiter-params")
    testImplementation (group = "org.hamcrest", name = "hamcrest", version = "2.2")
    testImplementation (group = "org.mockito", name = "mockito-core", version = "3.7.7")

    testRuntimeOnly (group = "org.junit.jupiter", name = "junit-jupiter-engine")
    testRuntimeOnly (group = "log4j", name = "log4j", version = "1.2.17")
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
