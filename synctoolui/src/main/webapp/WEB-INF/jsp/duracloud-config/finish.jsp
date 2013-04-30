<?xml version="1.0" encoding="ISO-8859-1"?>
<%-- Copyright (c) 2009-2013 DuraSpace. All rights reserved.--%><%-- Status
Page: displays configuration information for the synchronization process.
--%><%-- Author: Daniel Bernstein --%><%@include
file="../include/libraries.jsp"%>
<tiles:insertDefinition
 name="setup-wizard"
 flush="true">
  <tiles:putAttribute
   name="title">Sync Configuration Wizard</tiles:putAttribute>

  <tiles:putAttribute
   name="panelTitle"
   cascade="true">Sync Configuration Wizard</tiles:putAttribute>

  <tiles:putAttribute
   name="panelMessage"
   cascade="true">
   <div class="welcome">
    <div class="green-check">
    
    </div>
    <h1>
      Your settings have been successfully saved.
    </h1>
    
   </div>
   
   </tiles:putAttribute>

  <tiles:putAttribute
   name="panelContent"
   cascade="true">
   <c:set var="syncRunning" value="${syncState.name() == 'RUNNING'}"/>
    <c:if test="${syncRunning}">
    <p>
      In order for the sync tool to pick up the latest
      changes you must restart.
    </p> 
    </c:if>
    <form:form
     method="POST">
      <fieldset
       class="button-bar">
         <c:if test="${syncRunning}">
          <button
           id="restart"
           type="submit"
           name="_eventId_restart">
            Restart now
          </button>
         </c:if>
 
        <button
         id="end"
         type="submit"
         name="_eventId_end">
         <c:choose>
           <c:when test="${syncRunning}">
            Start later
           </c:when>
           <c:otherwise>
            Done
           </c:otherwise>
         </c:choose>          
        </button>
 
        
      </fieldset>
    </form:form>
  </tiles:putAttribute>
</tiles:insertDefinition>

