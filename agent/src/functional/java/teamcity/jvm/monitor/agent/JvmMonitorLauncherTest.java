package teamcity.jvm.monitor.agent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

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
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(TempDirectory.class)
class JvmMonitorLauncherTest {

    @Test
    void monitorJavaProcess(@TempDir Path logPath, @TempDir Path outputPath) throws Exception {
        File logDir = logPath.toFile();
        File outputDir = outputPath.toFile();

        JvmMonitorLauncher launcher = new JvmMonitorLauncher(logDir, outputDir);
        launcher.start();

        String javaHome = System.getProperty("java.home");
        String java = new File(javaHome, "bin/java").getAbsolutePath();
        String className = TestMain.class.getName();
        String classPath = System.getProperty("java.class.path");
        int result = exec(java, "-Xmx32m", "-classpath", classPath, className);

        launcher.stop();

        assertEquals(0, result, "Expected test Java process to run");

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
        assertThat(lines, hasItem(containsString("jvm version: " + "1.8")));

        List<String> dataLines = lines.stream().filter(line -> !line.startsWith("#")).collect(Collectors.toList());
        assertThat(dataLines.size(), greaterThanOrEqualTo(4));
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