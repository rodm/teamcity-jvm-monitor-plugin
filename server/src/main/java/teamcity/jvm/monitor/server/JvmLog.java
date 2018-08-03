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

import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JvmLog {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.SERVER");

    private List<String> contents = new ArrayList<>();

    public JvmLog() {
        this.contents.add("JVM log file not found");
    }

    public JvmLog(BuildArtifact artifact) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(artifact.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contents.add(line);
            }
        } catch (IOException e) {
            LOGGER.warn("Exception reading artifact: " + artifact.getName(), e);
        }
    }

    public List<String> getContents() {
        return Collections.unmodifiableList(this.contents);
    }
}
