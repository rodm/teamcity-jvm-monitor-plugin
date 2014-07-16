package teamcity.jvm.monitor.agent.monitor;

import sun.jvmstat.monitor.HostIdentifier;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.MonitoredVmUtil;
import sun.jvmstat.monitor.VmIdentifier;
import sun.jvmstat.monitor.event.HostEvent;
import sun.jvmstat.monitor.event.HostListener;
import sun.jvmstat.monitor.event.MonitorStatusChangeEvent;
import sun.jvmstat.monitor.event.VmEvent;
import sun.jvmstat.monitor.event.VmListener;
import sun.jvmstat.monitor.event.VmStatusChangeEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JvmMonitor implements Runnable, VmListener, HostListener {

    private final File outputDir;

    private ScheduledExecutorService executor;

    private MonitoredHost monitoredHost;

    private Map<Integer, JvmDataCollector> monitoredVms = new HashMap<Integer, JvmDataCollector>();

    public JvmMonitor(File outputDir) {
        this.outputDir = outputDir;
        this.executor = Executors.newScheduledThreadPool(4);
        try {
            this.monitoredHost = MonitoredHost.getMonitoredHost(new HostIdentifier((String) null));
            this.monitoredHost.addHostListener(this);
        } catch (MonitorException e) {
            e.printStackTrace(System.err);
        } catch (URISyntaxException e) {
            e.printStackTrace(System.err);
        }
    }

    public void start() {
        executor.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        for (JvmDataCollector collector : monitoredVms.values()) {
            collector.stop();
        }
        executor.shutdown();
    }

    @Override
    public void run() {
        try {
            Set<Integer> activeVms = monitoredHost.activeVms();
            for (Integer id : activeVms) {
                MonitoredVm mvm = monitoredHost.getMonitoredVm(new VmIdentifier(id.toString()));

                if (!monitoredVms.containsKey(id)) {
                    System.out.println("Adding JVM(pid=" + id + ") to monitored list");
                    String mainClass = MonitoredVmUtil.mainClass(mvm, true);
                    mainClass = mainClass.replace('/', '.');
                    mainClass = mainClass.trim();
                    String name = id + "-" + mainClass + ".txt";
                    File outputFile = new File(outputDir, name);
                    Writer writer = new FileWriter(outputFile);
                    JvmDataCollector collector = new JvmDataCollector(executor, mvm, writer);
                    monitoredVms.put(id, collector);
                    mvm.addVmListener(this);
                }
            }
        } catch (MonitorException e) {
            e.printStackTrace(System.err);
        } catch (URISyntaxException e) {
            e.printStackTrace(System.err);
        } catch (FileNotFoundException e) {
            e.printStackTrace(System.err);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void monitorStatusChanged(MonitorStatusChangeEvent event) {
    }

    @Override
    public void monitorsUpdated(VmEvent event) {
    }

    @Override
    public void disconnected(VmEvent event) {
        int id = event.getMonitoredVm().getVmIdentifier().getLocalVmId();
        System.out.println("JVM pid=" + id + " disconnected");

        if (monitoredVms.containsKey(id)) {
            System.out.println("Removing JVM(pid=" + id + ") from monitored list");
            JvmDataCollector collector = monitoredVms.remove(id);
            collector.stop();
            try {
                event.getMonitoredVm().removeVmListener(this);
            } catch (MonitorException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    @Override
    public void vmStatusChanged(VmStatusChangeEvent ev) {
        Set<Integer> terminated = ev.getTerminated();
        for (Integer vm : terminated) {
            System.out.println("vm terminated: " + vm);
        }
    }

    @Override
    public void disconnected(HostEvent event) {
        if (monitoredHost == event.getMonitoredHost()) {
            System.out.println("host: " + event.getMonitoredHost());
        }
    }
}
