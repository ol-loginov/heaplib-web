<%@ page contentType="text/html" pageEncoding="utf-8" %>
<%@include file="/WEB-INF/jspf/html.jspf" %>
<tags:layout titleCode='dashboard.pageTitle'>
    <jsp:body>
        <div class="m-3">
            <h1>Files in store folder</h1>

            <table class="table">
                <thead>
                <tr>
                    <th>File</th>
                    <th>Modification time</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="inputFile" items="${inputFiles}">
                    <tr>
                        <td>${h.h(inputFile.path)}</td>
                        <td>${inputFile.modificationTime}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </jsp:body>
</tags:layout>