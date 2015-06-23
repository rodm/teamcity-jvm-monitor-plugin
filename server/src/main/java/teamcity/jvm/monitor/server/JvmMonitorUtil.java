package teamcity.jvm.monitor.server;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;

public class JvmMonitorUtil {

    private static final String JVMMON_PATH = ".teamcity/jvmmon";

    static BuildArtifact getBuildArtifact(SBuild build) {
        BuildArtifacts artifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_HIDDEN_ONLY);
        return artifacts.getArtifact(JVMMON_PATH);
    }

    static BuildArtifact getBuildArtifact(SBuild build, String name) {
        BuildArtifacts artifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_HIDDEN_ONLY);
        return artifacts.getArtifact(JVMMON_PATH + "/" + name);
    }
}
