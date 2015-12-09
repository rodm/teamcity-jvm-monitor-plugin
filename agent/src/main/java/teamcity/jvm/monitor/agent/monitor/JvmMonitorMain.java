package teamcity.jvm.monitor.agent.monitor;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class JvmMonitorMain {

    private static final Logger LOGGER = Logger.getLogger(JvmMonitorMain.class);

    public static void main(String[] args) {
        System.setProperty("log.dir", args[0]);
        URL configurationResource = JvmMonitorMain.class.getResource("/teamcity-jvm-monitor-log4j.xml");
        DOMConfigurator.configure(configurationResource);

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
