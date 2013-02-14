<%-- Copyright (c) 2009-2013 DuraSpace. All rights reserved.--%>
<%-- Status Page: displays errors for the synchronization process. --%>
<%-- Author: Daniel Bernstein --%>

<%@include file="./include/libraries.jsp"%>

<ul
  class="jqueryFileTree"
  style="display: none;">
  <c:forEach
    items="${children}"
    var="child">
    <c:if test="${child.directory}">
      <li class="directory collapsed"><a
        href="#"
        rel="${child.getAbsolutePath()}/"> 
        <%--on windows, child.getName() will be empty when called on root directories --%>
        <c:choose>
         <c:when test="${not empty child.getName()}">
          ${child.getName()}
         </c:when>
         <c:otherwise>
            ${child.getAbsolutePath()}
         </c:otherwise>
         </c:choose>
       </a></li>
    </c:if>
  </c:forEach>
  <c:forEach
    items="${children}"
    var="child">
    <c:if test="${!child.directory}">
    
      <c:set var="child" value="${child}" scope="request" />
      <%
          java.io.File child = (java.io.File)request.getAttribute("child"); 
          int dotIndex = child.getName().lastIndexOf('.');
          String ext =
              dotIndex > 0 ? child.getName().substring(dotIndex + 1) : "";
      %>

      <li class="file ext_${ext}"><a
        href="#"
        rel="${child.getAbsolutePath()}"> ${child.getName()}</a></li>
    </c:if>
  </c:forEach>
</ul>
