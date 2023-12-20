<%@tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8" %>
<%@include file="/WEB-INF/jspf/taglibs.jspf" %>

<%--@elvariable id="navigation_section" type="java.lang.String"--%>
<%@attribute name="iconic" type="java.lang.Boolean" required="false" %>

<c:if test="${empty iconic}">
	<c:set var="iconic" value="${false}"/>
</c:if>

<ul class="nav flex-column mb-auto min-h-100">
		<li class="nav-item">
			<a href="/" class="nav-link ${navigation_section == 'dashboard' ? 'active' : ''}" aria-current="page" title="<spring:message code="html.navigation.dashboard"/>">
				<i class="fal fa-home fa-fw"></i>
				<c:if test="${not iconic}"><spring:message code="html.navigation.dashboard"/></c:if>
			</a>
		</li>


	<c:if test="${allow_url_details || allow_url_processes || allow_url_coatings || allow_url_rectifier_profiles || allow_url_users || allow_url_queue ||  allow_url_reports || allow_url_import}">
		<h6 class="sidebar-heading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 text-muted">
			<c:if test="${not iconic}"><spring:message code="html.navigation.group.editor"/></c:if>
		</h6>

		<ul class="nav flex-column">
				<li class="nav-item">
					<a class="nav-link ${navigation_section == 'details' ? 'active' : ''}" href="${cp}/details" title="<spring:message code="html.navigation.details"/>">
						<i class="fal fa-puzzle-piece fa-fw"></i>
						<c:if test="${not iconic}"><span><spring:message code="html.navigation.details"/></span></c:if>
					</a>
				</li>
				<li class="nav-item">
					<a class="nav-link ${navigation_section == 'processes' ? 'active' : ''}" href="${cp}/processes" title="<spring:message code="html.navigation.processes"/>">
						<i class="fal fa-rocket fa-fw"></i>
						<c:if test="${not iconic}"><span><spring:message code="html.navigation.processes"/></span></c:if>
					</a>
				</li>
		</ul>
	</c:if>


	<div class="flex-fill"></div>

	<ul class="nav flex-column mt-5 mb-3">
		<li class="nav-item">
			<a class="nav-link ${navigation_section == 'about' ? 'active' : ''}" href="${cp}/about" title="<spring:message code="html.navigation.about"/>">
				<i class="fal fa-square-info fa-fw"></i>
				<c:if test="${not iconic}"><span><spring:message code="html.navigation.about"/></span></c:if>
			</a>
		</li>
		<c:if test="${not iconic}">
			<li class="nav-item text-center">
				<span class="text-muted">version</span>
			</li>
		</c:if>
	</ul>
</ul>
