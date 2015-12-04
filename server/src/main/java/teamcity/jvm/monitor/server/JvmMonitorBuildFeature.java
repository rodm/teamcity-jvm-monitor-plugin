package teamcity.jvm.monitor.server;


import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.web.openapi.PluginDescriptor;

import java.util.Map;

public class JvmMonitorBuildFeature extends BuildFeature {

    private final PluginDescriptor descriptor;

    public JvmMonitorBuildFeature(PluginDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public String getType() {
        return "jvm-monitor-plugin";
    }

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

    @Override
    public String describeParameters(Map<String, String> params) {
        return "Collects garbage collection statistics for any JVM running during the build";
    }
}
