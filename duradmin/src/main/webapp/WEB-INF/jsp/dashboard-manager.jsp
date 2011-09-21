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

#report-date-slider, .date-slider {
  background-color:#6e6e6e;
}

#toolbar {
	padding: 5px 5px;
}


.scrollable {

	/* required settings */
	position:relative;
	overflow:hidden;
	width: 1100px;
	height:420px;
}

.scrollable > .items {
	/* this cannot be too large */
	width:20000em;
	position:absolute;
}

.scrollable > .items > div {
	float:left;
	width:1100px;
}

.dc-slider-value { /*padding-top:10px;*/
	font-size: 1.5em;
	min-width: 250x;
}

.dc-slider {
  background-color:#DDD;
}

.dc-navigation {
	width:49%;
    color: #555;
    cursor: pointer;
    font-size: 13px;
    line-height: 25px;
    padding: 5px;
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

.dc-graph-panel h5 {
	padding:5px;
}

.dc-graph-panel button {
	font-size:0.7em;
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



.dc-breadcrumb {
  width:49%;
  float:left;
  padding:5px;
}


a.download-button, a.back-link {
    height: 12px;
    line-height: 0px;
}

#main-content-tabs>div {
	background: #FFFFFF;
	color: #555;
    background: #9E9E9E url(../images/bg_list_browser_header.gif) no-repeat top right
}

#main-content-panel {
	overflow: auto !important;
}

.ui-widget-content {
/*
	border: 1px solid #aaaaaa;
	background: #ffffff url(images/ui-bg_flat_75_ffffff_40x100.png) 50% 50%
		repeat-x;
	color: #222222;
  background:#9e9e9e url(../images/bg_list_browser_header.gif) no-repeat top right !important;

 */
 
}


.highlight-box {
  	background-color: #DDD;
    border: 1px solid #EEE;
}

.north{
  height:100px;    
}

.diptych > div {
  width:480px;
  display: inline-block;
  padding:5px;
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
	/*color:black;*/
}

.back-link:hover{
	cursor: pointer;
}

.tickLabel a{
	color: #555555;
}

#service-list { list-style-type: none; margin: 0; padding: 0; width: 100%; }
#service-list li { margin: 3px; padding: 0.4em; }

.service {
	background-color: #EEEEEE;
	border-bottom: 1px solid #AAAAAA;
	opacity: 0.8;
	padding:3px;
}

#completion-status-list li {
	margin:3px;
	padding:3px;
}

.started-service {
	background-color: #CCFFCC;
}

.started-service .service-body {
	background-color: #DDFFDD;
}


.started-service .service-body table{
	background-color: #EEFFEE;
}

.successful-service {
	background-color: #AAFFAA;
}

.successful-service .service-body {
	background-color: #BBFFBB;
}


.successful-service .service-body table{
	background-color: #CCFFCC;
}


.failed-service {
	background-color: #FFCCCC;
}

.failed-service .service-body {
	background-color: #FFDDDD;
}

.failed-service .service-body table{
	background-color: #FFEEEE;
}


.service table {
	width:100%;
}


.service > .service-header:hover {
	cursor:pointer;
}

.service:hover {
	opacity: 1.0;
}

.service-header, .service-body {
	padding:5px;
}

.service-body {
	min-height: 100px;
	overflow: auto;
	padding: 5px;
}

.service-body h3 {
	font-weight:bolder;
}	

.service-body h3, .service-body td {
	padding:3px 3px 3px 3px; 
	font-size:0.8em;
}



.service-status{
	width: 100px;
}

.service-report{
	width: 50px;
	text-align:center;
}

.service-report .button {
    margin:  0px;
    padding: 3px 0 0 6px;
    height: 20px;
    width: 25px;
}

.service-stop-time, .service-start-time {
	width:200px;
}

 .service-header td {
	font-size: 0.9em;
}



.service-duration {
	width:150px;
}

.service-version {
	width:150px;
}


table {
	width:100%;
}

#service-list-panel {
	width:74%; 
	float:left; 
	padding:0px;
}

.center {
  min-height:460px;
  max-height:460px;
}

.services-panel, #service-list-filter, #service-list-panel {
	min-height:525px; 
	max-height:525px;
	overflow:auto;
}

#service-list-filter, #service-list-panel {
  overflow:auto;
}

.services-panel {
  overflow:hidden;
}

.service-configuration {
	float:left;
}

.service-properties {
	float:right;
}

.service-properties, .service-configuration {
	width:49%;
}

.service-body tr {
	border-top: 1px solid #999999;	
}


.service .service-body {
	border-top: 1px solid #DDDDDD;	
}

#service-list-filter  {
	float:right; 
	width:23%; 
	background-color:#EEEEEE;
}


