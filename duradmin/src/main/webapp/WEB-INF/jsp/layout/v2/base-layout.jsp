<%@include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<div id="page-header" class="outer">
	<div id="left" class="float-l">
		<div id="dc-logo-panel"><a href="/duradmin/spaces" id="dc-logo"></a></div>
		<div id="dc-tabs-panel">
		    <ul class="horizontal-list dc-main-tabs flex clearfix">
		    	<tiles:importAttribute name="mainTab" />
                <sec:authorize ifAnyGranted="ROLE_ROOT">
		        <li id="dashboard-tab" class="${mainTab == 'dashboard' ? 'selected':'' }"><a href="${pageContext.request.contextPath}/dashboard"><span>Dashboard</span></a></li>
                </sec:authorize>

                <sec:authorize ifAnyGranted="ROLE_ADMIN,ROLE_ROOT">
    	        <li id="spaces-tab" class="${mainTab == 'spaces' ? 'selected':'' }"><a href="${pageContext.request.contextPath}/spaces"><span>Spaces</span></a></li>
                </sec:authorize>
                <sec:authorize ifAnyGranted="ROLE_ADMIN">
                    <li id="services-tab" class="${mainTab == 'services' ? 'selected':'' }"><a href="${pageContext.request.contextPath}/services"><span>Services</span></a></li>
                </sec:authorize>
                <sec:authorize ifAnyGranted="ROLE_ROOT">
                <li class="${mainTab == 'admin' ? 'selected':'' }"><a href="${pageContext.request.contextPath}/admin"><span>Administration</span></a></li>
                </sec:authorize>

		    </ul>
		</div>
	</div>	
	<div id="right" class="float-r">
		<img class="float-r" id="dc-partner-logo" src="/duradmin/partnerlogo"/>
		<div id="dc-user" class="float-r">
      ${pageContext.request.userPrincipal.name}
      <ul
        class="horizontal-list"
        style="margin-top: 10px;">
        <li id="gettools"><a
          target="_blank"
          class="icon-link"
          href="https://wiki.duraspace.org/display/DURACLOUD/DuraCloud+Downloads"><i
            class="pre gettools"></i>Get Sync Tools</a></li>
        <li id="help"><a
          target="_blank"
          class="icon-link"
          href="https://wiki.duraspace.org/display/DURACLOUD/DuraCloud+Help+Center"><i
            class="pre help"></i>Help</a></li>
        <li><a
          class="icon-link"
          href='<c:url value="/logout"/>'
          class="logout"><i class="pre logoff"></i>Logout</a></li>
      </ul>
    </div>			
	</div>
</div>
<div id="page-content" class="pane-L1-body">
 	<tiles:insertAttribute name="main-content" />
</div>
<div class="ui-layout-south footer">
	 <tiles:insertAttribute name="main-footer" />
	<div class="outer" id="footer-content">
		<%@include file="/WEB-INF/jsp/include/footer.jsp" %>
	</div>

</div>	

<div id="busy-dialog" class="dialog" style="display:none">
	<h2 id="busy-dialog-title"></h2>
	<div id="busy-dialog-progressbar">
	
	</div>
</div>
<div id="message-dialog" class="dialog" style="display:none">
	<h2 id="message-dialog-title"></h2>
	<div id="message-dialog-content">
	
	</div>
</div>
	
