<?xml version="1.0" encoding="ISO-8859-1"?>
<%-- Copyright (c) 2009-2013 DuraSpace. All rights reserved.--%><%-- Status
Page: displays configuration information for the synchronization process.
--%><%-- Author: Daniel Bernstein --%><%@include
file="../include/libraries.jsp"%>
<tiles:insertDefinition
 name="setup-wizard"
 flush="true">
  <tiles:putAttribute
   name="title">Setup</tiles:putAttribute>

  <tiles:putAttribute
   name="panelTitle"
   cascade="true">Select Space</tiles:putAttribute>

  <tiles:putAttribute
   name="panelMessage"
   cascade="true">
    <p>Select a destination space for your content.</p>
   
   </tiles:putAttribute>

  <tiles:putAttribute
   name="panelContent"
   cascade="true">

      <spring:hasBindErrors name="spaceForm">
        <c:if test="${fn:length(errors.allErrors) > 0}">
            <ul class="global-errors">
              <c:forEach
                items="${errors.allErrors}"
                var="errorMessage">
                  <li><c:out value="${errorMessage.defaultMessage}" /></li>
              </c:forEach>
            </ul>
          </c:if>
      </spring:hasBindErrors>

    <form:form
     method="POST"
     modelAttribute="spaceForm">
      <fieldset>
        <ol>
          <li>
            <form:label
             path="spaceId">
              <spring:message
               code="spaceId" />
            </form:label>

            <form:select
             path="spaceId"
             items="${spaces}"
             autofocus="true"/>
          </li>
        </ol>
      </fieldset>

      <fieldset
       class="button-bar">
        <button
         id="next"
         type="submit"
         name="_eventId_next">
          <spring:message
           code="next" />
        </button>

        <button
         id="cancel"
         type="submit"
         name="_eventId_cancel">
          <spring:message
           code="cancel" />
        </button>
      </fieldset>
    </form:form>
  </tiles:putAttribute>
</tiles:insertDefinition>

