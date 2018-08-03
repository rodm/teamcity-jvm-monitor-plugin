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

import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import teamcity.jvm.monitor.JvmMonitorPlugin;

import java.util.Map;

public class JvmMonitorBuildFeature extends BuildFeature {

    private final PluginDescriptor descriptor;

    public JvmMonitorBuildFeature(PluginDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @NotNull
    @Override
    public String getType() {
        return JvmMonitorPlugin.FEATURE_TYPE;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "JVM Monitor";
    }

    @Override
    public String getEditParametersUrl() {
        return descriptor.getPluginResourcesPath("editFeature.jsp");
    }

    @Override
    public boolean isMultipleFeaturesPerBuildTypeAllowed() {
        return false;
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull Map<String, String> params) {
        return "Collects garbage collection statistics for any JVM running during the build";
    }
}
