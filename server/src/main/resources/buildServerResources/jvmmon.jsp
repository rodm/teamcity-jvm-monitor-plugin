<%--
  ~ Copyright 2018 Rod MacKenzie.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
--%>

<%@ include file="/include.jsp" %>

<jsp:useBean id="build" type="jetbrains.buildServer.serverSide.SBuild" scope="request"/>
<jsp:useBean id="processes" type="java.util.List<teamcity.jvm.monitor.server.JvmLogName>" scope="request"/>
<jsp:useBean id="teamcityPluginResourcesPath" type="java.lang.String" scope="request"/>

<bs:linkScript>
    ${teamcityPluginResourcesPath}js/jvmmon.js
    ${teamcityPluginResourcesPath}chartjs/Chart.bundle.js
</bs:linkScript>

<table>
    <tbody>
    <tr>
        <td style="width: 140px; vertical-align: top;">Java processes:</td>
        <td>
            <select size="5" name="process" onchange="BS.JvmMon.showJvmLog(this.value)">
                <c:forEach items="${processes}" var="process">
                    <option value="${process.fileName}"><c:out value="${process.displayName}"/></option>
                </c:forEach>
            </select>
        </td>
    </tr>
    <tr class="jvm-info" style="display: none"><td style="width: 140px;">JVM version:</td><td id="jvm-version"></td></tr>
    <tr class="jvm-info" style="display: none"><td style="width: 140px;">JVM arguments:</td><td id="jvm-args"></td></tr>
    <tr class="jvm-info" style="display: none"><td style="width: 140px;">Command line:</td><td id="jvm-cmdline"></td></tr>
    </tbody>
</table>
<div style="height: 1em;">
    <span id="loadingLog" style="display: none;"><forms:progressRing/> Please wait...</span>
</div>
<div id="charts" style="width: 95%">
</div>

<script type="text/javascript">
    (function() {
        BS.JvmMon.setBuildId(${build.buildId});
    })();
</script>
