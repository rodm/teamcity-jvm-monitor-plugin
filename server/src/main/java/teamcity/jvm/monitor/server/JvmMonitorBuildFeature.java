package teamcity.jvm.monitor.server;


import jetbrains.buildServer.serverSide.BuildFeature;

import java.util.Map;

public class JvmMonitorBuildFeature extends BuildFeature {

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
        return null;
    }

    @Override
    public boolean isMultipleFeaturesPerBuildTypeAllowed() {
        return false;
    }

    @Override
    public String describeParameters(Map<String, String> params) {
        return "JVM Monitor plugin";
    }
}
