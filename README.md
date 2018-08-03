# JVM Monitor Plugin for TeamCity

The JVM Monitor plugin is a Build Feature that can be added to a build configuration to record the garbage collection metrics for any JVM running during the build.

[![Build Status](https://travis-ci.org/rodm/teamcity-jvm-monitor-plugin.svg?branch=master)](https://travis-ci.org/rodm/teamcity-jvm-monitor-plugin)
[![Download](https://api.bintray.com/packages/rodm/teamcity-plugins/teamcity-jvm-monitor-plugin/images/download.svg)](https://bintray.com/rodm/teamcity-plugins/teamcity-jvm-monitor-plugin/_latestVersion)

## How to install

Download the plugin using the link above and follow the instructions from the TeamCity documentation, [Installing Additional Plugins](https://confluence.jetbrains.com/display/TCD9/Installing+Additional+Plugins)

## How to use the plugin

* Edit a build configuration
* Select the Build Features page a click the 'Add build feature' button.
* Select 'JVM Monitor' from the list of features.

When a build runs the garbage collection metrics are recorded into a text file for each Java process. These files are
uploaded as build artifacts and presented on the JVMMon tab of the build results page.

## How to build the plugin

* To build and package the plugin run `./gradlew build`, the plugin is output to the `build/distributions` directory.
* To deploy and test the plugin the following commands can be used to download and install a server, deploy the plugin and start the server.
  * Download and install a TeamCity server run `./gradlew installTeamCity`
  * Deploy the plugin run `./gradlew deployPlugin`
  * Start the server run `./gradlew startServer`
  * Start the build agent run `./gradlew startAgent`

## Compatibility

The plugin is compatible with TeamCity 10.0 and later and requires the build agent to be running on Java 7 or later.

## License

This plugin is available under the http://www.apache.org/licenses/LICENSE-2.0.html[Apache License, Version 2.0].
