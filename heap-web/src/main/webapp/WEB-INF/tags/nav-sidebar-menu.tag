<%@tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8" %>
<%@include file="/WEB-INF/jspf/taglibs.jspf" %>

<div class="d-flex flex-column flex-shrink-0 p-3 text-bg-dark" style="width: 280px;">
    <a href="/" class="d-flex align-items-center mb-3 mb-md-0 me-md-auto text-white text-decoration-none">
        <svg class="bi pe-none me-2" width="40" height="32">
            <use xlink:href="#bootstrap"></use>
        </svg>
        <span class="fs-4">Sidebar</span>
    </a>
    <hr>
    <ul class="nav nav-pills flex-column mb-auto">
<%--        
        <li class="nav-item">
            <a href="#" class="nav-link active" aria-current="page">
                <svg class="bi pe-none me-2" width="16" height="16">
                    <use xlink:href="#home"></use>
                </svg>
                Home
            </a>
        </li>
--%>        
        <li class="nav-item">
            <a href="/inputs" class="nav-link text-white">
                <svg class="bi pe-none me-2" width="16" height="16">
                    <use xlink:href="#speedometer2"></use>
                </svg>
                Heapdump files
            </a>
        </li>
<%--       
        <li>
            <a href="#" class="nav-link text-white">
                <svg class="bi pe-none me-2" width="16" height="16">
                    <use xlink:href="#people-circle"></use>
                </svg>
                Customers
            </a>
        </li>
 --%>        
    </ul>
    <hr>
</div>
