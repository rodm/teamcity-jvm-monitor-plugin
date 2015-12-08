package teamcity.jvm.monitor.server;

import jetbrains.buildServer.controllers.BaseFormXmlController;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class JvmMonitorController extends BaseFormXmlController {

    private final PluginDescriptor pluginDescriptor;

    public JvmMonitorController(@NotNull SBuildServer server, @NotNull WebControllerManager webControllerManager, @NotNull PluginDescriptor pluginDescriptor) {
        super(server);
        this.pluginDescriptor = pluginDescriptor;
        webControllerManager.registerController("/jvmmon.html", this);
    }

    @Override
    protected ModelAndView doGet(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        JvmLog jvmLog = getJvmLog(request);

        ModelAndView modelAndView = new ModelAndView(this.pluginDescriptor.getPluginResourcesPath("jvmlog.jsp"));
        Map<String, Object> model = modelAndView.getModel();
        model.put("jvmlog", jvmLog);
        return modelAndView;
    }

    private JvmLog getJvmLog(HttpServletRequest request) {
        SBuild build = getBuildFromRequest(request);
        String jvmLogName = getJvmLogNameFromRequest(request);

        BuildArtifact artifact = JvmMonitorUtil.getBuildArtifact(build, jvmLogName);
        if (artifact != null) {
            return new JvmLog(artifact);
        }
        return new JvmLog();
    }

    @Override
    protected void doPost(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Element element) {
    }

    @Nullable
    private SBuild getBuildFromRequest(HttpServletRequest request) {
        String value = request.getParameter("buildId");
        if (value != null) {
            try {
                long buildId = Long.parseLong(value);
                return this.myServer.findBuildInstanceById(buildId);
            }
            catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String getJvmLogNameFromRequest(HttpServletRequest request) {
        return request.getParameter("logId");
    }
}
