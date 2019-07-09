
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

sourceSets {
    create("functional") {
        compileClasspath += sourceSets["main"].output + configurations.testRuntime
        runtimeClasspath += sourceSets["main"].output + configurations.testRuntime
    }
}

dependencies {
    compile (project(":common"))
    provided (files("${javaHome}/../lib/tools.jar"))

    testCompile (platform("org.junit:junit-bom:5.2.0"))
    testCompile (group = "org.junit.jupiter", name = "junit-jupiter-api")
    testCompile (group = "org.junit.jupiter", name = "junit-jupiter-params")
    testCompile (group = "org.junit-pioneer", name = "junit-pioneer", version = "0.1.2")
    testCompile (group = "org.hamcrest", name = "hamcrest-core", version = "1.3")
    testCompile (group = "org.hamcrest", name = "hamcrest-library", version = "1.3")
    testCompile (group = "org.mockito", name = "mockito-core", version = "2.20.0")

    testRuntime (group = "org.junit.jupiter", name = "junit-jupiter-engine")
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
