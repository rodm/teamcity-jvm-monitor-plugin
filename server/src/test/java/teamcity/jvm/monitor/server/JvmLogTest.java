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

package teamcity.jvm.monitor.server;

import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JvmLogTest {

    private BuildArtifact artifact;
    private final String log = "# command line: org.example.Main -v start\n" +
        "# main class: org.example.Main\n" +
        "# main args: -v start\n" +
        "# jvm args: -Djava.util.logging.config.file=conf/logging.properties -Xmx1024m\n" +
        "# jvm flags: \n" +
        "# jvm version: 11.0.18\n" +
        "# timestamp, EU, EC, S0U, S0C, S1U, S1C, OU, OC, MU, MC, CCSU, CCSC, YGC, YGCT, FGC, FGCT\n" +
        "2023/02/13 12:10:13,3145728,11534344,0,8,2097152,2097160,233019912,254803976,215465376,225140736,24291984,27525120,149,0,0,0\n" +
        "2023/02/13 12:10:14,6291456,11534344,0,8,2097152,2097160,233019912,254803976,215465376,225140736,24291984,27525120,149,0,0,0\n" +
        "2023/02/13 12:10:15,0,12582920,0,8,1048576,1048584,214889312,254803976,215549576,225140736,24307152,27525120,150,0,0,0\n" +
        "2023/02/13 12:10:16,5242880,12582920,0,8,1048576,1048584,214889312,254803976,215549576,225140736,24307152,27525120,150,0,0,0\n";

    @BeforeEach
    public void setup() throws Exception {
        artifact = mock(BuildArtifact.class);
        when(artifact.getInputStream()).thenReturn(new ByteArrayInputStream(log.getBytes()));
    }

    @Test
    void jvmLogContainsFileContents() {
        JvmLog jvmLog = JvmLog.from(artifact);

        assertThat(jvmLog.getData().size(), greaterThan(0));
    }

    @Test
    void jvmLogContainsCommandLine() {
        JvmLog jvmLog = JvmLog.from(artifact);

        assertThat(jvmLog.getCommandLine(), equalTo("org.example.Main -v start"));
    }

    @Test
    void jvmLogContainsJvmArguments() {
        JvmLog jvmLog = JvmLog.from(artifact);

        assertThat(jvmLog.getJvmArguments(), equalTo("-Djava.util.logging.config.file=conf/logging.properties -Xmx1024m"));
    }

    @Test
    void jvmLogContainsJvmVersion() {
        JvmLog jvmLog = JvmLog.from(artifact);

        assertThat(jvmLog.getJvmVersion(), equalTo("11.0.18"));
    }

    @Test
    void jvmLogContainsDataHeaderColumns() {
        JvmLog jvmLog = JvmLog.from(artifact);

        assertThat(jvmLog.getColumns(), equalTo("timestamp, EU, EC, S0U, S0C, S1U, S1C, OU, OC, MU, MC, CCSU, CCSC, YGC, YGCT, FGC, FGCT"));
    }

    @Test
    void jvmLogContainsData() {
        JvmLog jvmLog = JvmLog.from(artifact);

        List<String> data = jvmLog.getData();
        assertThat(data.size(), greaterThan(0));
        assertThat(data, not(hasItem(startsWith("# "))));
    }
}
