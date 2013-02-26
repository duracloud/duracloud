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
   cascade="true"> </tiles:putAttribute>

  <tiles:putAttribute
   name="panelContent"
   cascade="true">

      <%--
        This bit of ugliness is necessary in order to separate out
        errors that are associated with the object duracloudCredentialsForm
        as a whole but not a particular field.  
        
        It seems this is a little bit broken in spring. errors.globalErrors should
        work, but technically the errors associated with a class are not technically
        global, and thus they are not returned by the errors.getGlobalErrors() method.
       --%>
      <spring:hasBindErrors name="duracloudCredentialsForm">
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
                
                 <form:hidden
                   
                   cssErrorClass="error"
                   path="port"
                   id="port"
                   placeholder="default: 443"
                    />
                    
                
            <form:errors
             path="host"
             cssClass="error"
             element="div" />
             </div>
             
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

