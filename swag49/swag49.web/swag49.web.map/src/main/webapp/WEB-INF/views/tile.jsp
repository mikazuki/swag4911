<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>

<c:choose>
    <c:when test="${tileInfo.hasBase && tileInfo.enemyTerritory}">
        <jsp:include page="enemybaseview.jsp"/>
    </c:when>
    <c:when test="${tileInfo.hasBase && !tileInfo.enemyTerritory}">
        <jsp:include page="ownbaseview.jsp"/>
    </c:when>
    <c:otherwise>
        <jsp:include page="tilebaseview.jsp"/>
    </c:otherwise>
</c:choose>
<a class="contentLink" href="sendtroops.html?x=${tileInfo.x}&y=${tileInfo.y}">Send Troops</a>