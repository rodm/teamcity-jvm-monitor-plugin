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

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JvmMonitorLauncherTest {

    private File toolDir;
    private File outputDir;

    @BeforeEach
    void init(@TempDir Path outputPath) {
        outputDir = outputPath.toFile();
        toolDir = Paths.get(System.getProperty("tool.dir", "")).toFile();
    }

    @ParameterizedTest(name = "monitor process running on Java {1}")
    @CsvSource(value = {
        "java8.home, 1.8", "java11.home, 11", "java17.home, 17", "java21.home, 21", "java22.home, 22"
    })
    void monitorJavaProcessOnJava(String homeProperty, String version) throws Exception {
        String javaHome = System.getProperty(homeProperty, "");
        assumeFalse(javaHome.trim().isEmpty(), "The property '" + javaHome + "' should not be empty");
        File javaBinary = new File(javaHome, "bin/java");
        assumeTrue(javaBinary.exists(), "The path set for the '" + javaHome + "' property is not a valid Java install");

        monitorTestApp(System.getProperty("java.home"), javaHome);

        String className = TestMain.class.getName();
        File[] files = outputDir.listFiles();
        assertNotNull(files, "Expected JVM metrics files in output directory");
        assertNotEquals(0, files.length, "Expected JVM metrics files in output directory");
        File metrics = getMetricsFileFor(className, files);
        assertNotNull(metrics, "Expected JVM metrics file for " + className + " process");

        List<String> lines = Files.readAllLines(metrics.toPath());
        assertThat(lines, hasItem(containsString("main class: " + className)));
        assertThat(lines, hasItem(containsString("jvm version: " + version)));

        List<String> dataLines = lines.stream().filter(line -> !line.startsWith("#")).collect(Collectors.toList());
        assertThat(dataLines.size(), greaterThanOrEqualTo(4));
        assertThat(dataLines, not(hasItem(startsWith("#"))));
    }

    @ParameterizedTest(name = "launch jvm monitor running on Java {1}")
    @CsvSource(value = {
        "java8.home, 1.8", "java11.home, 11", "java17.home, 17", "java21.home, 21", "java22.home, 22"
    })
    void launchJvmMonitorOnJava(String homeProperty, String version) throws Exception {
        String javaHome = System.getProperty(homeProperty, "");
        assumeFalse(javaHome.trim().isEmpty(), "The property '" + javaHome + "' should not be empty");
        File javaCommand = new File(javaHome, "bin/java");
        assumeTrue(javaCommand.exists(), "The path set for the '" + javaHome + "' property is not a valid Java install");

        monitorTestApp(javaHome, System.getProperty("java.home"));

        String className = TestMain.class.getName();
        File[] files = outputDir.listFiles();
        assertNotNull(files, "Expected JVM metrics files in output directory");
        assertNotEquals(0, files.length, "Expected JVM metrics files in output directory");
        File metrics = getMetricsFileFor(className, files);
        assertNotNull(metrics, "Expected JVM metrics file for " + className + " process");
    }

    @Test
    void monitorToolOutputsLogFile() throws Exception {
        String javaHome = System.getProperty("java.home");
        monitorTestApp(javaHome, javaHome);

        Path monitorLogPath = outputDir.toPath().resolve("jvm-monitor.log");
        assertTrue(Files.exists(monitorLogPath));

        List<String> lines = Files.readAllLines(monitorLogPath);
        assertThat(lines, hasItem(containsString("Starting JVM Monitor")));
        assertThat(lines, hasItem(containsString("Stopping JVM Monitor")));
    }

    private void monitorTestApp(String monitorJavaHome, String appJavaHome) throws Exception {
        File javaCommand = new File(appJavaHome, "bin/java");
        String java = javaCommand.getAbsolutePath();
        String classPath = System.getProperty("java.class.path");
        String className = TestMain.class.getName();

        JvmMonitorLauncher launcher = new JvmMonitorLauncher(toolDir, outputDir);
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

        ProcessBuilder builder = new ProcessBuilder()
            .command(commandLine)
            .inheritIO();

        Process process = builder.start();
        return process.waitFor();
    }

    @Nullable
    private static File getMetricsFileFor(String className, File[] files) {
        return Arrays.stream(files)
            .filter(file -> file.getName().contains(className))
            .findFirst()
            .orElse(null);
    }
}
