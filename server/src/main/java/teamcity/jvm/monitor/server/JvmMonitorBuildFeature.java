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
