<%@include file="/WEB-INF/jsp/include.jsp" %>
<tiles:insertDefinition name="app-base" >
	<tiles:putAttribute name="title">
		Administration	
	</tiles:putAttribute>

	<tiles:putAttribute name="header-extensions">
		<script type="text/javascript">
		  var users = null;
		  $(document).ready(function(){
				users =
					 [
						<c:forEach var="user" items="${users}">
						{
							username: '${user.username}',
							enabled: '${user.enabled}',
							accountNonExpired: '${user.accountNonExpired}',
							accountNonLocked: '${user.accountNonLocked}',
							grantedAuthorities: '${user.topAuthorityDisplay}'
						},
						</c:forEach>				
					];
		  });
		</script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/admin-manager.js"></script>
		
	</tiles:putAttribute>
	
	<tiles:putAttribute name="body">

	<tiles:insertDefinition name="app-frame">
		<tiles:putAttribute name="mainTab">admin</tiles:putAttribute>

		<tiles:putAttribute name="main-content">
	   		<div id="users-list-view" class="list-browser dc-list-item-viewer">
				<div class="north header list-header clearfix">
					<div id="header-spaces-list" class="header-section clearfix">
						<h2>Users</h2>
					</div>
				</div>
			
				<div class="center dc-item-list-wrapper">
					<div id="users-list" class="dc-item-list">
						<p>No users defined</p>
					</div>			
				</div>
			</div>

			<div id="detail-pane" class="detail-pane" style="padding-top:10px;">
				
			</div>

			<div id="user-detail-pane" style="display:none">
				<div class="north header">
					<h1>User Detail</h1>
					<h2>
                        <span class="user-name"> Name</span> :
                        <span class="roles">Roles</span>
                    </h2>
				</div>
				<div class="center">
				</div>

			</div>	

		</tiles:putAttribute>
		<tiles:putAttribute name="main-footer">
				<div id="status-holder">
				</div>
		</tiles:putAttribute>
</tiles:insertDefinition>	
	</tiles:putAttribute>
</tiles:insertDefinition>