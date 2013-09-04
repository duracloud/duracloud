<?xml version="1.0" encoding="ISO-8859-1"?>
<%-- Copyright (c) 2009-2013 DuraSpace. All rights reserved.--%>
<%-- messages display
--%>
<%-- Author: Daniel Bernstein --%><%@include file="./libraries.jsp"%>

      <%--
        This bit of ugliness is necessary in order to separate out
        errors that are associated with the object duracloudCredentialsForm
        as a whole but not a particular field.  

        It seems this is a little bit broken in spring. errors.globalErrors should
        work, but the errors associated with a class are not technically global
        and thus they are not returned by the errors.getGlobalErrors() method.
       --%>
      <spring:hasBindErrors name="${param.form}">
        <c:if test="${fn:length(errors.allErrors) > 0}">
          <c:forEach
            items="${errors.allErrors}"
            var="errorMessage">
            <c:if test="${fn:length(errorMessage.field) == 0}">
              <c:set
                var="hasGlobal"
                value="true" />
            </c:if>
          </c:forEach>
          <c:if test="${hasGlobal}">

            <ul class="global-errors">
              <c:forEach
                items="${errors.allErrors}"
                var="errorMessage">
                <c:if test="${fn:length(errorMessage.field) == 0}">
                  <li><c:out value="${errorMessage.defaultMessage}" /></li>
                </c:if>
              </c:forEach>
            </ul>
          </c:if>
        </c:if>
      </spring:hasBindErrors>


