<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:insertDefinition name="app-base">
	<tiles:putAttribute name="title">
		<spring:message code="services" />
	</tiles:putAttribute>

	<tiles:putAttribute name="header-extensions">
		<script type="text/javascript"
			src="${pageContext.request.contextPath}/js/dashboard-manager.js"></script>
		<script type="text/javascript"
			src="${pageContext.request.contextPath}/jquery/plugins/jquery.flot/jquery.flot.js"></script>
		<script type="text/javascript"
			src="${pageContext.request.contextPath}/jquery/plugins/jquery.flot/jquery.flot.pie.js"></script>
		<style>
#report-date-slider-wrapper {
	min-width: 300px;
	max-width: 300px;
	margin-left: 10px;
	margin-right: 10px;
}

#report-pane {
	/*padding-left: 10px;*/
}

.dc-slider-value { /*padding-top:10px;*/
	font-size: 1.5em;
	min-width: 300px;
}

.dc-date-slider {
	
}

.dc-navigation {
	
}

.dc-small-graph-panel>h3,.dc-small-graph-panel>div {
	padding: 5px;
}

.dc-small-graph-panel {
	
}

.center {
	overflow: auto;
}

.dc-navigation
{
	border-bottom:1px solid #999;
	/*background:#414141;*/
	background:#EFEFEF;
	
	color:#555;
	cursor:pointer;
	font-size:13px;
	line-height:25px;
	padding:5px;
	
}

.dc-navigation > div {
	display:inline-block;	
}

#main-content-tabs > div {
	background:#FFFFFF;
	overflow:auto;
	min-height:500px;
	color:#555;
}
#main-content-panel {
	overflow:auto !important;
}

.ui-widget-content { border: 1px solid #aaaaaa; background: #ffffff url(images/ui-bg_flat_75_ffffff_40x100.png) 50% 50% repeat-x; color: #222222;

</style>

	</tiles:putAttribute>
	<tiles:putAttribute name="body">
		<tiles:insertDefinition name="app-frame">
			<tiles:putAttribute name="mainTab">dashboard</tiles:putAttribute>

			<tiles:putAttribute name="main-content">
				<!-- 
				<div class="center-north" id="center-pane-north">
				<div class="float-l">
				<h1>Welcome</h1>
				</div>
				</div>
				 -->
				
				<div id="dynamic-panel">
				<div id="announcements"></div>
				<div id="current-users"></div>
				</div>
				<div id="main-content-panel">
				<div id="main-content-tabs">
				<ul>
					<li><a href="#tabs-overview"><span>Overview</span></a></li>
					<li><a href="#tabs-storage"><span>Storage</span></a></li>
					<li><a href="#tabs-services"><span>Services</span></a></li>
				</ul>
				<div id="tabs-overview">
					<p>Overview of storage and service reports will go here.</p>
				</div>
				<div id="tabs-services">Service report browser will go here.</div>

				<div id="tabs-storage">

				<div class="north dc-report-panel ">
				<div class="dc-navigation">
				<div class="dc-slider-value"><span id="report-selected-date"></span></div>

				<div class="dc-date-slider">
				<div id="report-start-range"></div>
				<div id="report-date-slider-wrapper">
				<div id="report-date-slider"></div>
				</div>
				<div id="report-end-range"></div>
				</div>
				</div>
				<div id="report-breadcrumb" class="dc-breadcrumb"></div>
				</div>
				<div class="center">
				<div id="report-pane"></div>
				<div id="storage-providers" class="dc-hidden">
				<table>
					<tr>
						<td>
						<div id="bytes"></div>

						</td>
						<td>
						<div id="files"></div>
						</td>
					</tr>
					<tr>
						<td>
						<div id="mimetype-bytes"></div>
						</td>
						<td>
						<div id="mimetype-files"></div>
						</td>
					</tr>
				</table>
				</div>
				<div id="storage-provider" class="dc-hidden">
				<table>
					<tr>
						<td>
						<div id="bytes"></div>

						</td>
						<td>
						<div id="files"></div>
						</td>
					</tr>
					<tr>
						<td>
						<div id="mimetype-bytes"></div>
						</td>
						<td>
						<div id="mimetype-files"></div>
						</td>
					</tr>
				</table>
				</div>
				<div id="space" class="dc-hidden">
				<table>
					<tr>
						<td>
						<div id="mimetype-bytes"></div>
						</td>
						<td>
						<div id="mimetype-files"></div>
						</td>
					</tr>
				</table>
				</div>
				</div>
				</div>
				</div>

				</div>


				<!-- 
						<h2>Getting Started with Duracloud</h2>
						<p>
						DuraCloud is particularly focused on providing preservation support services and access services for academic libraries, academic research centers, and other cultural heritage organizations.
						</p>
						<p>
						The service builds on the pure storage from expert storage providers by overlaying the access functionality and preservation support tools that are essential to ensuring long-term access and durability. DuraCloud offers cloud storage across multiple commercial and non commercial providers, and offers compute services that are key to unlocking the value of digital content stored in the cloud. DuraCloud provides services that enable digital preservation, data access, transformation, and data sharing. Customers are offered "elastic capacity" coupled with a "pay as you go" approach. DuraCloud is appropriate for individuals, single institutions, or for multiple organizations that want to use cross-institutional infrastructure.
						</p>

						<p>
						You can get started by <a href="<c:url value='/spaces'/>">adding a space</a> or 
						 <a href="<c:url value='/services'/>">deploying a service</a>
						</p>						
						<h3>Useful Links</h3>
						
						<ul>
							<li><a href="http://wiki.duraspace.org/display/duracloud/DuraCloud">Wiki</a></li>
							<li><a href="http://jira.duraspace.org/browse/DURACLOUD">Issue Tracker</a></li>
							<li><a href="http://duracloud.org">Duracloud.org</a></li>
						
						</ul>
					 -->

			</tiles:putAttribute>
			<tiles:putAttribute name="main-footer">
				<div id="status-holder"></div>
			</tiles:putAttribute>
		</tiles:insertDefinition>
	</tiles:putAttribute>
</tiles:insertDefinition>



