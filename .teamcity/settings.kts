
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.project
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot.AgentCheckoutPolicy.NO_MIRRORS
import jetbrains.buildServer.configs.kotlin.version

version = "2022.10"

project {
    description = "A TeamCity plugin that collects JVM metrics during a build"

    val vcsId = "JvmMonitor"
    val vcsRoot = GitVcsRoot {
        id(vcsId)
        name = "jvm-monitor"
        url = "https://github.com/rodm/teamcity-jvm-monitor-plugin"
        branch = "refs/heads/main"
        branchSpec = """
            +:refs/heads/(main)
            +:refs/tags/(*)
        """.trimIndent()
        useTagsAsBranches = true
        checkoutPolicy = NO_MIRRORS
    }
    vcsRoot(vcsRoot)

    val buildTemplate = template {
        id("Build")
        name = "Build"

        vcs {
            root(vcsRoot)
        }

        steps {
            gradle {
                id = "RUNNER_11"
                buildFile = "build.gradle.kts"
                tasks = "%gradle.tasks%"
                gradleParams = "%gradle.shared.opts% %gradle.opts%"
                useGradleWrapper = true
                enableStacktrace = true
                jdkHome = "%java.home%"
            }
        }

        triggers {
            vcs {
                id = "vcsTrigger"
                branchFilter = ""
                triggerRules = """
                    -:.github/**
                    -:README.adoc
                """.trimIndent()
            }
        }

        failureConditions {
            executionTimeoutMin = 15
        }

        features {
            feature {
                id = "perfmon"
                type = "perfmon"
            }
        }

        // create java home properties for LTS versions and versions after last LTS
        val javaVersions = listOf(8, 11, 17, 18, 19, 20)
        val sharedOptions = javaVersions.map { version ->
            "-Pjava${version}.home=%java${version}.home%"
        }.toList().joinToString(" ")
        params {
            param("gradle.opts", "")
            param("gradle.shared.opts", sharedOptions)
            param("gradle.tasks", "clean build functionalTest")
            param("java.home", "%java8.home%")
        }
    }

    val build1 = buildType {
        templates(buildTemplate)
        id("Build1")
        name = "Build - TeamCity 2018.1"

        artifactRules = "server/build/distributions/*.zip"

        features {
            feature {
                type = "jvm-monitor-plugin"
            }
        }
    }

    val build2 = buildType {
        templates(buildTemplate)
        id("Build2")
        name = "Build - TeamCity 2022.10"

        params {
            param("gradle.opts", "-Pteamcity.api.version=2022.10")
        }
    }

    val build3 = buildType {
        templates(buildTemplate)
        id("Build3")
        name = "Build - TeamCity 2023.05"

        params {
            param("gradle.opts", "-Pteamcity.api.version=2023.05")
        }
    }

    val reportCodeQuality = buildType {
        templates(buildTemplate)
        id("ReportCodeQuality")
        name = "Report - Code Quality"

        params {
            param("gradle.opts", "%sonar.opts%")
            param("gradle.tasks", "clean build sonarqube")
            param("java.home", "%java8.home%")
        }
    }

    buildTypesOrder = arrayListOf(build1, build2, build3, reportCodeQuality)
}
