package teamcity.jvm.monitor.agent;

import jetbrains.buildServer.agent.AgentBuildFeature;
import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.util.EventDispatcher;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import teamcity.jvm.monitor.JvmMonitorPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class JvmMonitorBuildFeature extends AgentLifeCycleAdapter {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.AGENT");

    private final ArtifactsWatcher artifactsWatcher;

    private JvmMonitorLauncher monitor;

    private File outputDir;

    public JvmMonitorBuildFeature(ArtifactsWatcher artifactsWatcher, EventDispatcher<AgentLifeCycleAdapter> eventDispatcher) {
        this.artifactsWatcher = artifactsWatcher;
        eventDispatcher.addListener(this);
    }

    @Override
    public void buildStarted(@NotNull AgentRunningBuild build) {
        Collection<AgentBuildFeature> features = build.getBuildFeaturesOfType(JvmMonitorPlugin.FEATURE_TYPE);
        if (!features.isEmpty()) {
            LOGGER.info("jvm-monitor-plugin feature enabled for build");

            BuildAgentConfiguration config = build.getAgentConfiguration();
            outputDir = new File(config.getTempDirectory(), "jvmmon");
            try {
                FileUtils.deleteDirectory(outputDir);
            }
            catch (IOException e) {
                // ignore
            }
            boolean result = outputDir.mkdirs();
            if (!result) {
                LOGGER.warn("Failed to create output directory");
            }
            monitor = new JvmMonitorLauncher(config.getAgentLogsDirectory(), outputDir);
            try {
                monitor.start();
            }
            catch (Exception e) {
                LOGGER.warn("Start monitor failed", e);
            }
        }
    }

    @Override
    public void beforeBuildFinish(@NotNull AgentRunningBuild build, @NotNull BuildFinishedStatus buildStatus) {
        if (monitor != null) {
            try {
                monitor.stop();
            } catch (Exception e) {
                LOGGER.warn("Stop monitor failed", e);
            }
            monitor = null;
            artifactsWatcher.addNewArtifactsPath(outputDir.getAbsolutePath() + "=>" + ".teamcity/jvmmon/");
        }
    }
}
