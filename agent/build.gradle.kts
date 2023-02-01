
plugins {
    id ("teamcity.agent-plugin")
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

val tool by configurations.creating

dependencies {
    implementation (project(":common"))

    testImplementation (platform("org.junit:junit-bom:5.5.2"))
    testImplementation (group = "org.junit.jupiter", name = "junit-jupiter-api")
    testImplementation (group = "org.junit.jupiter", name = "junit-jupiter-params")
    testImplementation (group = "org.hamcrest", name = "hamcrest", version = "2.2")
    testImplementation (group = "org.mockito", name = "mockito-core", version = "3.7.7")

    testRuntimeOnly (group = "org.junit.jupiter", name = "junit-jupiter-engine")
    testRuntimeOnly (group = "log4j", name = "log4j", version = "1.2.17")

    tool (project(":monitor"))
}

tasks {
    named<JavaCompile>("compileFunctionalJava").configure {
        sourceCompatibility = JavaVersion.VERSION_1_7.toString()
        targetCompatibility = JavaVersion.VERSION_1_7.toString()
    }

    val toolDir = project.layout.buildDirectory.dir("tool")
    register("copyTool", Copy::class) {
        destinationDir = toolDir.get().asFile
        from(tool)
    }

    register("functionalTest", Test::class) {
        group = "verification"
        description = "Runs the functional tests."
        useJUnitPlatform()
        dependsOn(named("copyTool"))
        testClassesDirs = sourceSets["functional"].output.classesDirs
        classpath = sourceSets["functional"].runtimeClasspath
        systemProperty ("tool.dir", toolDir.get().asFile.absolutePath)
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

        files {
            into("tool") {
                from(tool)
            }
        }
    }
}
