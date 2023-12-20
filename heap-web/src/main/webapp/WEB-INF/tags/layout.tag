<%@tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8" %>
<%@include file="/WEB-INF/jspf/taglibs.jspf" %>

<%@attribute name="title" type="java.lang.String" required="false" %>
<%@attribute name="titleCode" type="java.lang.String" required="false" %>
<%@attribute name="footScripts" fragment="true" required="false" %>

<html lang="${pageContext.response.locale.language}">
<head>
	<tags:html-head titleCode="${titleCode}" title="${title}"/>
</head>

<body>

<tags:nav-topbar/>

<div class="container-fluid">
	<div class="row d-print-block">
		<nav id="sidebarMenu" class="col-md-3 col-lg-2 d-md-block bg-light sidebar sidebar-fixed d-print-none collapse">
			<div class="position-sticky pt-3 sidebar-sticky">
				<tags:nav-sidebar-menu/>
			</div>
		</nav>

		<main class="col-md-9 ms-sm-auto col-lg-10 px-md-4 col-print-12">
			<jsp:doBody/>
		</main>
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
