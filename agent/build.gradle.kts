
plugins {
    id ("org.gradle.java")
    id ("org.gradle.jacoco")
    id ("com.github.rodm.teamcity-agent")
}

val javaHome = System.getProperty("java.home")

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

base {
    archivesName.set("jvm-monitor-agent")
}

val java7Home = findProperty("java7.home") ?: ""
val java8Home = findProperty("java8.home") ?: ""
val java9Home = findProperty("java9.home") ?: ""
val java10Home = findProperty("java10.home") ?: ""
val java11Home = findProperty("java11.home") ?: ""
val java12Home = findProperty("java12.home") ?: ""
val java13Home = findProperty("java13.home") ?: ""
val java14Home = findProperty("java14.home") ?: ""
val java15Home = findProperty("java15.home") ?: ""

sourceSets {
    create("functional") {
        compileClasspath += sourceSets["main"].output + configurations.testRuntimeClasspath
        runtimeClasspath += sourceSets["main"].output + configurations.testRuntimeClasspath
    }
}

dependencies {
    implementation (project(":common"))
    provided (files("${javaHome}/../lib/tools.jar"))

    testImplementation (platform("org.junit:junit-bom:5.5.2"))
    testImplementation (group = "org.junit.jupiter", name = "junit-jupiter-api")
    testImplementation (group = "org.junit.jupiter", name = "junit-jupiter-params")
    testImplementation (group = "org.hamcrest", name = "hamcrest", version = "2.2")
    testImplementation (group = "org.mockito", name = "mockito-core", version = "3.7.7")

    testRuntimeOnly (group = "org.junit.jupiter", name = "junit-jupiter-engine")
    testRuntimeOnly (group = "log4j", name = "log4j", version = "1.2.17")
}

tasks.named("test") {
    finalizedBy (tasks.named("jacocoTestReport"))
}

tasks.named<JacocoReport>("jacocoTestReport") {
    reports {
        xml.required.set(true)
    }
}

tasks {
    "test"(Test::class) {
        useJUnitPlatform()
    }

    val jar by existing

    task("functionalTest", Test::class) {
        group = "verification"
        description = "Runs the functional tests."
        useJUnitPlatform()
        testClassesDirs = sourceSets["functional"].output.classesDirs
        classpath = project.files(jar, sourceSets["functional"].runtimeClasspath)
        systemProperty ("java7.home", java7Home)
        systemProperty ("java8.home", java8Home)
        systemProperty ("java9.home", java9Home)
        systemProperty ("java10.home", java10Home)
        systemProperty ("java11.home", java11Home)
        systemProperty ("java12.home", java12Home)
        systemProperty ("java13.home", java13Home)
        systemProperty ("java14.home", java14Home)
        systemProperty ("java15.home", java15Home)
    }
}

teamcity {
    agent {
        archiveName = "jvm-monitor-agent.zip"

        descriptor {
            pluginDeployment {
                useSeparateClassloader = true
            }
        }
    }
}
