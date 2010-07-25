<%@include file="/WEB-INF/jsp/include.jsp" %>
<tiles:insertDefinition name="app-base" >
	<tiles:putAttribute name="title">
		Duradmin: Login
	</tiles:putAttribute>
	
	<tiles:putAttribute name="header-extensions">
		<link rel="stylesheet"  href="${pageContext.request.contextPath}/style/login.css" type="text/css" />
	</tiles:putAttribute>
	
	<tiles:putAttribute name="body">
		<form id="loginForm" action="${pageContext.request.contextPath}/j_spring_security_check" method="post" >
			<div id="login-wrapper">
				<div id="login-header" class="outer clearfix">
					<div id="dc-logo-panel"><a href="/duradmin/spaces" id="dc-logo"></a><span id="dc-app-title"></span></div>			
				</div>
				<script type="text/javascript">
					$(document).ready(function(){
						$("#username").focus();
					});
				
				</script>
				<div id="login-content" class="pane-L1-body clearfix">
					<h1 id="title" class="float-l">Login</h1>
					<div id="form-fields" class="form-fields float-r">
						<div id="msg-error" class="error" style="display:none">Username/Password combination not valid. Please try again.</div>
						<ul>
							<li class="clearfix">
								<label for="j_username">Username</label>							
								<input type="text" id="username" name="j_username" class="field"/>	
							</li>
							<li class="clearfix">
								<label for="j_password">Password</label>
								<input type="password" name="j_password" class="field"/>
							</li>
							<li class="clearfix">
								<label><a href="#" id="forgot-password" class="helper-link">Forgot Password?</a></label>
								<button id="button-login" class="primary  float-r">Login</button>											
							</li>
						</ul>
					
					</div>
				</div>
				
				<div id="login-footer" class="outer footer clearfix">
					<div class="footer-content">		
						<%@include file="/WEB-INF/jsp/include/footer.jsp" %>
					</div>
				</div>
			</div>
		</form>
	</tiles:putAttribute>
</tiles:insertDefinition>
