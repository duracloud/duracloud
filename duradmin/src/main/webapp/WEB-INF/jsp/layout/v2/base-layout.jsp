<%@include file="/WEB-INF/jsp/include.jsp" %>
<div id="page-header" class="outer">
	<div id="left" class="float-l">
		<div id="dc-logo-panel"><a href="/duradmin/spaces" id="dc-logo"></a><span id="dc-app-title"></span></div>
		<div id="dc-tabs-panel">
		    <ul class="horizontal-list dc-main-tabs flex clearfix">
		    	<tiles:importAttribute name="mainTab" />
		        <li class="${mainTab == 'dashboard' ? 'selected':'' }"><a href="${pageContext.request.contextPath}/dashboard"><span>Dashboard</span></a></li>
		        <li class="${mainTab == 'spaces' ? 'selected':'' }"><a href="${pageContext.request.contextPath}/spaces"><span>Spaces</span></a></li>
		        <li class="${mainTab == 'services' ? 'selected':'' }"><a href="${pageContext.request.contextPath}/services"><span>Services</span></a></li>
		        <li class="${mainTab == 'admin' ? 'selected':'' }"><a href="${pageContext.request.contextPath}/admin"><span>Administration</span></a></li>
		        <!-- 
		        <li class="${mainTab == 'reports' ? 'selected':'' }"><a href="javascript:void(1); alert('Reports click')"><span>Reports</span></a></li>
		     	-->
		        
		    </ul>
		</div>
	</div>	
	<div id="right" class="float-r">
		<img class="float-r" id="dc-partner-logo" src="/duradmin/images/partner_logo_placeholder.png"/>
		<div id="dc-user" class="float-r">
			${pageContext.request.userPrincipal.name}
			<ul class="horizontal-list" style="margin-top:10px;">
				<li id="help"><a class="icon-link" href="#"><i class="pre help"></i>Help</a></li>
				<li><a class="icon-link" href='<c:url value="/logout"/>' class="logout"><i class="pre logoff"></i>Logout</a></li>
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
	