package teamcity.jvm.monitor.agent.monitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class JvmMonitorMain {

    public static void main(String[] args) {
        BufferedReader reader = null;
        try {
            File outputDir = new File(args[0]);
            System.out.println("output dir=" + outputDir.getCanonicalPath());
            if (!outputDir.exists()) {
                System.err.println("Output directory does not exist");
                return;
            }

            JvmMonitor monitor = new JvmMonitor(outputDir);
            System.out.println("JVM Monitor process started");
            reader = new BufferedReader(new InputStreamReader(System.in));
            boolean run = true;
            while (run) {
                String command = reader.readLine();
                if ("start".equals(command)) {
                    System.out.println("Starting JVM Monitor");
                    monitor.start();
                } else if ("stop".equals(command)) {
                    System.out.println("Stopping JVM Monitor");
                    run = false;
                    monitor.stop();
                }
            }
            System.out.println("JVM Monitor process exiting");
        }
        catch (IOException e) {
            e.printStackTrace(System.err);
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
