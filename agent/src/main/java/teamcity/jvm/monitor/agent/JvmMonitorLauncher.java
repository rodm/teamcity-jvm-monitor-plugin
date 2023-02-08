/*
 * Copyright 2018 Rod MacKenzie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package teamcity.jvm.monitor.agent;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JvmMonitorLauncher {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.AGENT");
    private static final String JVM_MONITOR_TOOL_CLASS = "teamcity.jvm.monitor.tool.JvmMonitorTool";
    private static final String EXPORT_MONITOR_PACKAGE = "--add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED";
    private static final String EXPORT_EVENT_PACKAGE = "--add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor.event=ALL-UNNAMED";

    private final File toolDir;
    private final File logDir;
    private final File outputDir;

    private String javaHome;
    private PrintWriter writer;
    private Process process;

    public JvmMonitorLauncher(File toolDir, File logDir, File outputDir) {
        this.toolDir = toolDir;
        this.logDir = logDir;
        this.outputDir = outputDir;
    }

    public void start() throws IOException {
        LOGGER.info("Starting JVM Monitor process");
        File javaHomeFile = getJavaHome();
        File javaCommand = new File(javaHomeFile, "bin/java");
        File toolsJar = new File(javaHomeFile, "lib/tools.jar");

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
        List<String> commandLine = new ArrayList<>();
        commandLine.add(javaCommand.getAbsolutePath());
        if (isJava9OrLater()) {
            commandLine.add(EXPORT_MONITOR_PACKAGE);
            commandLine.add(EXPORT_EVENT_PACKAGE);
        }
        commandLine.add("-cp");
        commandLine.add(String.join(File.pathSeparator, classPath));
        commandLine.add(JVM_MONITOR_TOOL_CLASS);
        commandLine.add(logDir.getCanonicalPath());
        commandLine.add(outputDir.getCanonicalPath());
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(commandLine);
        LOGGER.info("JVM Monitor command line: " + commandLine);

        process = builder.start();
        OutputStream in = process.getOutputStream();
        InputStream out = process.getInputStream();
        InputStream err = process.getErrorStream();

        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream(pos);
        writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(pos)));

        Thread stdinThread = new Thread(new StreamPumper(pis, in));
        Thread stdoutThread = new Thread(new StreamPumper(out, System.out));
        Thread stderrThread = new Thread(new StreamPumper(err, System.err));
        stdinThread.start();
        stdoutThread.start();
        stderrThread.start();

        writer.println("start");
        writer.flush();
        LOGGER.info("Started JVM Monitor process");
    }

    public void stop() throws InterruptedException {
        LOGGER.info("Stopping JVM Monitor process");

        writer.println("stop");
        writer.flush();
        writer.close();

        int exitValue = process.waitFor();
        LOGGER.info("Stopped JVM Monitor process, exit value: " + exitValue);
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
