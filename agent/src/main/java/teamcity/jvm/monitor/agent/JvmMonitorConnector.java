/*
 * Copyright 2023 Rod MacKenzie.
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

package teamcity.jvm.monitor.agent;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;

public class JvmMonitorConnector implements Runnable {

    private static final Logger LOGGER = Logger.getLogger("jetbrains.buildServer.AGENT");

    private final ServerSocket socket;
    private final CountDownLatch ready = new CountDownLatch(1);
    private final CountDownLatch start = new CountDownLatch(1);
    private BufferedWriter writer;
    private BufferedReader reader;

    public static JvmMonitorConnector createConnector() throws IOException {
        JvmMonitorConnector connector = new JvmMonitorConnector();
        new Thread(connector).start();
        return connector;
    }

    public JvmMonitorConnector() throws IOException {
        this.socket = new ServerSocket(0);
    }

    public int getPort() {
        return socket.getLocalPort();
    }

    @Override
    public void run() {
        try {
            ready.countDown();
            Socket client = socket.accept();
            writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            start.countDown();
        }
        catch (IOException e) {
            LOGGER.warn("Connector failed", e);
        }
    }

    public void ready() throws InterruptedException {
        ready.await();
    }

    public void startMonitor() throws IOException, InterruptedException {
        if (start.await(10, SECONDS)) {
            sendCommand("start");
            String response = reader.readLine();
            if (!"started".equals(response)) {
                LOGGER.warn("Response received: " + response);
                throw new IOException("JVM Monitor tool failed to start");
            }
        } else {
            LOGGER.warn("Connector timed out waiting for JVM Monitor to connect");
        }
    }

    public void stopMonitor() throws IOException {
        sendCommand("stop");
        String response = reader.readLine();
        if (!"stopped".equals(response)) {
            LOGGER.warn("Response received: " + response);
            throw new IOException("JVM Monitor tool failed to stop");
        }
    }

    private void sendCommand(String c) throws IOException {
        writer.write(c);
        writer.newLine();
        writer.flush();
    }
}
