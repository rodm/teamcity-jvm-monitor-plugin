package TeamCityPlugins_JvmMonitor

import jetbrains.buildServer.configs.kotlin.v2017_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2017_2.CheckoutMode
import jetbrains.buildServer.configs.kotlin.v2017_2.FailureAction
import jetbrains.buildServer.configs.kotlin.v2017_2.Template
import jetbrains.buildServer.configs.kotlin.v2017_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2017_2.project
import jetbrains.buildServer.configs.kotlin.v2017_2.projectFeatures.VersionedSettings
import jetbrains.buildServer.configs.kotlin.v2017_2.projectFeatures.versionedSettings
import jetbrains.buildServer.configs.kotlin.v2017_2.triggers.ScheduleTrigger
import jetbrains.buildServer.configs.kotlin.v2017_2.triggers.schedule
import jetbrains.buildServer.configs.kotlin.v2017_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2017_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2017_2.version

version = "2017.2"
project {
    uuid = "e6a5445f-f6ac-4cba-a488-0d9e6b729e8d"
    id = "TeamCityPlugins_JvmMonitor"
    parentId = "TeamCityPlugins"
    name = "JVM Monitor"

    val vcsId = "TeamCityJvmMonitorPlugin_JvmMonitorPlugin"
    val vcsRoot = GitVcsRoot({
        uuid = "0600f2cb-a7a9-4f48-a3d3-61908a9e8f95"
        id = vcsId
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
        id = "TeamCityJvmMonitorPlugin_BuildPlugin"
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
            param("gradle.opts", "")
            param("gradle.tasks", "clean build")
            param("java.home", "%java8.home%")
            param("system.teamcity.version", "%version%")
            param("version", "%teamcity80.version%")
        }
    })
    template(buildTemplate)

    val build81 = BuildType({
        template(buildTemplate)
        uuid = "854e9d34-02b3-443f-a648-aec4053a9a79"
        id = "TeamCityJvmMonitorPlugin_BuildTeamCity81"
        name = "Build - TeamCity 8.1"

        params {
            param("gradle.opts", "-Dteamcity.version=%version%")
            param("version", "%teamcity81.version%")
        }

        disableSettings("RUNNER_5")
    })
    buildType(build81)

    val build100 = BuildType({
        template(buildTemplate)
        uuid = "55cb3e07-59d7-40e6-b684-eaf82ccbdbcf"
        id = "TeamCityJvmMonitorPlugin_BuildTeamCity100"
        name = "Build - TeamCity 10.0"

        params {
            param("gradle.opts", "-Dteamcity.version=%version%")
            param("version", "%teamcity100.version%")
        }

        disableSettings("RUNNER_5")
    })
    buildType(build100)

    val build20171 = BuildType({
        template(buildTemplate)
        uuid = "1566d1b0-8750-41cc-8eb0-2f8e83fff661"
        id = "TeamCityJvmMonitorPlugin_BuildTeamCity20171"
        name = "Build - TeamCity 2017.1"

        artifactRules = "build/distributions/*.zip"

        params {
            param("gradle.opts", "-Dteamcity.version=%version%")
            param("version", "2017.1")
        }
    })
    buildType(build20171)

    val reportCodeQuality = BuildType({
        template(buildTemplate)
        uuid = "37093bfa-d15a-46a1-acce-8a6a5800d186"
        id = "TeamCityJvmMonitorPlugin_ReportCodeQuality"
        name = "Report - Code Quality"

        triggers {
            schedule {
                id = "TRIGGER_13"
                schedulingPolicy = weekly {
                    dayOfWeek = ScheduleTrigger.DAY.Saturday
                    hour = 11
                    minute = 25
                }
                branchFilter = ""
                triggerBuild = always()
            }
        }

        params {
            param("gradle.opts", "%sonar.opts% -Dteamcity.version=%version%")
            param("gradle.tasks", "clean build sonarqube")
            param("java.home", "%java8.home%")
            param("version", "%teamcity81.version%")
        }

        disableSettings("RUNNER_5", "vcsTrigger")
    })
    buildType(reportCodeQuality)

    val publishTemplate = Template({
        uuid = "7dcda1c4-21a7-4fc3-9ea9-62cfbb6c53da"
        id = "TeamCityJvmMonitorPlugin_PublishPlugin"
        name = "publish plugin"

        vcs {
            root(vcsRoot)
            checkoutMode = CheckoutMode.ON_SERVER
        }

        steps {
            gradle {
                id = "RUNNER_13"
                tasks = "publish"
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
            dependency(build20171) {
                snapshot {
                    onDependencyFailure = FailureAction.FAIL_TO_START
                }

                artifacts {
                    id = "ARTIFACT_DEPENDENCY_2"
                    cleanDestination = true
                    artifactRules = "teamcity-jvm-monitor-plugin-1.0-SNAPSHOT.zip => build/distributions"
                }
            }
        }

        params {
            param("gradle.opts", "-x build -x jar -x serverPlugin -PrepositoryUrl=%repository.url% -PrepositoryUsername=%repository.user% -PrepositoryPassword=%repository.password%")
            param("java.home", "%java7.home%")
            param("repository.password", "")
            param("repository.url", "")
            param("repository.user", "")
            param("version", "%teamcity80.version%")
        }
    })
    template(publishTemplate)

    val publishToBintray = BuildType({
        template(publishTemplate)
        uuid = "99e12728-996d-4bf9-b71c-7601172e0a1a"
        id = "TeamCityJvmMonitorPlugin_PublishToBintray"
        name = "Publish to Bintray"

        params {
            param("gradle.opts", """
            -x build -x jar -x serverPlugin
            -Dversion=%system.version% -PrepositoryUrl=%repository.url% -PrepositoryUsername=%repository.user% -PrepositoryPassword=%repository.password%
        """.trimIndent())
            param("repository.password", "%bintray.repository.password%")
            param("repository.url", "%bintray.repository.url%teamcity-jvm-monitor-plugin")
            param("repository.user", "%bintray.repository.user%")
            param("system.teamcity.version", "%version%")
            param("system.version", "1.0-b%dep.TeamCityJvmMonitorPlugin_BuildTeamCity20171.build.number%")
        }
    })
    buildType(publishToBintray)

    buildTypesOrder = arrayListOf(build81, build100, build20171, reportCodeQuality, publishToBintray)
}
