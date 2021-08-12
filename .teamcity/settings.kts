
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2018_2.project
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot.AgentCheckoutPolicy.NO_MIRRORS
import jetbrains.buildServer.configs.kotlin.v2018_2.version

version = "2020.2"

project {
    description = "A TeamCity plugin that collects JVM metrics during a build"

    val vcsId = "JvmMonitor"
    val vcsRoot = GitVcsRoot {
        id(vcsId)
        name = "jvm-monitor"
        url = "https://github.com/rodm/teamcity-jvm-monitor-plugin"
        branchSpec = """
            +:refs/heads/(master)
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

        // create java home properties for versions 7 to 15
        val sharedOptions = IntRange(7, 15).map{ "-Pjava${it}.home=%java${it}.home%" }.toList().joinToString(" ")
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
        name = "Build - TeamCity 2020.2"

        params {
            param("gradle.opts", "-Pteamcity.api.version=2020.2")
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

    buildTypesOrder = arrayListOf(build1, build2, reportCodeQuality)
}
