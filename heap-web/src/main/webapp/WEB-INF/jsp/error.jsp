<%@ page contentType="text/html" pageEncoding="utf-8" %>
<%@include file="/WEB-INF/jspf/html.jspf" %>
<tags:layout>
    <main class="text-center mt-5">
        <h1>${status}</h1>
        <h2>${error}</h2>
        <h2 style="color:transparent">${message}</h2>
    </main>

    <!--${trace}-->
</tags:layout>
