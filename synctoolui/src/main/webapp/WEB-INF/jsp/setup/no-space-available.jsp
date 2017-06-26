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
   cascade="true">Sync Setup</tiles:putAttribute>

  <tiles:putAttribute
   name="panelMessage"
   cascade="true">
   <div>
    <h2>
       There is a minor problem: you do not currently have any available spaces configured.
    </h2>
    <p>In order to continue you must have write access to at least one space in your account. 
    Please assign appropriate permissions to the space to which you would like to sync. 
    If you're not sure how to proceed, please contact your DuraCloud administrator.</p>
   </div>
   
   </tiles:putAttribute>

  <tiles:putAttribute
   name="panelContent"
   cascade="true">
    <form
     method="POST">
      <fieldset
       class="button-bar">
        <button
         id="cancel"
         type="submit"
         name="_eventId_cancel">
          <spring:message
           code="cancel" />
        </button>

      </fieldset>
    </form>
  </tiles:putAttribute>
</tiles:insertDefinition>

