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

package teamcity.jvm.monitor.tool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class JvmMonitorTool {

    private static final Logger LOGGER = LogManager.getLogger(JvmMonitorTool.class);

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        File outputDir = new File(args[1]);
        run(port, outputDir);
    }

    private static void run(int port, File outputDir) {
        try (Socket socket = new Socket("localhost", port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())))
        {
            LOGGER.info("Launcher port: " + port);
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
                    sendResponse(writer, "started");
                } else if ("stop".equals(command) || command == null) {
                    LOGGER.info("Stopping JVM Monitor");
                    monitor.stop();
                    sendResponse(writer, "stopped");
                    run = false;
                }
            }
            LOGGER.info("JVM Monitor process exiting");
        }
        catch (IOException e) {
            LOGGER.warn("", e);
        }
    }

    private static void sendResponse(BufferedWriter writer, String response) throws IOException {
        writer.write(response);
        writer.newLine();
        writer.flush();
    }
}
