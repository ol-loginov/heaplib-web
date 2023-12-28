<%@ page contentType="text/html" pageEncoding="utf-8" %>
<%@include file="/WEB-INF/jspf/html.jspf" %>
<tags:layout titleCode='dashboard.pageTitle'>
    <jsp:body>
        <div class="px-3">
                <%--            https://getbootstrap.com/docs/5.3/examples/dashboard/--%>
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h1 class="h2">Previous loads</h1>
            </div>

            <div class="table-responsive mb-5">
                <c:choose>
                    <c:when test="${not empty inputLoads}">
                        <table class="table">
                            <thead>
                            <tr>
                                <th>#</th>
                                <th class="text-start">Status</th>
                                <th>File</th>
                                <th class="text-center">Start</th>
                                <th class="text-center">Finish</th>
                                <th class="text-end">Progress</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="inputLoad" items="${inputLoads}">
                                <tr class="align-baseline">
                                    <td>${inputLoad.id}</td>
                                    <td class="text-start"><small><em>${inputLoad.status}</em></small></td>
                                    <td>
                                        <span>${h.h(inputLoad.path)}</span>
                                        <c:if test="${not empty inputLoad.loadError}">
                                            <div class="text-danger ">
                                                <small>${h.h(inputLoad.loadError)}</small>
                                            </div>
                                        </c:if>
                                    </td>
                                    <td class="text-center">${inputLoad.loadStart}</td>
                                    <td class="text-center">${inputLoad.loadFinish}</td>
                                    <td class="text-end">${inputLoad.loadProgress}</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:when>
                    <c:otherwise>
                        <div class="m-3 text-center"><em>nothing has been loaded yet</em></div>
                    </c:otherwise>
                </c:choose>
            </div>


            <h2>Available for load in <span class="text-muted">${inputFilesFolder}</span></h2>

            <c:choose>
                <c:when test="${not empty inputFiles}">
                    <table class="table table-hover">
                        <thead>
                        <tr>
                            <th>File</th>
                            <th class="text-center">Modification time</th>
                            <th class="text-end">Size</th>
                            <th class="text-end">Action</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="inputFile" items="${inputFiles}">
                            <tr class="align-middle">
                                <td> ${h.h(inputFile.path)}</td>
                                <td class="text-center">${inputFile.modificationTime}</td>
                                <td class="text-end">${inputFile.size}</td>
                                <td class="text-end">
                                    <form method="post" action="${cp}/inputs/load">
                                        <input type="hidden" name="path" value="${h.h(inputFile.path)}"/>
                                        <button type="submit" class="btn btn-primary">Load &amp; Analyze</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <div class="m-3 text-center"><em>nothing to load in that folder</em></div>
                </c:otherwise>
            </c:choose>
        </div>
    </jsp:body>
</tags:layout>