package teamcity.jvm.monitor.agent.monitor;

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

    private static final int DEFAULT_POOL_SIZE = 4;

    private final File outputDir;

    private ScheduledExecutorService executor;

    private MonitoredHost monitoredHost;

    private Map<Integer, JvmDataCollector> monitoredVms = new HashMap<Integer, JvmDataCollector>();

    public JvmMonitor(File outputDir) {
        this.outputDir = outputDir;
    }

    public void start() {
        executor = Executors.newScheduledThreadPool(DEFAULT_POOL_SIZE);
        try {
            monitoredHost = MonitoredHost.getMonitoredHost(new HostIdentifier((String) null));
            monitoredHost.addHostListener(this);
        } catch (MonitorException e) {
            e.printStackTrace(System.err);
        } catch (URISyntaxException e) {
            e.printStackTrace(System.err);
        }
    }

    public void stop() {
        for (Integer vmId : new HashSet<Integer>(monitoredVms.keySet())) {
            stopCollector(vmId);
        }
        try {
            monitoredHost.removeHostListener(this);
        } catch (MonitorException e) {
            e.printStackTrace(System.err);
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
    }

    private void startCollector(Integer id) {
        try {
            MonitoredVm mvm = monitoredHost.getMonitoredVm(new VmIdentifier(id.toString()));

            if (!monitoredVms.containsKey(id)) {
                System.out.println("Starting collector for VM: " + id);
                String mainClass = MonitoredVmUtil.mainClass(mvm, true);
                mainClass = mainClass.replace('/', '.');
                mainClass = mainClass.trim();
                String name = id + "-" + mainClass + ".txt";
                File outputFile = new File(outputDir, name);
                Writer writer = new FileWriter(outputFile);
                JvmDataCollector collector = new JvmDataCollector(executor, mvm, writer);
                monitoredVms.put(id, collector);
            }
        }
        catch (MonitorException e) {
            e.printStackTrace(System.err);
        }
        catch (URISyntaxException e) {
            e.printStackTrace(System.err);
        }
        catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private void stopCollector(Integer id) {
        System.out.println("Stopping collector for VM: " + id);
        JvmDataCollector collector = monitoredVms.remove(id);
        if (collector != null) {
            collector.stop();
        }
    }
}
