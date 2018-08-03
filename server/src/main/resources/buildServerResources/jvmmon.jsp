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
<jsp:useBean id="processes" type="java.util.List" scope="request"/>
<jsp:useBean id="teamcityPluginResourcesPath" type="java.lang.String" scope="request"/>

<script src="<c:url value='${teamcityPluginResourcesPath}js/jvmmon.js'/>" type="text/javascript"></script>

<div id="container">
    <p>Java processes</p>
    <p>
        <select size="5" name="process" onchange="BS.JvmMon.showJvmLog(this.value)">
            <c:forEach items="${processes}" var="process">
                <option value="${process}"><c:out value="${process}"/></option>
            </c:forEach>
        </select>
    </p>
</div>

<div style="height: 1em;">
    <span id="loadingLog" style="display: none;"><forms:progressRing/> Please wait...</span>
</div>
<div id="jvmLogDiv" style="display: none;">
    <a id="jvmLogAnchor"></a>
    <div><span id="jvmLogMarker">JVM log:</span></div>
    <div id="jvmLogContainer"></div>
</div>

<script type="text/javascript">
    (function() {
        BS.JvmMon.setBuildId(${build.buildId});
    })();
</script>
