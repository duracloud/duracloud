<%@include file="/WEB-INF/jsp/include.jsp" %>
<tiles:insertDefinition name="app-base" >
	<tiles:putAttribute name="title">
      ${title}
	</tiles:putAttribute>

	<tiles:putAttribute name="header-extensions">
    <style>
      body {
        margin-right: 0px !important;
        overflow: auto !important;
    }
    </style>
	</tiles:putAttribute>
	<tiles:putAttribute name="body">
        <h2>${title}</h2>
        <a class="button" href="${reportLink}">Download Raw Report</a>
        <table class="tablesorter">
          <thead>
          <c:forEach items="${data}" var="row" varStatus="status" end="0">
            <tr>
              <c:forEach items="${row}" var="cell">
                <th>
                    <c:out value="${cell}"/>                      
                </th>
              </c:forEach>              
            </tr>
          </c:forEach>
          </thead>
          <tbody>
            <c:forEach items="${data}" var="row">
              <tr>
              <c:forEach items="${row}" var="cell">
                <td>
                    <c:out value="${cell}"/>                      
                </td>
              </c:forEach>              
              </tr>
             </c:forEach>
          </tbody>
        </table>
    </tiles:putAttribute>
</tiles:insertDefinition>	
