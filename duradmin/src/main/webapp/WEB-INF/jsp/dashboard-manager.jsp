<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:insertDefinition name="app-base">
	<tiles:putAttribute name="title">
		<spring:message code="dashboard" />
	</tiles:putAttribute>

	<tiles:putAttribute name="header-extensions">
		<script type="text/javascript"
			src="${pageContext.request.contextPath}/js/dashboard-manager.js"></script>
		<script type="text/javascript"
			src="${pageContext.request.contextPath}/jquery/plugins/jquery.flot/jquery.flot.js"></script>
		<script type="text/javascript"
			src="${pageContext.request.contextPath}/jquery/plugins/jquery.flot/jquery.flot.pie.js"></script>
		<script type="text/javascript"
			src="${pageContext.request.contextPath}/jquery/plugins/jquery.tools/jquery.tools.min.js"></script>				
		<script type="text/javascript"
			src="${pageContext.request.contextPath}/jquery/plugins/jquery.tablesorter/jquery.tablesorter.min.js"></script>				
		<style>

	
#report-date-slider-wrapper {
	min-width: 250px;
	max-width: 250px;
	margin-left: 10px;
	margin-right: 10px;
}

#report-pane { /*padding-left: 10px;*/
	
}



.scrollable {

	/* required settings */
	position:relative;
	overflow:hidden;
	width: 1000px;
	height:400px;
}

.scrollable > .items {
	/* this cannot be too large */
	width:20000em;
	position:absolute;
}

.scrollable > .items > div {
	float:left;
	width:1000px;
}

.dc-slider-value { /*padding-top:10px;*/
	font-size: 1.5em;
	min-width: 250x;
}

.dc-date-slider {
	
}

.dc-navigation {
	width:49%;
	float:right;
}

.dc-small-graph-panel>h3,.dc-small-graph-panel>div {
	padding: 5px;
}


.dc-small-graph-panel
{
	border:1px solid #727576;
	padding:2px;
}


.center {
}

.dc-graph {
	width:480px;
	height:250px;
	background: #EEEEEE;
}

.dc-graph canvas {
	/*z-index:-1;*/
}

.dc-navigation {
	color: #555;
	cursor: pointer;
	font-size: 13px;
	line-height: 25px;
	padding: 5px;
}

.dc-breadcrumb {
	width:49%;
	float:left;
}



#main-content-tabs>div {
	background: #FFFFFF;
	/*
	overflow: auto;
	min-height: 500px;
	*/
	color: #555;
}

#main-content-panel {
	overflow: auto !important;
}

.ui-widget-content {
	border: 1px solid #aaaaaa;
	background: #ffffff url(images/ui-bg_flat_75_ffffff_40x100.png) 50% 50%
		repeat-x;
	color: #222222;
}

.north {
	height:60px;
	
}

.diptych > div {
  width:480px;
  margin:10px;
  display: inline-block;
}

.diptych .header {
	height:30px;
	width:100%;		
}

.diptych div.header > div:first-child {
	float:left;
	max-width:400px;
}

.diptych {
	width:1100px;
}

.diptych .button-panel {
	float:right;
}

.graph-switch a {
	color:black;
}

.back-link:hover{
	cursor: pointer;
}

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
				<div id="report-breadcrumb" class="dc-breadcrumb"></div>
				<div class="dc-navigation">
					<div class="dc-slider-value"><span id="report-selected-date"></span><a id="report-link" href="x">[xml]</a></div>
					<div class="dc-date-slider">
					<div id="report-start-range"></div>
					<div id="report-date-slider-wrapper">
						<div id="report-date-slider"></div>
					</div>
					<div id="report-end-range"></div>
				</div>
				</div>
				</div>
				<div class="center">
				
				<div class="scrollable">
				<div class="items">
				<div id="storage-summary">
					<div class="ui-widget-header ui-corner-all">
						<span class="graph-switch" >
							<input type="radio" id="entity-radio-0" class="entity-radio" name="radio0" checked="checked" /><label  for="entity-radio-0">Storage Providers</label>
							<input type="radio" id="mimetype-radio-0" class="mimetype-radio"  name="radio0" /><label for="mimetype-radio-0">File Type</label>
						</span>
					</div>
					<div class="diptych entity-panel">
						<div class="bytes-graph">
						</div>
						<div class="files-graph"></div>
					</div>
					<div class="diptych mimetype-panel" style="display:none">
						<div class="bytes-graph">

						</div>
						<div class="files-graph"></div>
					</div>

				</div>
				<div id="storage-provider">
					<div class="ui-widget-header ui-corner-all">
						<div class="graph-switch">
							<a href="#" class="back-link">&lt; Back</a>
							<input type="radio" id="entity-radio-1" class="entity-radio" name="radio" checked="checked" /><label  for="entity-radio-1">Spaces</label>
							<input type="radio" id="mimetype-radio-1" class="mimetype-radio"  name="radio" /><label for="mimetype-radio-1">File Type</label>
						</div>
					</div>
					<div class="diptych entity-panel">
						<div class="bytes-graph">

						</div>
						<div class="files-graph"></div>
					</div>
					<div class="diptych mimetype-panel" style="display:none">
						<div class="bytes-graph">

						</div>
						<div class="files-graph"></div>
					</div>

				</div>
				<div id="space">
					<div class="ui-widget-header ui-corner-all">
						<div class="graph-switch">
							<a href="#" class="back-link">&lt; Back</a>
						</div>
					</div>
					<div class="diptych mimetype-panel">
						<div class="bytes-graph">

						</div>
						<div class="files-graph"></div>
					</div>
				</div>
				</div>
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



