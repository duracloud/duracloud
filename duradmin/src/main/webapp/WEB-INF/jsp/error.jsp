<%@include file="/WEB-INF/jsp/include.jsp" %>
<tiles:insertDefinition name="app-base" >
	<tiles:putAttribute name="title">
		Duradmin: 		<spring:message code="unexpectedError" /> 	

	</tiles:putAttribute>
	
	<tiles:putAttribute name="header-extensions">
		<link rel="stylesheet"  href="${pageContext.request.contextPath}/style/error.css" type="text/css" />
	</tiles:putAttribute>
	
	<tiles:putAttribute name="body">
			<div id="error-wrapper">
				<div id="error-header" class="outer clearfix">
					<div id="dc-logo-panel"><a href="/duradmin/spaces" id="dc-logo"></a><span id="dc-app-title"></span></div>			
				</div>
				<div id="error-content" class="pane-L1-body clearfix">
					<h1 id="title" class="float-l error">${error}</h1>
					<div id="msg-error" class="error">
						<c:out value="${message}"/>
					</div>
					
					<c:if test="${not empty stack}">
					<div id="stack-div" class="message-error">
						<c:out value="${stack}"/>
					</div>
					</c:if>				
				</div>
				
				<div id="error-footer" class="outer footer clearfix">
					<div class="footer-content">		
						<%@include file="/WEB-INF/jsp/include/footer.jsp" %>
					</div>
				</div>
			</div>
	</tiles:putAttribute>
</tiles:insertDefinition>
