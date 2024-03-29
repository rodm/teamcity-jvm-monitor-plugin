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

package teamcity.jvm.monitor.server;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;

import static teamcity.jvm.monitor.JvmMonitorPlugin.JVM_MONITOR_LOG_PATH;

public class JvmMonitorUtil {

    static BuildArtifact getBuildArtifact(SBuild build) {
        BuildArtifacts artifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_HIDDEN_ONLY);
        return artifacts.getArtifact(JVM_MONITOR_LOG_PATH);
    }

    static BuildArtifact getBuildArtifact(SBuild build, String name) {
        BuildArtifacts artifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_HIDDEN_ONLY);
        return artifacts.getArtifact(JVM_MONITOR_LOG_PATH + "/" + name);
    }

    private JvmMonitorUtil() {}
}
