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

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        BuildArtifact artifact = getBuildArtifact(request);

        try {
            JsonObjectBuilder responseNode = Json.createObjectBuilder();
            response.setContentType("text/json");
            process(artifact, responseNode);
            response.getOutputStream().write(responseNode.build().toString().getBytes(UTF_8));
        }
        catch (Exception e) {
            // ignore
        }
        return null;
    }

    void process(BuildArtifact artifact, JsonObjectBuilder responseNode) {
        List<String> headers = new ArrayList<>();
        List<String> data = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(artifact.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("# ")) {
                    headers.add(line);
                } else {
                    data.add(line);
                }
            }
        }
        catch (IOException e) {
            LOGGER.warn("Exception reading artifact: " + artifact.getName(), e);
        }
        String cmdline = "";
        String jvmArgs = "";
        String jvmVersion = "";
        String head = "";
        for (String header : headers) {
            if (header.startsWith("# command line")) {
                cmdline = header.substring("# command line: ".length());
            }
            if (header.startsWith("# jvm args:")) {
                jvmArgs = header.substring("# jvm args: ".length());
            }
            if (header.startsWith("# jvm version:")) {
                jvmVersion = header.substring("# jvm version: ".length());
            }
            if (header.startsWith("# timestamp")) {
                head = header.replace("# ", "");
            }
        }

        List<String> timestamps = new ArrayList<>();
        Map<String, List<Long>> datasets = new LinkedHashMap<>();

        String[] columns = head.split(",");
        for (String line : data) {
            String[] parts = line.split(",");
            for (int i = 0; i < parts.length; i++) {
                if ("timestamp".equals(columns[i])) {
                    timestamps.add(parts[i]);
                } else {
                    String column = columns[i].trim();
                    datasets.computeIfAbsent(column, k -> new ArrayList<>()).add(Long.parseLong(parts[i]));
                }
            }
        }

        JsonObject info = Json.createObjectBuilder()
            .add("cmdline", cmdline)
            .add("jvmargs", jvmArgs)
            .add("jvmversion", jvmVersion)
            .build();
        responseNode.add("info", info);

        JsonObjectBuilder jsonDatasets = Json.createObjectBuilder();
        jsonDatasets.add("timestamp", Json.createArrayBuilder(timestamps));
        for (Map.Entry<String, List<Long>> entry : datasets.entrySet()) {
            jsonDatasets.add(entry.getKey(), Json.createArrayBuilder(entry.getValue()));
        }
        responseNode.add("datasets", jsonDatasets.build());
    }

    private BuildArtifact getBuildArtifact(HttpServletRequest request) {
        Long buildId = getBuildIdFromRequest(request);
        String jvmLogName = getJvmLogNameFromRequest(request);
        SBuild build = myServer.findBuildInstanceById(buildId);
        return JvmMonitorUtil.getBuildArtifact(build, jvmLogName);
    }

    @Nullable
    private Long getBuildIdFromRequest(HttpServletRequest request) {
        String value = request.getParameter("buildId");
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
