
plugins {
    id ("teamcity.agent-plugin")
}

val functional by sourceSets.creating
val tool by configurations.creating

configurations["functionalImplementation"].extendsFrom(configurations["testImplementation"])
configurations["functionalRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

dependencies {
    implementation (project(":jvm-monitor-common"))

    testRuntimeOnly (libs.log4j)

    "functionalImplementation" (project)

    tool (project(":jvm-monitor-tool"))
}

tasks {
    val toolDir = project.layout.buildDirectory.dir("tool")
    register("copyTool", Copy::class) {
        destinationDir = toolDir.get().asFile
        from(tool)
    }

    val javaVersions = listOf(8, 11, 17, 18, 19, 20, 21, 22)
    register("functionalTest", Test::class) {
        group = "verification"
        description = "Runs the functional tests."
        useJUnitPlatform()
        dependsOn(named("copyTool"))
        testClassesDirs = functional.output.classesDirs
        classpath = functional.runtimeClasspath
        systemProperty ("tool.dir", toolDir.get().asFile.absolutePath)
        javaVersions.forEach { version ->
            val propertyName = "java${version}.home"
            systemProperty(propertyName, findProperty(propertyName) ?: "")
        }
    }
}

teamcity {
    agent {
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
