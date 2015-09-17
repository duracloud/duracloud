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
   cascade="true">Set Mode</tiles:putAttribute>

  <tiles:putAttribute
   name="panelMessage"
   cascade="true">
   
   </tiles:putAttribute>

  <tiles:putAttribute
   name="panelContent"
   cascade="true">
    <div class="options">
    <form:form
     method="POST"
     modelAttribute="modeForm">
     <jsp:include page="../include/modeForm.jsp"/>

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
    </div>
    
  </tiles:putAttribute>
</tiles:insertDefinition>