#service-list-filter > div, #service-list-filter h3 {
	padding: 10px;
}

#service-list-filter h3 {
	background-color: #DDDDDD;
	font-size: 0.9em;
	margin:0;
}


.date-slider {
 margin:10px 15px 10px 0px;
}


.pane-L1-body a {
  color: #555;
}

a.button {
  padding-bottom:7px;
    color:#AAA;
}


</style>

	</tiles:putAttribute>
	<tiles:putAttribute name="body">
		<tiles:insertDefinition name="app-frame">
			<tiles:putAttribute name="mainTab">dashboard</tiles:putAttribute>

			<tiles:putAttribute name="main-content">
				<div id="main-content-panel">
				<div id="main-content-tabs">
				<ul>
					<li><a id="storage-tab-link" href="#tabs-storage"><span>Storage</span></a></li>
					<li><a id="services-tab-link" href="#tabs-services"><span>Services</span></a></li>
					<li><a id="tools-tab-link" href="#tabs-tools"><span>Tools</span></a></li>
				</ul>

                <div id="tabs-tools" class="ui-corner-all">
                    <div class="tools-header">
                    <h3>DuraCloud Tools:</h3>
                    </div>

					<div class="highlight-box ui-corner-all">
                        <div class="tools-all">
                        All DuraCloud tools can be <a target="_blank" href="https://wiki.duraspace.org/display/DURACLOUD/DuraCloud+Downloads">downloaded here</a>.
                        </div>

                        <a class="tool-header" target="_blank" href="https://wiki.duraspace.org/display/DURACLOUDDOC/DuraCloud+Sync+Tool">Sync Tool:</a>
                        <div class="tool-description">
                        The Sync Tool is a utility which will allow you to transfer (or "copy") files from a local file system to DuraCloud and subsequently keep the files in DuraCloud synchronized with those on the local system. Note that the capabilities of the chunker tool are part of the sync tool. <a target="_blank" href="https://wiki.duraspace.org/display/DURACLOUDDOC/DuraCloud+Sync+Tool">Learn more and download the Sync Tool here.</a>
                        </div>
                        <a class="tool-header" target="_blank" href="https://wiki.duraspace.org/display/DURACLOUDDOC/DuraCloud+Retrieval+Tool">Retrieval Tool:</a>
                        <div class="tool-description">
                        The Retrieval Tool is a utility which will allow you to transfer (or "retrieve") digital content from DuraCloud to your local file system. Note that the capabilities of the stitcher tool are part of the retrieval tool. <a target="_blank" href="https://wiki.duraspace.org/display/DURACLOUDDOC/DuraCloud+Retrieval+Tool">Learn more and download the Retrieval Tool here.</a>
                        </div>
                        <a class="tool-header" target="_blank" href="https://wiki.duraspace.org/display/DURACLOUDDOC/DuraCloud+Chunker+Tool">Chunker Tool:</a>
                        <div class="tool-description">
                        The Chunker Tool is a utility which will allow you to copy large files from a local file system to DuraCloud in a "one-off" manner and "chunk" those files into a size specified by you. <a target="_blank" href="https://wiki.duraspace.org/display/DURACLOUDDOC/DuraCloud+Chunker+Tool">Learn more and download the Chunker Tool here.</a>
                        </div>
                        <a class="tool-header" target="_blank" href="https://wiki.duraspace.org/display/DURACLOUDDOC/DuraCloud+Stitcher+Tool">Stitcher Tool:</a>
                        <div class="tool-description">
                        The Stitcher Tool is a utility which will allow you to retrieve chunked files from DuraCloud and have those files "stitched" back together on your local file system. <a target="_blank" href="https://wiki.duraspace.org/display/DURACLOUDDOC/DuraCloud+Stitcher+Tool">Learn more and download the Stitcher Tool here.</a>
                        </div>
                    </div>
                </div>

				<div id="tabs-services" class="ui-corner-all">
					<div id="toolbar" class="ui-widget-header ui-corner-all">
						<span id="phase">
							<input type="radio" id="installed-radio" name="phase" checked="checked"/><label for="installed-radio">Installed</label>
							<input type="radio"  id="completed-radio" name="phase" /><label for="completed-radio">Completed</label>
						</span>

					</div>
					<div id="installed-services-panel" class="services-panel highlight-box ui-corner-all"  >
						<div class="service-header">
								<table>
									<tr>
										<td class="service-name">Service</td>									
										<td class="service-status">Status</td>
										<td class="service-start-time">Start</td>
										<td class="service-duration">Duration</td>
									</tr>
								</table>

						</div>
						<div id="service-viewer">
								<div class="service" style="display:none"> 
								<div class="service-header">
									<table>
										<tr>
											<td class="service-name">name</td>									
											<td class="service-status">status</td>
											<td class="service-start-time">start</td>
											<td class="service-duration">duration</td>

										</tr>
									</table>
								</div>
								<div class="service-body"  style="display:none">
									<div class="service-configuration">
										<h3>Configuration</h3>
									</div>									
									<div class="service-properties" > 
										<h3>Properties</h3>
									</div>
								</div>
							</div>									
						
						
						</div>
					</div>
					
					<div id="completed-services-panel" class="services-panel" style="display:none;">
						
						<div id="service-list-panel" class="highlight-box ui-corner-all">

							<div class="service-header">
									<table>
										<tr>
											<td class="service-name"><a href="#">Service</a></td>									
											<td class="service-status">Status</td>
											<td class="service-start-time">Start</td>
											<td class="service-stop-time"><a href="#">Stop</a></td>
											<td class="service-duration">Duration</td>
											<td class="service-report">Report</td>

										</tr>
									</table>

							</div>
														
							<div id="service-viewer">
							
								<div class="service" style="display:none"> 
									<div class="service-header">
										<table>
											<tr>
												<td class="service-name">name</td>									
												<td class="service-status">status</td>
												<td class="service-start-time">stop</td>
												<td class="service-stop-time">stop</td>
												<td class="service-duration">duration</td>

												<td class="service-report"><a class="button" href=""><i class="pre download"></i></a></td>
												
											</tr>
										</table>
									</div>
									<div class="service-body"  style="display:none">
										<div class="service-configuration">
											<h3>Configuration</h3>
										</div>									
										<div class="service-properties" > 
											<h3>Properties</h3>
										</div>
									</div>
								</div>									

							
							</div>
						</div>
					
						<div id="service-list-filter" class="highlight-box ui-corner-all">
							<h3>Date Range</h3>
							<div id="service-date-slider">
								<table>
									<tr>
										<td>
											Start
										
										</td>
										<td>
											 <span class="date-range-start"></span>										
										</td>
									</tr>
									<tr>
										<td>
											End
										
										</td>
										<td>
											 <span class="date-range-end"></span>										
										</td>
									</tr>

								</table>
								<div class="date-slider">
								</div>					

							</div>					

							<h3>Completion Status</h3>

							<div>
								<ul id="completion-status-list">
									<li class="successful-service">
										<span>
											<input type="checkbox" id="success-checkbox" checked="checked" /><label for="success-checkbox">Success</label>
										</span>
									</li>
									<li class="started-service">
										<span >
											<input type="checkbox" id="started-checkbox"  checked="checked" /><label for="started-checkbox">Started</label>
										</span>
									</li>
									<li class="failed-service">
										<span >
											<input type="checkbox" id="failure-checkbox"  checked="checked" /><label for="failure-checkbox">Failed</label>
										</span>
									</li>
								</ul>
							</div>

							<h3>Service Type</h3>
							<div id="service-list-selection-controls" class="subtitle">
								Select: <a class="select-all" href="#">All</a>	<a class="select-none" href="#">None</a>							
							</div>
							<div>
								<div id="service-list">
								
								</div>
							</div>
						</div>
						
					</div>
									
				</div>

				<div id="tabs-storage" class="ui-corner-all">
				  <div class="north">
                    <div id="report-breadcrumb" class="dc-breadcrumb"></div>
                    <div class="dc-navigation">
                      <div class="dc-slider-value">
                        <span id="report-selected-date"></span>
                         <a
                          id="report-link" class="button download-button"
                          target="_NEW" href="x"><i class="pre download"></i>Download
                          Full Report</a>
  
                      </div>
                      <div class="dc-date-slider">
                        <div id="report-start-range"></div>
                        <div id="report-date-slider-wrapper">
                          <div id="report-date-slider"></div>
                        </div>
                        <div id="report-end-range"></div>
                      </div>
                  </div>
               </div>
              <div class="center highlight-box ui-corner-all">
              <div class="scrollable">
				<div class="items">
				<div id="storage-summary" >
					<div id="toolbar" class="ui-widget-header">
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
					<div id="toolbar"  class="ui-widget-header">
						<div class="graph-switch">
							<a href="#" class="button back-link"><i class="pre back"></i>Back</a>
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
					<div id="toolbar"  class="ui-widget-header">
						<div class="graph-switch">
							<a href="#" class="button back-link"><i class="pre back"></i>Back</a>
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
			</tiles:putAttribute>
			<tiles:putAttribute name="main-footer">
				<div id="status-holder"></div>
			</tiles:putAttribute>
		</tiles:insertDefinition>
	</tiles:putAttribute>
</tiles:insertDefinition>



