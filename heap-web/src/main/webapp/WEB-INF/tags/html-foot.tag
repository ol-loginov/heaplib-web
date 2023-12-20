<%@tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8" %>
<%@include file="/WEB-INF/jspf/taglibs.jspf" %>

<%@attribute name="footScripts" fragment="true" required="false" %>
<%@attribute name="disableStatus" type="java.lang.Boolean" required="false" %>
<%@attribute name="disableApp" type="java.lang.Boolean" required="false" %>

<div id="toastContainer" class="toast-container position-fixed top-0 start-50 translate-middle-x p-3 d-print-none"></div>

<c:if test="${empty disableApp || not disableApp}">
	<script src="${cp}/static/dist/scripts/App.js"></script>
	<script>
		App.AppConfig.CP = "${cp}"
		App.AppConfig.locale = "${pageContext.response.locale.language}"
	</script>
</c:if>

<c:if test="${not empty footScripts}">
	<jsp:invoke fragment="footScripts"/>
</c:if>
