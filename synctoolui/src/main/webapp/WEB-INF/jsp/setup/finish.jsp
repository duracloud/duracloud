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
   <div class="welcome">
    <div class="green-check">
    
    </div>
    <h1>
      You're ready to start syncing!
    </h1>
    <p>Click the start button to begin syncing your content now.</p>
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
         id="startNow"
         type="submit"
         name="_eventId_startNow">
          <spring:message
           code="startNow" />
        </button>
        <button
         id="startLater"
         type="submit"
         name="_eventId_startLater">
          <spring:message
           code="continue" />
        </button>
        <button
         id="optimize">
          <!-- using a button here because the a link wasn't rendering properly
               and css problem was not clear to me. dbernstein -->
          <spring:message
           code="optimize" text="Optimize Now" />
        </button>
        <script>
    	    $("#optimize").click(function(e){
    	        e.preventDefault();
    	        window.location.href = 'configuration#optimize-now';
    	    });
        </script>

      </fieldset>
    </form>
  </tiles:putAttribute>
</tiles:insertDefinition>

