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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

class JvmDataCollectorTest {

    private ScheduledExecutorService executor;
    private MonitoredVm monitoredVm;
    private StringWriter writer;
    private Monitor integerMonitor;
    private Monitor longMonitor;

    @BeforeEach
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
    void collectorRegistersWithScheduledExecutorService() {
        new JvmDataCollector(executor, monitoredVm, writer);

        verify(executor).scheduleAtFixedRate(any(JvmDataCollector.class), eq(0L), eq(1L), eq(TimeUnit.SECONDS));
    }

    @Test
    void collectOutputsMonitoredVmJavaVersion() throws MonitorException {
        Monitor stringMonitor = mock(Monitor.class);
        when(stringMonitor.getValue()).thenReturn("1.8");
        when(monitoredVm.findByName("java.property.java.version")).thenReturn(stringMonitor);

        new JvmDataCollector(executor, monitoredVm, writer);

        assertThat(writer.toString(), containsString("# jvm version: 1.8"));
    }

    @Test
    void collectorOutputsHeaderForJvmWithMetaspace() {
        new JvmDataCollector(executor, monitoredVm, writer);

        assertThat(writer.toString(), containsString("# timestamp, EU, EC, S0U, S0C, S1U, S1C, OU, OC, MU, MC, CCSU, CCSC, YGC, YGCT, FGC, FGCT"));
    }

    @Test
    void collectorOutputsGarbageCollectorMonitorValuesForVmWithMetaspace() throws MonitorException {
        when(monitoredVm.findByName(startsWith("sun.gc.generation"))).thenReturn(integerMonitor);
        when(monitoredVm.findByName(startsWith("sun.gc.collector"))).thenReturn(longMonitor);
        when(monitoredVm.findByName(startsWith("sun.os"))).thenReturn(longMonitor);
        when(monitoredVm.findByName(startsWith("sun.gc.metaspace"))).thenReturn(integerMonitor);
        when(monitoredVm.findByName(startsWith("sun.gc.compressedclassspace"))).thenReturn(integerMonitor);

        JvmDataCollector collector = new JvmDataCollector(executor, monitoredVm, writer);
        collector.run();

        assertThat(writer.toString(), containsString("1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1"));
    }

    @Test
    void collectorOutputsDummyValuesForMissingMonitors() throws MonitorException {
        when(monitoredVm.findByName(startsWith("sun.gc.generation"))).thenReturn(integerMonitor);
        when(monitoredVm.findByName(startsWith("sun.gc.collector"))).thenReturn(longMonitor);
        when(monitoredVm.findByName(startsWith("sun.os"))).thenReturn(longMonitor);

        JvmDataCollector collector = new JvmDataCollector(executor, monitoredVm, writer);
        collector.run();

        assertThat(writer.toString(), containsString("1,1,1,1,1,1,1,1,-,-,-,-,1,1,1,1"));
    }
}
