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
import sun.jvmstat.monitor.HostIdentifier;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.MonitoredVmUtil;
import sun.jvmstat.monitor.VmIdentifier;
import sun.jvmstat.monitor.event.HostEvent;
import sun.jvmstat.monitor.event.HostListener;
import sun.jvmstat.monitor.event.VmStatusChangeEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class JvmMonitor implements HostListener {

    private static final Logger LOGGER = LogManager.getLogger(JvmMonitor.class);

    private static final int DEFAULT_POOL_SIZE = 4;

    private final File outputDir;

    private ScheduledExecutorService executor;

    private MonitoredHost monitoredHost;

    private Map<Integer, JvmDataCollector> monitoredVms = new HashMap<>();

    public JvmMonitor(File outputDir) {
        this.outputDir = outputDir;
    }

    public void start() {
        executor = Executors.newScheduledThreadPool(DEFAULT_POOL_SIZE);
        try {
            monitoredHost = MonitoredHost.getMonitoredHost(new HostIdentifier((String) null));
            monitoredHost.addHostListener(this);
        }
        catch (MonitorException e) {
            LOGGER.error("Exception starting JVM monitor", e);
        }
        catch (URISyntaxException e) {
            LOGGER.error("Invalid host identifier", e);
        }
    }

    public void stop() {
        for (Integer vmId : new HashSet<>(monitoredVms.keySet())) {
            stopCollector(vmId);
        }
        try {
            monitoredHost.removeHostListener(this);
        }
        catch (MonitorException e) {
            LOGGER.error("Exception stopping JVM monitor", e);
        }
        executor.shutdown();
    }

    @Override
    public void vmStatusChanged(VmStatusChangeEvent event) {
        Set<Integer> startedVMs = event.getStarted();
        for (Integer id : startedVMs) {
            startCollector(id);
        }
        Set<Integer> terminatedVMs = event.getTerminated();
        for (Integer id : terminatedVMs) {
            stopCollector(id);
        }
    }

    @Override
    public void disconnected(HostEvent event) {
        // ignore
    }

    private void startCollector(Integer id) {
        try {
            MonitoredVm mvm = monitoredHost.getMonitoredVm(new VmIdentifier(id.toString()));

            if (!monitoredVms.containsKey(id)) {
                String mainClass = getMainClass(mvm);
                LOGGER.info("Starting collector for VM: " + id + ": main class: " + mainClass);
                String name = id + "-" + mainClass + ".txt";
                File outputFile = new File(outputDir, name);
                Writer writer = new FileWriter(outputFile);
                JvmDataCollector collector = new JvmDataCollector(executor, mvm, writer);
                monitoredVms.put(id, collector);
            }
        }
        catch (MonitorException e) {
            LOGGER.error("Exception starting collector for VM: " + id, e);
        }
        catch (URISyntaxException e) {
            LOGGER.error("Invalid VM identifier", e);
        }
        catch (IOException e) {
            LOGGER.error("Failure creating JVM collector file", e);
        }
    }

    private void stopCollector(Integer id) {
        LOGGER.info("Stopping collector for VM: " + id);
        JvmDataCollector collector = monitoredVms.remove(id);
        if (collector != null) {
            collector.stop();
        }
    }

    private static String getMainClass(MonitoredVm mvm) throws MonitorException {
        String mainClass = MonitoredVmUtil.mainClass(mvm, true);
        mainClass = mainClass.replace('/', '.').trim();
        return mainClass.isEmpty() ? "unknown" : mainClass;
    }
}
