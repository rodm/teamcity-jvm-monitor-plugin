package teamcity.jvm.monitor.agent;

import org.apache.log4j.Logger;
import teamcity.jvm.monitor.agent.monitor.JvmMonitorMain;

import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class JvmMonitorLauncher {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.AGENT");

    private File logDir;

    private File outputDir;

    private PrintWriter writer;

    private Process process;

    public JvmMonitorLauncher(File logDir, File outputDir) {
        this.logDir = logDir;
        this.outputDir = outputDir;
    }

    public void start() throws Exception {
        LOGGER.info("Starting JVM Monitor process");
        String javaHome = System.getProperty("java.home") + "/..";
        File javaCommand = new File(javaHome, "bin/java");
        File agentJar = new File(JvmMonitorLauncher.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        File toolsJar = new File(javaHome, "lib/tools.jar");
        File log4jJar = new File(agentJar.getParentFile(), "log4j-1.2.17.jar");

        String classPath = toolsJar.getCanonicalPath() + File.pathSeparator +
                agentJar.getCanonicalPath() + File.pathSeparator +
                log4jJar.getCanonicalPath();
        List<String> args = new ArrayList<String>();
        args.add(javaCommand.getAbsolutePath());
        args.add("-cp");
        args.add(classPath);
        args.add(JvmMonitorMain.class.getName());
        args.add(logDir.getCanonicalPath());
        args.add(outputDir.getCanonicalPath());
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(args);
        LOGGER.info("command line: " + builder.toString());

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

    public void stop() throws Exception {
        LOGGER.info("Stopping JVM Monitor process");

        writer.println("stop");
        writer.flush();
        writer.close();

        int exitValue = process.waitFor();
        LOGGER.info("Stopped JVM Monitor process, exit value: " + exitValue);
    }
}
