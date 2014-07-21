package teamcity.jvm.monitor.agent;

import jetbrains.buildServer.agent.AgentBuildFeature;
import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.EventDispatcher;

import java.io.File;
import java.util.Collection;

public class JvmMonitorBuildFeature extends AgentLifeCycleAdapter {

    private final ArtifactsWatcher artifactsWatcher;

    private JvmMonitorLauncher monitor;

    private File outputDir;

    public JvmMonitorBuildFeature(ArtifactsWatcher artifactsWatcher, EventDispatcher<AgentLifeCycleAdapter> eventDispatcher) {
        this.artifactsWatcher = artifactsWatcher;
        eventDispatcher.addListener(this);
    }

    @Override
    public void buildStarted(AgentRunningBuild build) {
        Collection<AgentBuildFeature> features = build.getBuildFeaturesOfType("jvm-monitor-plugin");
        if (!features.isEmpty()) {
            Loggers.AGENT.info("jvm-monitor-plugin feature enabled for build");

            BuildAgentConfiguration config = build.getAgentConfiguration();
            outputDir = new File(config.getTempDirectory(), "jvmmon");
            boolean result = outputDir.mkdirs();
            if (!result) {
                Loggers.AGENT.warn("Failed to create output directory");
            }
            monitor = new JvmMonitorLauncher(outputDir.getAbsolutePath());
            try {
                monitor.start();
                Loggers.AGENT.info("Started JVM Monitor for build");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void beforeBuildFinish(AgentRunningBuild build, BuildFinishedStatus buildStatus) {
        if (monitor != null) {
            try {
                monitor.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            monitor = null;
            Loggers.AGENT.info("Stopped JVM Monitor");
            artifactsWatcher.addNewArtifactsPath(outputDir.getAbsolutePath() + "=>" + ".teamcity/jvmmon/");
        }
    }
}
