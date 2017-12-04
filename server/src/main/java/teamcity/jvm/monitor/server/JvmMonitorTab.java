package teamcity.jvm.monitor.server;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.ViewLogTab;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JvmMonitorTab extends ViewLogTab {

    private static final String INCLUDE_URL = "jvmmon.jsp";
    private static final String TITLE = "JVM Monitor";

    public JvmMonitorTab(@NotNull PagePlaces pagePlaces,
                         @NotNull SBuildServer server,
                         @NotNull PluginDescriptor pluginDescriptor)
    {
        super(TITLE, "jvmmon", pagePlaces, server);
        setPluginName(pluginDescriptor.getPluginName());
        setIncludeUrl(INCLUDE_URL);
    }

    @Override
    public boolean isAvailable(@NotNull HttpServletRequest request) {
        SBuild build = this.getBuild(request);
        return build != null && this.hasArtifacts(build);
    }

    private boolean hasArtifacts(@NotNull SBuild build) {
        BuildArtifact artifact = JvmMonitorUtil.getBuildArtifact(build);
        return artifact != null && !artifact.getChildren().isEmpty();
    }

    @Override
    protected void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request, @NotNull SBuild build) {
        model.put("build", build);
        model.put("processes", getProcesses(build));
    }

    private List<String> getProcesses(SBuild build) {
        List<String> processes = new ArrayList<>();
        BuildArtifact artifact = JvmMonitorUtil.getBuildArtifact(build);
        if (artifact != null) {
            for (BuildArtifact file : artifact.getChildren()) {
                processes.add(file.getName());
            }
        }
        return processes;
    }
}
