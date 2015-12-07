package teamcity.jvm.monitor.server;

import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JvmLog {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.SERVER");

    private List<String> contents = new ArrayList<String>();

    public JvmLog() {
        this.contents.add("JVM log file not found");
    }

    public JvmLog(BuildArtifact artifact) {
        BufferedReader reader = null;
        try {
            String line;
            long s = artifact.getSize();
            reader = new BufferedReader(new InputStreamReader(artifact.getInputStream()));
            while ((line = reader.readLine()) != null) {
                contents.add(line);
            }
        } catch (IOException e) {
            LOGGER.warn("Exception reading artifact: " + artifact.getName(), e);
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.warn("Exception closing artifact input stream", e);
                }
            }
        }
    }

    public List<String> getContents() {
        return Collections.unmodifiableList(this.contents);
    }
}
