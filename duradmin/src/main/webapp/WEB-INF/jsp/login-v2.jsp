<%@include file="/WEB-INF/jsp/include.jsp" %>
<tiles:insertDefinition name="app-base" >
	<tiles:putAttribute name="title">
		Login
	</tiles:putAttribute>

	<tiles:putAttribute name="header-extensions">
		<link rel="stylesheet"  href="${pageContext.request.contextPath}/style/login.css" type="text/css" />
	</tiles:putAttribute>

	<tiles:putAttribute name="body">
		<script type="text/javascript">
		$(function() {
			$("#username").focus();
			$("#button-login").click(function(evt) {
				evt.stopPropagation();
				dc.login($("#loginForm"));
			});
		});
		</script>

		<form id="loginForm" action="${pageContext.request.contextPath}/login"  method="post" onsubmit="return false;" >
			<div id="login-wrapper">
				<div id="login-header" class="outer clearfix">
					<div id="dc-logo-panel"><a href="${pageContext.request.contextPath}/spaces" id="dc-logo"><img src="${pageContext.request.contextPath}/images/logo_top_duracloud_lg.png" alt="DURACLOUD"/></a></div>
				</div>
				<div id="login-content" class="pane-L1-body clearfix">
					<div id="form-fields" class="form-fields float-r">
						<div id="msg-error" class="error" style="display:none">Username/Password combination not valid. Please try again.</div>

						<ul>
							<li class="clearfix">
								<label for="username">Username</label>
								<input type="text" id="username" name="username" class="field"/>
							</li>
							<li class="clearfix">
								<label for="password">Password</label>
								<input id="password" type="password" name="password" class="field"/>
							</li>
							<li class="clearfix">
								<button id="button-login" class="primary  float-r">Login</button>
								<span id="feedback" style="display:none; color:white" class="primary  float-r"><img src="/duradmin/images/wait.gif"/>Logging in...</span>

							</li>
						</ul>
						<div id="login-links">
						    <ul class="horizontal-list">
							    <li><a href="${amaUrl}/users/forgot-password" target="_blank">Forgot Password</a></li>
						    </ul>
						</div>
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
