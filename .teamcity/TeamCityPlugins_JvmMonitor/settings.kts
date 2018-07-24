package TeamCityPlugins_JvmMonitor

import jetbrains.buildServer.configs.kotlin.v2018_1.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_1.CheckoutMode
import jetbrains.buildServer.configs.kotlin.v2018_1.FailureAction
import jetbrains.buildServer.configs.kotlin.v2018_1.Template
import jetbrains.buildServer.configs.kotlin.v2018_1.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2018_1.project
import jetbrains.buildServer.configs.kotlin.v2018_1.projectFeatures.VersionedSettings
import jetbrains.buildServer.configs.kotlin.v2018_1.projectFeatures.versionedSettings
import jetbrains.buildServer.configs.kotlin.v2018_1.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_1.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2018_1.version

version = "2018.1"
project {
    uuid = "e6a5445f-f6ac-4cba-a488-0d9e6b729e8d"
    id("TeamCityPlugins_JvmMonitor")
    parentId ("TeamCityPlugins")
    name = "JVM Monitor"

    val vcsId = "TeamCityJvmMonitorPlugin_JvmMonitorPlugin"
    val vcsRoot = GitVcsRoot({
        uuid = "0600f2cb-a7a9-4f48-a3d3-61908a9e8f95"
        id(vcsId)
        name = "jvm monitor plugin"
        pollInterval = 3600
        url = "https://github.com/rodm/teamcity-jvm-monitor-plugin"
        useMirrors = false
    })
    vcsRoot(vcsRoot)

    features {
        versionedSettings {
            id = "PROJECT_EXT_8"
            mode = VersionedSettings.Mode.ENABLED
            rootExtId = vcsId
            showChanges = true
            settingsFormat = VersionedSettings.Format.KOTLIN
            buildSettingsMode = VersionedSettings.BuildSettingsMode.PREFER_SETTINGS_FROM_VCS
        }
    }

    val buildTemplate = Template({
        uuid = "bd4f0ea8-d47e-4ba3-8ea5-a77d78f97bad"
        id("TeamCityJvmMonitorPlugin_BuildPlugin")
        name = "build plugin"

        vcs {
            root(vcsRoot)
            checkoutMode = CheckoutMode.ON_SERVER
        }

        steps {
            gradle {
                id = "RUNNER_11"
                tasks = "%gradle.tasks%"
                gradleParams = "%gradle.opts%"
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

        params {
            param("gradle.opts", "-Dteamcity.version=%version%")
            param("gradle.tasks", "clean build")
            param("java.home", "%java8.home%")
            param("system.teamcity.version", "%version%")
            param("version", "%teamcity80.version%")
        }
    })
    template(buildTemplate)

    val build1 = BuildType({
        templates(buildTemplate)
        uuid = "854e9d34-02b3-443f-a648-aec4053a9a79"
        id("TeamCityPlugins_JvmMonitor_Build1")
        name = "Build - TeamCity 10.0"

        artifactRules = "server/build/distributions/*.zip"

        params {
            param("version", "10.0")
        }
    })
    buildType(build1)

    val build2 = BuildType({
        templates(buildTemplate)
        uuid = "55cb3e07-59d7-40e6-b684-eaf82ccbdbcf"
        id("TeamCityPlugins_JvmMonitor_Build2")
        name = "Build - TeamCity 2017.1"

        params {
            param("version", "2017.1")
        }
    })
    buildType(build2)

    val build3 = BuildType({
        templates(buildTemplate)
        uuid = "1566d1b0-8750-41cc-8eb0-2f8e83fff661"
        id("TeamCityPlugins_JvmMonitor_Build3")
        name = "Build - TeamCity 2017.2"

        params {
            param("version", "2017.2")
        }
    })
    buildType(build3)

    val reportCodeQuality = BuildType({
        templates(buildTemplate)
        uuid = "37093bfa-d15a-46a1-acce-8a6a5800d186"
        id("TeamCityJvmMonitorPlugin_ReportCodeQuality")
        name = "Report - Code Quality"

        params {
            param("gradle.opts", "%sonar.opts% -Dteamcity.version=%version%")
            param("gradle.tasks", "clean build sonarqube")
            param("java.home", "%java8.home%")
            param("version", "10.0")
        }
    })
    buildType(reportCodeQuality)

    val publishTemplate = Template({
        uuid = "7dcda1c4-21a7-4fc3-9ea9-62cfbb6c53da"
        id("TeamCityJvmMonitorPlugin_PublishPlugin")
        name = "publish plugin"

        vcs {
            root(vcsRoot)
            checkoutMode = CheckoutMode.ON_SERVER
        }

        steps {
            gradle {
                id = "RUNNER_13"
                tasks = "bintrayUpload"
                gradleParams = "%gradle.opts%"
                useGradleWrapper = true
                enableStacktrace = true
                jdkHome = "%java.home%"
            }
        }

        failureConditions {
            executionTimeoutMin = 5
        }

        features {
            feature {
                id = "perfmon"
                type = "perfmon"
            }
        }

        dependencies {
            dependency(build1) {
                snapshot {
                    onDependencyFailure = FailureAction.FAIL_TO_START
                }

                artifacts {
                    id = "ARTIFACT_DEPENDENCY_2"
                    cleanDestination = true
                    artifactRules = "jvm-monitor-1.0-SNAPSHOT.zip => server/build/distributions"
                }
            }
        }

        params {
            param("gradle.opts", "")
            param("java.home", "%java8.home%")
            param("version", "%teamcity80.version%")
        }
    })
    template(publishTemplate)

    val publishToBintray = BuildType({
        templates(publishTemplate)
        uuid = "99e12728-996d-4bf9-b71c-7601172e0a1a"
        id("TeamCityJvmMonitorPlugin_PublishToBintray")
        name = "Publish to Bintray"

        params {
            param("gradle.opts", "")
            param("system.teamcity.version", "%version%")
            param("system.version", "1.0-b%dep.TeamCityPlugins_JvmMonitor_Build1.build.number%")
        }
    })
    buildType(publishToBintray)

    buildTypesOrder = arrayListOf(build1, build2, build3, reportCodeQuality, publishToBintray)
}
