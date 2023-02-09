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

package teamcity.jvm.monitor.tool;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class JvmMonitorTool {

    private static final Logger LOGGER = Logger.getLogger(JvmMonitorTool.class);

    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            File outputDir = new File(args[0]);
            LOGGER.info("Output directory: " + outputDir.getCanonicalPath());
            if (!outputDir.exists()) {
                LOGGER.warn("Output directory does not exist. JVM Monitor exiting.");
                return;
            }

            JvmMonitor monitor = new JvmMonitor(outputDir);
            LOGGER.info("JVM Monitor process started");
            boolean run = true;
            while (run) {
                String command = reader.readLine();
                if ("start".equals(command)) {
                    LOGGER.info("Starting JVM Monitor");
                    monitor.start();
                } else if ("stop".equals(command) || command == null) {
                    LOGGER.info("Stopping JVM Monitor");
                    run = false;
                    monitor.stop();
                }
            }
            LOGGER.info("JVM Monitor process exiting");
        }
        catch (IOException e) {
            LOGGER.warn("", e);
        }
    }
}
