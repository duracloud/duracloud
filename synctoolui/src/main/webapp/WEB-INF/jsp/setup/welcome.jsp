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
   cascade="true">Welcome!</tiles:putAttribute>

  <tiles:putAttribute
   name="panelMessage"
   cascade="true">
   </tiles:putAttribute>

  <tiles:putAttribute
   name="panelContent"
   cascade="true">
    <div class="welcome">
      <h1> 
        Welcome to the <br/>
        DuraCloud <br/>
        Sync Tool
      </h1>
      <p>
      Once set up, this application will automatically back up your vital
      digital files to DuraCloud, thus ensuring your data remains
      accessible and secure.
      </p>      
    </div>
    <form
     method="POST" action="${flowExecutionUrl}">
      <fieldset
       class="button-bar">
        <button
         id="next"
         type="submit"
         name="_eventId_next">
          <spring:message
           code="continue" />
        </button>
      </fieldset>
    </form>
  </tiles:putAttribute>
</tiles:insertDefinition>

