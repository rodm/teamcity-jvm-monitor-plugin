/*
 * Copyright 2018 Rod MacKenzie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package teamcity.jvm.monitor.agent;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JvmMonitorLauncher {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.AGENT");
    private static final String JVM_MONITOR_TOOL_CLASS = "teamcity.jvm.monitor.tool.JvmMonitorTool";
    private static final String EXPORT_MONITOR_PACKAGE = "--add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED";
    private static final String EXPORT_EVENT_PACKAGE = "--add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor.event=ALL-UNNAMED";

    private static final String LOG_DIR_FORMAT = "-Dlog.dir=%s";
    private static final String LOG_CONFIG_FORMAT = "-Dlog4j2.configurationFile=jar:file:%s!/teamcity-jvm-monitor-log4j2.xml";

    private final File toolDir;
    private final File outputDir;

    private String javaHome;
    private JvmMonitorConnector connector;
    private Process process;

    public JvmMonitorLauncher(File toolDir, File outputDir) {
        this.toolDir = toolDir;
        this.outputDir = outputDir;
    }

    public void start() throws IOException, InterruptedException {
        LOGGER.info("Starting JVM Monitor process");
        connector = JvmMonitorConnector.createConnector();

        List<String> commandLine = new ArrayList<>();
        commandLine.add(getJavaCommand());
        if (isJava9OrLater()) {
            commandLine.add(EXPORT_MONITOR_PACKAGE);
            commandLine.add(EXPORT_EVENT_PACKAGE);
        }
        commandLine.add("-cp");
        commandLine.add(getClassPath());
        commandLine.add(String.format(LOG_DIR_FORMAT, outputDir.getAbsolutePath()));
        commandLine.add(String.format(LOG_CONFIG_FORMAT, getMonitorToolJarPath()));
        commandLine.add(JVM_MONITOR_TOOL_CLASS);
        commandLine.add("" + connector.getPort());
        commandLine.add(outputDir.getCanonicalPath());
        LOGGER.info("JVM Monitor command line: " + commandLine);

        ProcessBuilder builder = new ProcessBuilder()
            .command(commandLine)
            .inheritIO();
        connector.ready();
        process = builder.start();
        connector.startMonitor();
        LOGGER.info("JVM Monitor process started");
    }

    public void stop() throws IOException, InterruptedException {
        LOGGER.info("Stopping JVM Monitor process");
        connector.stopMonitor();
        int exitValue = process.waitFor();
        LOGGER.info("JVM Monitor process stopped, exit value: " + exitValue);
    }

    private String getJavaCommand() {
        return new File(getJavaHome(), "bin/java").getAbsolutePath();
    }

    private String getClassPath() throws IOException {
        File toolsJar = new File(getJavaHome(), "lib/tools.jar");
        List<String> classPath = new ArrayList<>();
        if (toolsJar.exists()) {
            classPath.add(toolsJar.getCanonicalPath());
        }
        File[] toolFiles = toolDir.listFiles();
        if (toolFiles != null) {
            for (File file : toolFiles) {
                classPath.add(file.getCanonicalPath());
            }
        }
        return String.join(File.pathSeparator, classPath);
    }

    private String getMonitorToolJarPath() {
        return new File(toolDir, "jvm-monitor-tool.jar").getAbsolutePath();
    }

    void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    private File getJavaHome() {
        if (javaHome == null) {
            javaHome = System.getProperty("java.home");
        }
        File javaHomeFile = new File(javaHome);
        if ("jre".equals(javaHomeFile.getName())) {
            return javaHomeFile.getParentFile();
        } else {
            return javaHomeFile;
        }
    }

    private boolean isJava9OrLater() {
        Path modulesFile = getJavaHome().toPath().resolve("lib/modules");
        return Files.exists(modulesFile);
    }
}
