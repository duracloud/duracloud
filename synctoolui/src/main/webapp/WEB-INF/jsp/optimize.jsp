<%-- Copyright (c) 2009-2013 DuraSpace. All rights reserved.--%>
<%-- Log Page: displays log of the synchronization process. --%>
<%-- Author: Daniel Bernstein --%>

<%@include file="./include/libraries.jsp"%>
<tiles:insertDefinition
  name="basic-panel">
  <tiles:putAttribute
   name="panelTitle">Optimize</tiles:putAttribute>

  <tiles:putAttribute
   name="panelContent">
   <form:form>

   <c:choose>
   <c:when test="${not syncOptimizer.running}">
     <p>In order to optimize the threads, we'll run a series of test uploads
        from your local drive to your DuraCloud space. This process may take up to 30 minutes 
        to complete. Upon completion, the thread count will be modified automatically and, if
        necessary, the sync process will be restarted.</p>
        <fieldset>
        <label style="width:100%; text-align:left">
        <input name="autoStart" type="checkbox" value="true" checked="checked"/>
        Automatically start on completion
        </label>
        
        </fieldset>

   </c:when>
   <c:otherwise>
     A thread count optimization is already underway. 
   </c:otherwise>
   </c:choose>

   
        
        
       <fieldset
       class="button-bar">
        <c:if test="${not syncOptimizer.running}">
        <button
         id="optimize"
         type="submit"
         name="optimize">
          Optimize threads now
        </button>
        </c:if>
        <button
         id="cancel"
         type="submit">
          <spring:message
           code="cancel" />
        </button>
      </fieldset>
      </form:form>
  </tiles:putAttribute>
</tiles:insertDefinition>


