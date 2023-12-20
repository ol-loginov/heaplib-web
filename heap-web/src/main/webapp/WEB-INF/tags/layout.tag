<%@tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8" %>
<%@include file="/WEB-INF/jspf/taglibs.jspf" %>

<%@attribute name="title" type="java.lang.String" required="false" %>
<%@attribute name="titleCode" type="java.lang.String" required="false" %>
<%@attribute name="footScripts" fragment="true" required="false" %>

<html lang="${pageContext.response.locale.language}" data-bs-theme="light">
<head>
    <tags:html-head titleCode="${titleCode}" title="${title}"/>
</head>

<body>

<%--<tags:nav-topbar/>--%>

<div class="layout-main d-flex flex-nowrap">
    <tags:nav-sidebar-menu/>

    <div class="b-example-divider b-example-vr"></div>
    <div class="flex-fill">
        <jsp:doBody/>
    </div>
</div>

<tags:html-foot>
    <jsp:attribute name="footScripts">
        <c:if test="${not empty footScripts}">
            <jsp:invoke fragment="footScripts"/>
        </c:if>
    </jsp:attribute>
</tags:html-foot>

</body>
</html>
