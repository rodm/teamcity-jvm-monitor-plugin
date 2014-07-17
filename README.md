# JVM Monitor Plugin for TeamCity

The JVM Monitor plugin is a Build Feature that can be added to a build configuration to record the garbage collection metrics for any JVM running during the build.

## How to install

Download the plugin archive from [Bintray](https://bintray.com/rodm/teamcity-plugins/teamcity-jvm-monitor-plugin) and copy the `teamcity-jvm-monitor-plugin.zip` file to the `.BuildServer/plugins` directory and restart the TeamCity server.

## How to use the plugin

* Edit a build configuration
* Select the Build Features page a click the 'Add build feature' button.
* Select 'JVM Monitor' from the list of features.

When a build runs the garbage collection metrics are recorded into a text file for each process. These files are then
uploaded as build artifacts and can be found as hidden artifacts under the Artifacts tab in the jvmmon directory. 

## How to build the plugin

1. [Download](http://www.jetbrains.com/teamcity/download/index.html) and install TeamCity version 7.1 or later.
2. Copy the `example.build.properties` file to `build.properties`
3. Edit the `build.properties` file to set the properties teamcity.home, teamcity.version and teamcity.java.home
4. Run the Ant build, the default is to build and package the plugin, the plugin is output to `dist/jvm-monitor-plugin.zip`

The Ant build script provides a target to deploy the plugin to a local configuration directory, deploy-plugin. The
TeamCity server can be started using the start-teamcity-server target. The TEAMCITY_DATA_PATH is set by default to use
a local directory and not the `~/.BuildServer` directory.
