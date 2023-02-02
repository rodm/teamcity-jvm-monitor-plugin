
plugins {
    id ("teamcity.agent-plugin")
}

base {
    archivesName.set("jvm-monitor-agent")
}

sourceSets {
    create("functional") {
        compileClasspath += sourceSets["main"].output + configurations.testRuntimeClasspath
        runtimeClasspath += sourceSets["main"].output + configurations.testRuntimeClasspath
    }
}

val tool by configurations.creating

dependencies {
    implementation (project(":common"))

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

    val javaVersions = 7..19
    register("functionalTest", Test::class) {
        group = "verification"
        description = "Runs the functional tests."
        useJUnitPlatform()
        dependsOn(named("copyTool"))
        testClassesDirs = sourceSets["functional"].output.classesDirs
        classpath = sourceSets["functional"].runtimeClasspath
        systemProperty ("tool.dir", toolDir.get().asFile.absolutePath)
        javaVersions.forEach { version ->
            val propertyName = "java${version}.home"
            systemProperty(propertyName, findProperty(propertyName) ?: "")
        }
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
