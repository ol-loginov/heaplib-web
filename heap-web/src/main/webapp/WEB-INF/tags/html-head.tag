<%@tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8" %>
<%@include file="/WEB-INF/jspf/taglibs.jspf" %>

<%@attribute name="title" type="java.lang.String" required="false" %>
<%@attribute name="titleCode" type="java.lang.String" required="false" %>

<link rel="apple-touch-icon" sizes="180x180" href="/static/favicon/apple-touch-icon.png">
<link rel="icon" type="image/png" sizes="32x32" href="/static/favicon/favicon-32x32.png">
<link rel="icon" type="image/png" sizes="16x16" href="/static/favicon/favicon-16x16.png">
<link rel="manifest" href="/site.webmanifest">
<link rel="mask-icon" href="/static/favicon/safari-pinned-tab.svg" color="#5bbad5">
<meta name="msapplication-TileColor" content="#da532c">
<meta name="theme-color" content="#ffffff">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<link href="${cp}/static/vendor/bootstrap@5.2.3/bootstrap.min.css" rel="stylesheet" integrity="sha384-rbsA2VBKQhggwzxH7pPCaAqO46MgnOM80zW1RWuH61DGLwZJEdK2Kadq2F9CUG65" crossorigin="anonymous">
<link href="${cp}/static/dist/scss/theme.css" rel="stylesheet">

<title>
    <c:choose>
        <c:when test="${not empty title}"><c:out value="${title}"/></c:when>
        <c:when test="${not empty titleCode}"><spring:message code="${titleCode}"/></c:when>
        <c:otherwise><spring:message code="html.defaultTitle"/></c:otherwise>
    </c:choose>
</title>
