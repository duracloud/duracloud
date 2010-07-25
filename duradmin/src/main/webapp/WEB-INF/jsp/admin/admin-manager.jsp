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
							grantedAuthorities: '${user.grantedAuthorities}'
						},
						</c:forEach>				
					];
		  });
		</script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/javascript/admin-manager.js"></script>
		
	</tiles:putAttribute>
	
	<tiles:putAttribute name="body">

	<tiles:insertDefinition name="app-frame">
		<tiles:putAttribute name="mainTab">admin</tiles:putAttribute>

		<tiles:putAttribute name="main-content">
	   		<div id="users-list-view" class="list-browser dc-list-item-viewer">
				<div class="north header list-header clearfix">
					<div id="header-spaces-list" class="header-section clearfix">							
						<button class="float-r add-user-button"><i class="pre plus"></i>Add User</button>
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
						<span class="user-name"> Name</span> 
					</h2>
					<div class="button-bar">
						<button class="featured change-password-button"><i class="pre pencil"></i>Change Password</button>
						<button class="delete-user-button"><i class="pre trash"></i>Delete</button>

					</div>

				</div>
				<div class="center">
				</div>

			</div>	
			
			<div id="add-user-dialog" class="dialog detail-pane">
				<h1>Add User</h1>
				<p class="hint">All fields are required.</p>
				<form enctype="multipart/form-data" id="add-user-form">
				<input type="hidden" name="verb" value="add"/>
				<div id="form-fields" class="form-fields">
				<fieldset>
				<ul>
					<li class="row clearfix"><label for="username">Username</label><input type="text" name="username" id="username" class="field" /></li>
					<li class="row clearfix"><label for="password">Password</label><input type="password" name="password" id="password" class="field" /></li>
				</ul>
				</fieldset>
				</div>
				</form>

			</div>

			<div id="change-password-dialog" class="dialog detail-pane">
				<h1>Change Password for <span class="username"></span></h1>
				<p class="hint">All fields are required.</p>
				<form enctype="multipart/form-data" id="change-password-form">
				<input type="hidden" id="username" name="username" value=""/>
				<input type="hidden" name="verb" value="modify"/>

				<div id="form-fields" class="form-fields">
				<fieldset>
				<ul>
					<li class="row clearfix"><label for="newPassword">New Password</label><input type="password" name="password" id="newPassword" class="field" /></li>
				</ul>
				</fieldset>
				</div>
				</form>

			</div>

		</tiles:putAttribute>
		<tiles:putAttribute name="main-footer">
				<div id="status-holder">
				</div>
		</tiles:putAttribute>
</tiles:insertDefinition>	
	</tiles:putAttribute>
</tiles:insertDefinition>