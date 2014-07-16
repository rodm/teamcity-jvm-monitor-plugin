package teamcity.jvm.monitor.agent.monitor;

import sun.jvmstat.monitor.Monitor;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.MonitoredVmUtil;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class JvmDataCollector implements Runnable {

    private ScheduledFuture<?> future;

    private MonitoredVm monitoredVm;

    private PrintWriter out;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

    public JvmDataCollector(ScheduledExecutorService executor, MonitoredVm mvm, Writer writer) {
        this.monitoredVm = mvm;
        this.out = new PrintWriter(new BufferedWriter(writer));
        try {
            String cmdLine = MonitoredVmUtil.commandLine(mvm);
            String mainClass = MonitoredVmUtil.mainClass(mvm, true);
            String mainArgs = MonitoredVmUtil.mainArgs(mvm);
            String jvmArgs = MonitoredVmUtil.jvmArgs(mvm);
            String jvmFlags = MonitoredVmUtil.jvmFlags(mvm);
            this.out.println("# command line: " + cmdLine);
            this.out.println("# main class: " + mainClass);
            this.out.println("# main args: " + mainArgs);
            this.out.println("# jvm args: " + jvmArgs);
            this.out.println("# jvm flags: " + jvmFlags);
            this.out.println("# timestamp, EU, EC, S0U, S0C, S1U, S1C, OU, OC, PU, PC, YGC, YGCT, FGC, FGCT");
            this.future = executor.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
        }
        catch (MonitorException e) {
            e.printStackTrace(System.err);
        }
    }

    public void stop() {
        future.cancel(false);
        out.close();
    }
    @Override
    public void run() {
        StringBuilder stats = new StringBuilder();
        stats.append(dateFormat.format(new Date())).append(",");
        stats.append(outputMonitor("sun.gc.generation.0.space.0.used")).append(",");
        stats.append(outputMonitor("sun.gc.generation.0.space.0.capacity")).append(",");
        stats.append(outputMonitor("sun.gc.generation.0.space.1.used")).append(",");
        stats.append(outputMonitor("sun.gc.generation.0.space.1.capacity")).append(",");
        stats.append(outputMonitor("sun.gc.generation.0.space.2.used")).append(",");
        stats.append(outputMonitor("sun.gc.generation.0.space.2.capacity")).append(",");
        stats.append(outputMonitor("sun.gc.generation.1.space.0.used")).append(",");
        stats.append(outputMonitor("sun.gc.generation.1.space.0.capacity")).append(",");
        stats.append(outputMonitor("sun.gc.generation.2.space.0.used")).append(",");
        stats.append(outputMonitor("sun.gc.generation.2.space.0.capacity")).append(",");
        stats.append(outputMonitor("sun.gc.collector.0.invocations")).append(",");
        stats.append(calculateTime("sun.gc.collector.0.time", "sun.os.hrt.frequency")).append(",");
        stats.append(outputMonitor("sun.gc.collector.1.invocations")).append(",");
        stats.append(calculateTime("sun.gc.collector.1.time", "sun.os.hrt.frequency")).append("\n");
        out.write(stats.toString());
        out.flush();
    }

    private long calculateTime(String timeName, String frequencyName) {
        Long time = (Long) outputMonitor(timeName);
        Long frequency = (Long) outputMonitor(frequencyName);
        return time / frequency;
    }

    private Object outputMonitor(String name) {
        Object value = "-";
        try {
            Monitor monitor = monitoredVm.findByName(name);
            value = monitor.getValue();
        }
        catch (MonitorException e) {
            e.printStackTrace(System.err);
        }
        return value;
    }
}
