<?xml version="1.0" encoding="ISO-8859-1"?>
<%-- Copyright (c) 2009-2013 DuraSpace. All rights reserved.--%>
<%@include file="../libraries.jsp"%>

<p>The configuration was successfully updated.</p>
<c:if test="${syncProcessState.name() == 'RUNNING'}">
  <p>You must restart in order for the changes to take effect.</p>

  <form method="POST" action="${pageContext.request.contextPath}/status">
    <fieldset class="button-bar">
      <button
        id="restart"
        type="submit"
        name="restart">Restart now</button>
    </fieldset>
  </form>
</c:if>

