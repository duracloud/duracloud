<%-- Copyright (c) 2009-2013 DuraSpace. All rights reserved.--%>
<%-- Log Page: displays log of the synchronization process. --%>
<%-- Author: Daniel Bernstein --%>

<%@include file="./include/libraries.jsp"%>
<tiles:insertDefinition
  name="app-base"
  flush="true">

  <tiles:putAttribute name="primaryTab" value="log" cascade="true"/>
  
  <tiles:putAttribute
    name="content"
    cascade="true">
    <div id="log">
      <p>This feature has not yet been implemented.</p>
    </div>
  </tiles:putAttribute>
</tiles:insertDefinition>

