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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JvmMonitorLauncherTest {

    private File logDir;
    private File outputDir;

    @BeforeEach
    void init(@TempDir Path logPath, @TempDir Path outputPath) {
        logDir = logPath.toFile();
        outputDir = outputPath.toFile();
    }

    @ParameterizedTest(name = "monitor process running on Java {1}")
    @CsvSource({
        "java7.home , 1.7", "java8.home , 1.8",
        "java9.home , 9", "java10.home , 10", "java11.home, 11", "java12.home, 12",
        "java13.home , 13", "java14.home, 14", "java15.home, 15"
    })
    void monitorJavaProcessOnJava(String homeProperty, String version) throws Exception {
        String javaHome = System.getProperty(homeProperty, "");
        assumeFalse(javaHome.trim().isEmpty(), "The property '" + javaHome + "' should not be empty");
        File javaBinary = new File(javaHome, "bin/java");
        assumeTrue(javaBinary.exists(), "The path set for the '" + javaHome + "' property is not a valid Java install");

        monitorTestApp(System.getProperty("java.home"), javaHome);

        String className = TestMain.class.getName();
        File[] files = outputDir.listFiles();
        if (files == null || files.length == 0) {
            fail("Expected JVM metrics file for " + className + " process");
        }
        File metrics = null;
        for (File file : files) {
            if (file.getName().contains(className)) {
                metrics = file;
            }
        }
        if (metrics == null) {
            fail("Expected JVM metrics file for " + className + " process");
        }
        List<String> lines = Files.readAllLines(metrics.toPath());

        assertThat(lines, hasItem(containsString("main class: " + className)));
        assertThat(lines, hasItem(containsString("jvm version: " + version)));

        List<String> dataLines = new ArrayList<>();
        for (String line : lines) {
            if (!line.startsWith("#")) dataLines.add(line);
        }
        assertThat(dataLines.size(), greaterThanOrEqualTo(4));
    }

    @ParameterizedTest(name = "launch jvm monitor running on Java {0}")
    @ValueSource(strings = {
        "java7.home", "java8.home", "java9.home", "java10.home", "java11.home",
        "java12.home", "java13.home", "java14.home", "java15.home"
    })
    void launchJvmMonitorOnJava(String homeProperty) throws Exception {
        String javaHome = System.getProperty(homeProperty, "");
        assumeFalse(javaHome.trim().isEmpty(), "The property '" + javaHome + "' should not be empty");
        File javaCommand = new File(javaHome, "bin/java");
        assumeTrue(javaCommand.exists(), "The path set for the '" + javaHome + "' property is not a valid Java install");

        monitorTestApp(javaHome, System.getProperty("java.home"));

        String className = TestMain.class.getName();
        File[] files = outputDir.listFiles();
        assertNotNull(files, "Expected JVM metrics file for " + className + " process");
        assertNotEquals(0, files.length, "Expected JVM metrics file for " + className + " process");

        File metrics = null;
        for (File file : files) {
            if (file.getName().contains(className)) {
                metrics = file;
            }
        }
        assertNotNull(metrics, "Expected JVM metrics file for " + className + " process");
    }

    private void monitorTestApp(String monitorJavaHome, String appJavaHome) throws Exception {
        File javaCommand = new File(appJavaHome, "bin/java");
        String java = javaCommand.getAbsolutePath();
        String classPath = System.getProperty("java.class.path");
        String className = TestMain.class.getName();

        JvmMonitorLauncher launcher = new JvmMonitorLauncher(logDir, outputDir);
        launcher.setJavaHome(monitorJavaHome);
        try {
            launcher.start();

            int result = exec(java, "-Xmx32m", "-classpath", classPath, className);

            assertEquals(0, result, "Expected test Java process to run");
        }
        finally {
            launcher.stop();
        }
    }

    private int exec(String command, String... args) throws Exception {
        List<String> commandLine = new ArrayList<>();
        commandLine.add(command);
        Collections.addAll(commandLine, args);

        ProcessBuilder builder = new ProcessBuilder();
        builder.command(commandLine);

        Process process = builder.start();
        OutputStream in = process.getOutputStream();
        InputStream out = process.getInputStream();
        InputStream err = process.getErrorStream();

        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream(pos);

        Thread stdinThread = new Thread(new StreamPumper(pis, in));
        Thread stdoutThread = new Thread(new StreamPumper(out, System.out));
        Thread stderrThread = new Thread(new StreamPumper(err, System.err));
        stdinThread.start();
        stdoutThread.start();
        stderrThread.start();
        return process.waitFor();
    }
}
