
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.CheckoutMode
import jetbrains.buildServer.configs.kotlin.v2018_2.FailureAction
import jetbrains.buildServer.configs.kotlin.v2018_2.Template
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2018_2.project
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2018_2.version
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.VcsTrigger.QuietPeriodMode.USE_DEFAULT

version = "2019.1"

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
        useMirrors = false
    }
    vcsRoot(vcsRoot)

/*
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
*/

    val buildTemplate = Template {
        id("Build")
        name = "Build"

        vcs {
            root(vcsRoot)
            checkoutMode = CheckoutMode.ON_SERVER
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

        // create java home properties for versions 7 to 12
        val sharedOptions = IntRange(7, 12).map{ "-Pjava${it}.home=%java${it}.home%" }.toList().joinToString(" ")
        params {
            param("gradle.opts", "")
            param("gradle.shared.opts", sharedOptions)
            param("gradle.tasks", "clean build functionalTest")
            param("java.home", "%java8.home%")
        }
    }
    template(buildTemplate)

    val build1 = BuildType {
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
    buildType(build1)

    val build2 = BuildType {
        templates(buildTemplate)
        id("Build2")
        name = "Build - TeamCity 2018.2"

        params {
            param("gradle.opts", "-Pteamcity.api.version=2018.2")
        }
    }
    buildType(build2)

    val build3 = BuildType {
        templates(buildTemplate)
        id("Build3")
        name = "Build - TeamCity 2019.1"

        params {
            param("gradle.opts", "-Pteamcity.api.version=2019.1")
        }
    }
    buildType(build3)

    val reportCodeQuality = BuildType {
        templates(buildTemplate)
        id("ReportCodeQuality")
        name = "Report - Code Quality"

        params {
            param("gradle.opts", "%sonar.opts%")
            param("gradle.tasks", "clean build sonarqube")
            param("java.home", "%java8.home%")
        }
    }
    buildType(reportCodeQuality)

    val publishTemplate = Template {
        id("Publish")
        name = "Publish"

        vcs {
            root(vcsRoot)
            checkoutMode = CheckoutMode.ON_SERVER
        }

        steps {
            gradle {
                id = "RUNNER_13"
                buildFile = "build.gradle.kts"
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
                    artifactRules = "jvm-monitor-*.zip => server/build/distributions"
                }
            }
        }

        params {
            param("gradle.opts", "")
            param("java.home", "%java8.home%")
        }
    }
    template(publishTemplate)

    val publishToBintray = BuildType {
        templates(publishTemplate)
        id("PublishToBintray")
        name = "Publish to Bintray"

        params {
            param("gradle.opts", "%bintray.opts%")
            param("gradle.tasks", "bintrayUpload")
        }

        triggers {
            vcs {
                quietPeriodMode = USE_DEFAULT
                branchFilter = "+:v*"
            }
        }
    }
    buildType(publishToBintray)

    buildTypesOrder = arrayListOf(build1, build2, build3, reportCodeQuality, publishToBintray)
}
