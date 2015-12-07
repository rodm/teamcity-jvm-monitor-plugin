package teamcity.jvm.monitor.agent.monitor;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class JvmMonitorMain {

    public static final Logger LOGGER = Logger.getLogger("JvmMonitor");

    public static void main(String[] args) {
        File logDir = new File(args[0]);
        FileAppender appender = new FileAppender();
        appender.setName("JvmMonitorLogger");
        appender.setFile(logDir + File.separator + "jvm-monitor.log");
        appender.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
        appender.setThreshold(Level.ALL);
        appender.activateOptions();
        LOGGER.addAppender(appender);
        LOGGER.setLevel(Level.DEBUG);

        BufferedReader reader = null;
        try {
            File outputDir = new File(args[1]);
            LOGGER.info("Output directory: " + outputDir.getCanonicalPath());
            if (!outputDir.exists()) {
                LOGGER.warn("Output directory does not exist. JVM Monitor exiting.");
                return;
            }

            JvmMonitor monitor = new JvmMonitor(outputDir);
            LOGGER.info("JVM Monitor process started");
            reader = new BufferedReader(new InputStreamReader(System.in));
            boolean run = true;
            while (run) {
                String command = reader.readLine();
                if ("start".equals(command)) {
                    LOGGER.info("Starting JVM Monitor");
                    monitor.start();
                } else if ("stop".equals(command)) {
                    LOGGER.info("Stopping JVM Monitor");
                    run = false;
                    monitor.stop();
                }
            }
            LOGGER.info("JVM Monitor process exiting");
        }
        catch (IOException e) {
            LOGGER.warn("", e);
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}
