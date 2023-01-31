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

import jetbrains.buildServer.agent.AgentBuildFeature;
import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.FileUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import teamcity.jvm.monitor.JvmMonitorPlugin;

import java.io.File;
import java.util.Collection;

public class JvmMonitorBuildFeature extends AgentLifeCycleAdapter {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.AGENT");

    private final PluginDescriptor pluginDescriptor;
    private final ArtifactsWatcher artifactsWatcher;

    private JvmMonitorLauncher monitor;

    private File outputDir;

    public JvmMonitorBuildFeature(PluginDescriptor pluginDescriptor, ArtifactsWatcher artifactsWatcher, EventDispatcher<AgentLifeCycleListener> eventDispatcher) {
        this.pluginDescriptor = pluginDescriptor;
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
            FileUtil.delete(outputDir);
            boolean result = outputDir.mkdirs();
            if (!result) {
                LOGGER.warn("Failed to create output directory");
            }

            File toolDir = pluginDescriptor.getPluginRoot().toPath().resolve("tool").toFile();
            monitor = new JvmMonitorLauncher(toolDir, config.getAgentLogsDirectory(), outputDir);
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
