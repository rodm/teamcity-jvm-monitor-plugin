
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
    archivesBaseName = "jvm-monitor-agent"
}

val java7Home = findProperty("java7.home") ?: ""
val java8Home = findProperty("java8.home") ?: ""
val java9Home = findProperty("java9.home") ?: ""
val java10Home = findProperty("java10.home") ?: ""
val java11Home = findProperty("java11.home") ?: ""
val java12Home = findProperty("java12.home") ?: ""

sourceSets {
    create("functional") {
        compileClasspath += sourceSets["main"].output + configurations.testRuntime
        runtimeClasspath += sourceSets["main"].output + configurations.testRuntime
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
