/*
 * Copyright 2018 Rod MacKenzie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package teamcity.jvm.monitor.server;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class JvmMonitorController extends BaseController {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.SERVER");

    public JvmMonitorController(@NotNull SBuildServer server, @NotNull WebControllerManager webControllerManager) {
        super(server);
        webControllerManager.registerController("/jvmmon.html", this);
    }

    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        var artifact = getBuildArtifact(request);
        if (artifact != null) {
            try {
                var responseNode = Json.createObjectBuilder();
                response.setContentType("text/json");
                process(artifact, responseNode);
                response.getOutputStream().write(responseNode.build().toString().getBytes(UTF_8));
            }
            catch (Exception e) {
                LOGGER.error("Failure writing response", e);
            }
        }
        return null;
    }

    void process(BuildArtifact artifact, JsonObjectBuilder responseNode) {
        List<String> timestamps = new ArrayList<>();
        Map<String, List<Long>> datasets = new LinkedHashMap<>();

        var jvmLog = JvmLog.from(artifact);
        var columns = jvmLog.getColumns().split(",");
        for (String line : jvmLog.getData()) {
            var parts = line.split(",");
            for (var i = 0; i < parts.length; i++) {
                if ("timestamp".equals(columns[i])) {
                    timestamps.add(parts[i]);
                } else {
                    var column = columns[i].trim();
                    datasets.computeIfAbsent(column, k -> new ArrayList<>()).add(Long.parseLong(parts[i]));
                }
            }
        }

        var info = Json.createObjectBuilder()
            .add("cmdline", jvmLog.getCommandLine())
            .add("jvmargs", jvmLog.getJvmArguments())
            .add("jvmversion", jvmLog.getJvmVersion())
            .build();
        responseNode.add("info", info);

        var jsonDatasets = Json.createObjectBuilder();
        jsonDatasets.add("timestamp", Json.createArrayBuilder(timestamps));
        for (Map.Entry<String, List<Long>> entry : datasets.entrySet()) {
            jsonDatasets.add(entry.getKey(), Json.createArrayBuilder(entry.getValue()));
        }
        responseNode.add("datasets", jsonDatasets.build());
    }

    @Nullable
    private BuildArtifact getBuildArtifact(HttpServletRequest request) {
        var buildId = getBuildIdFromRequest(request);
        if (buildId == null) return null;
        var jvmLogName = getJvmLogNameFromRequest(request);
        var build = myServer.findBuildInstanceById(buildId);
        if (build == null) return null;
        return JvmMonitorUtil.getBuildArtifact(build, jvmLogName);
    }

    @Nullable
    private Long getBuildIdFromRequest(HttpServletRequest request) {
        var value = request.getParameter("buildId");
        if (value != null) {
            try {
                return Long.parseLong(value);
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
