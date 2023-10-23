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
   cascade="true">Enter Your DuraCloud Account Info</tiles:putAttribute>

  <tiles:putAttribute
   name="panelMessage"
   cascade="true"
   value="" />

  <tiles:putAttribute
   name="panelContent"
   cascade="true">
      <jsp:include page="../include/messages.jsp">
        <jsp:param name="form" value="duracloudCredentialsForm"/>
      </jsp:include>

    <form:form
     method="POST"
     modelAttribute="duracloudCredentialsForm">

      <fieldset>
        <ol>
          <li>
            <form:label
             cssErrorClass="error"
             path="host">
              <spring:message
               code="host" /> 
            </form:label>
            <div class="fieldgroup">
              <form:input
                cssErrorClass="error"
                path="host" 
                placeholder="e.g. myinstance.duracloud.org"
                autofocus="true" 
                />
                
             
             <c:if test="${duracloudCredentialsForm.defaultPort}">
             <a href="#" id="advanced">
              Advanced
             </a>
             </c:if>             
            
            <form:errors
             path="host"
             cssClass="error"
             element="div" />
             </div>
             
          </li>  
          
          <li id="portListItem" style="display:${duracloudCredentialsForm.defaultPort ? 'none' : ''}">
            <form:label
             cssErrorClass="error"
             path="port">
              <spring:message
               code="port" /> 
            </form:label>
            <div class="fieldgroup">
              <form:input
                cssErrorClass="error"
                path="port" 
                placeholder=""
                autofocus="true" 
                />
                
            <form:errors
             path="port"
             cssClass="error"
             element="div" />
             </div>
          
          
          </li>
          <li>
            <form:label
             cssErrorClass="error"
             path="username">
              <spring:message
               code="username" />
            </form:label>
            
            <div class="fieldgroup">
              <form:input
               cssErrorClass="error"
               path="username"
               placeholder="Your DuraCloud username here"
               />
  
              <form:errors
               path="username"
               cssClass="error"
               element="span" />
             </div>
            
          </li>

          <li>
            <form:label
             cssErrorClass="error"
             path="password">
              <spring:message
               code="password" />
            </form:label>

            <div class="fieldgroup">
              <form:password
               cssErrorClass="error"
               path="password" 
               showPassword="true"
               />
  
              <form:errors
               path="password"
               cssClass="error"
               element="div" />
             </div>
          </li>             
             
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

