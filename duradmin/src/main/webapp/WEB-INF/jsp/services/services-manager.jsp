<%@include file="/WEB-INF/jsp/include.jsp" %>
<tiles:insertDefinition name="app-base" >
	<tiles:putAttribute name="title">
		<spring:message code="services" />	
	</tiles:putAttribute>

	<tiles:putAttribute name="header-extensions">
		<script type="text/javascript" src="${pageContext.request.contextPath}/javascript/services-manager.js"></script>
	</tiles:putAttribute>
	<tiles:putAttribute name="body">
	<tiles:insertDefinition name="app-frame">
		<tiles:putAttribute name="mainTab">services</tiles:putAttribute>

		<tiles:putAttribute name="main-content">
	   		<div id="services-list-view" class="list-browser dc-list-item-viewer">
				<div class="north header list-header clearfix">
					<div id="header-spaces-list" class="header-section clearfix">							
						<button class="float-r deploy-service-button"><i class="pre plus"></i>Deploy a New Service</button>
						<h2>Services</h2>
					</div>
				</div>
			
				<div class="center dc-item-list-wrapper">
					<div id="deployed-services" class="dc-item-list">
						<table id="deployed-services-table" style="display:none">
							<thead>
								<tr>
									<th>&nbsp;</th>
									<th>Service</th>
									<th>Hostname</th>
									<th>Status</th>
								</tr>
							</thead>
							<tbody  id="services-list">
							</tbody>
						</table>
					</div>			
				</div>
			</div>

			<div id="detail-pane" class="detail-pane" style="padding-top:10px;">
				
			</div>

			<div id="service-detail-pane" style="display:none">
				<div class="north header">
					<h1>Service Detail</h1>
					<h2>
						<span class="service-name"> Name</span> 
						<span class="service-version">Version</span>
						<input type="hidden" value="" id="deployment-id"/>
					</h2>
					<div class="toggle-control switch-holder">
					    <div class="r">
					    	<span>
					    		Status
					    	</span>
					    	<span class="deploy-switch">
					    		[deployed] [undeploy]
					    	</span>
					    </div>
					</div>

					<div class="button-bar">
						<button class="reconfigure-button">Reconfigure</button>
					</div>

				</div>
				<div class="center">
				</div>

			</div>	
			
			<div id="available-services-dialog" class="dialog detail-pane">
				<h1>Select a Service</h1>
				<div class="dc-item-list-wrapper" id="dc-item-list-wrapper"> 
					<div id="available-services-list-wrapper" class="dc-item-list">
						<span class="dc-message">Loading...</span>
						<table>
							<tbody id="available-services-list">
							</tbody>
						</table>
					</div>
				 </div> 
			
				<div class="dc-service-detail-wrapper" id="dc-service-detail-wrapper"> 
					<div id="service-detail" class="dialog-detail">
						<p>Select a service to get more details about it</p>
					</div>
				</div> 
			</div>

			<div id="reconfigure-service-dialog" class="dialog" title="Reconfigure Service">
				<h1>Reconfigure the Service</h1>
				<div id="reconfigure-service-config">
				</div>
			</div>

			<div id="configure-service-dialog" class="dialog" title="Configure Service">
				<h1>Configure the Service</h1>
				<p class="hint">Configure your service, then click "Deploy"</p>
				<div id="deploy-service-config">
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