<%-- Copyright (c) 2009-2013 DuraSpace. All rights reserved.--%>
<%-- Log Page: displays log of the synchronization process. --%>
<%-- Author: Daniel Bernstein --%>

<%@include file="./include/libraries.jsp"%>
<tiles:insertDefinition
  name="basic-panel">
  <tiles:putAttribute
   name="panelTitle">Add Directory</tiles:putAttribute>

  <tiles:putAttribute
   name="panelContent">
    <jsp:include page="/WEB-INF/jsp/include/directoryForm.jsp" />
  </tiles:putAttribute>
</tiles:insertDefinition>


