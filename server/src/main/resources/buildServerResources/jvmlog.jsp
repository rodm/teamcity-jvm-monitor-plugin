<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="jvmlog" type="teamcity.jvm.monitor.server.JvmLog" scope="request"/>

<p>
<c:forEach items="${jvmlog.contents}" var="line">
    <c:out value="${line}"/><br/>
</c:forEach>
</p>
