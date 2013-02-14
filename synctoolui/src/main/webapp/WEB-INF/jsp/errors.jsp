<%-- Copyright (c) 2009-2013 DuraSpace. All rights reserved.--%>
<%-- Status Page: displays errors for the synchronization process. --%>
<%-- Author: Daniel Bernstein --%>

<%@include file="./include/libraries.jsp"%>
<tiles:insertDefinition
  name="app-base"
  flush="true">
  <tiles:putAttribute
    name="content"
    cascade="true">Errors Content Goes Here</tiles:putAttribute>
</tiles:insertDefinition>

