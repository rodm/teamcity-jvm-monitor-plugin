package teamcity.jvm.monitor.agent.monitor;

import org.junit.Before;
import org.junit.Test;
import sun.jvmstat.monitor.Monitor;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredVm;

import java.io.StringWriter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JvmDataCollectorTest {

    private ScheduledExecutorService executor;
    private MonitoredVm monitoredVm;
    private StringWriter writer;
    private Monitor integerMonitor;
    private Monitor longMonitor;

    @Before
    public void setup() {
        executor = mock(ScheduledExecutorService.class);
        monitoredVm = mock(MonitoredVm.class);
        writer = new StringWriter();
        integerMonitor = mock(Monitor.class);
        longMonitor = mock(Monitor.class);
        when(integerMonitor.getValue()).thenReturn(1);
        when(longMonitor.getValue()).thenReturn(1L);
    }

    @Test
    public void collectorRegistersWithScheduledExecutorService() {
        new JvmDataCollector(executor, monitoredVm, writer);

        verify(executor).scheduleAtFixedRate(any(JvmDataCollector.class), eq(0L), eq(1L), eq(TimeUnit.SECONDS));
    }

    @Test
    public void collectOutputsMonitoredVmJavaVersion() throws MonitorException {
        Monitor stringMonitor = mock(Monitor.class);
        when(stringMonitor.getValue()).thenReturn("1.7");
        when(monitoredVm.findByName(eq("java.property.java.version"))).thenReturn(stringMonitor);

        new JvmDataCollector(executor, monitoredVm, writer);

        assertThat(writer.toString(), containsString("# jvm version: 1.7"));
    }

    @Test
    public void collectorOutputsGarbageCollectorMonitorValues() throws MonitorException {
        when(monitoredVm.findByName(startsWith("sun.gc.generation"))).thenReturn(integerMonitor);
        when(monitoredVm.findByName(startsWith("sun.gc.collector"))).thenReturn(longMonitor);
        when(monitoredVm.findByName(startsWith("sun.os"))).thenReturn(longMonitor);

        JvmDataCollector collector = new JvmDataCollector(executor, monitoredVm, writer);
        collector.run();

        assertThat(writer.toString(), containsString("1,1,1,1,1,1,1,1,1,1,1,1,1,1"));
    }
    
    @Test
    public void collectorOutputsGarbageCollectorMonitorValuesForJava8() throws MonitorException {
        when(monitoredVm.findByName(startsWith("sun.gc.generation"))).thenReturn(integerMonitor);
        when(monitoredVm.findByName(startsWith("sun.gc.collector"))).thenReturn(longMonitor);
        when(monitoredVm.findByName(startsWith("sun.os"))).thenReturn(longMonitor);

        // monitors for the permanent generation no longer exist
        when(monitoredVm.findByName(eq("sun.gc.generation.2.space.0.used"))).thenReturn(null);
        when(monitoredVm.findByName(eq("sun.gc.generation.2.space.0.capacity"))).thenReturn(null);

        JvmDataCollector collector = new JvmDataCollector(executor, monitoredVm, writer);
        collector.run();

        assertThat(writer.toString(), containsString("1,1,1,1,1,1,1,1,-,-,1,1,1,1"));
    }
}
