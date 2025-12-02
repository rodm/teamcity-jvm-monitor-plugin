
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
    register<Copy>("copyTool") {
        destinationDir = toolDir.get().asFile
        from(tool)
    }

    val javaVersions = listOf(8, 11, 17, 21, 22, 23, 24, 25)
    register<Test>("functionalTest") {
        group = "verification"
        description = "Runs the functional tests."
        useJUnitPlatform()
        dependsOn(named("copyTool"))
        testClassesDirs = functional.output.classesDirs
        classpath = functional.runtimeClasspath
        systemProperty ("tool.dir", toolDir.get().asFile.absolutePath)
        systemProperty ("java.versions", javaVersions.joinToString())
        javaVersions.forEach { version ->
            val launcher = project.javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(version))
            }
            val javaHome = launcher.get().metadata.installationPath.toString()
            systemProperty ("java${version}.home", javaHome)
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
