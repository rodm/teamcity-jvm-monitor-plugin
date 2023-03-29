
plugins {
    id ("teamcity.agent-plugin")
}

sourceSets {
    create("functional") {
        compileClasspath += sourceSets["main"].output + configurations.testRuntimeClasspath
        runtimeClasspath += sourceSets["main"].output + configurations.testRuntimeClasspath
    }
}

val tool by configurations.creating

dependencies {
    implementation (project(":jvm-monitor-common"))

    testRuntimeOnly (libs.log4j)

    tool (project(":jvm-monitor-tool"))
}

tasks {
    val toolDir = project.layout.buildDirectory.dir("tool")
    register("copyTool", Copy::class) {
        destinationDir = toolDir.get().asFile
        from(tool)
    }

    val javaVersions = listOf(8, 11, 17, 18, 19, 20)
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
