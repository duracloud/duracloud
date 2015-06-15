<%@include file="/WEB-INF/jsp/include.jsp"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<tiles:insertDefinition name="app-base">
	<tiles:putAttribute name="title">
		<spring:message code="dashboard" />
	</tiles:putAttribute>

	<tiles:putAttribute name="header-extensions">
        <script type="text/javascript"
            src="${pageContext.request.contextPath}/jquery/dc/ext/jquery.dc.chart.js"></script>
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
    background-color: #9E9E9E
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
  background:#9e9e9e url(images/bg_list_browser_header.gif) no-repeat top right !important;

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


#completion-status-list li {
	margin:3px;
	padding:3px;
}



table {
	width:100%;
}

.center {
  min-height:460px;
  max-height:460px;
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

.ui-state-default, 
.ui-widget-content .ui-state-default, 
.ui-widget-header .ui-state-default {
   background: #6e6e6e 
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
                    <sec:authorize ifAnyGranted="ROLE_ADMIN">
                        <li><a id="storage-tab-link" href="#tabs-storage"><span>Storage</span></a></li>
                    </sec:authorize>
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

                        <a class="tool-header" target="_blank" href="https://wiki.duraspace.org/display/DURACLOUDDOC/DuraCloud+Upload+Tool">Upload Tool:</a>
                        <div class="tool-description">
                        The Upload Tool is a utility which will allow you to transfer (or "copy") files from a local file system to DuraCloud using a graphical interface. <a target="_blank" href="https://wiki.duraspace.org/display/DURACLOUDDOC/DuraCloud+Upload+Tool">Learn more and download the Upload Tool here.</a>
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

                <sec:authorize ifAnyGranted="ROLE_ADMIN">
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
                </sec:authorize>

     			</div>
				</div>
			</tiles:putAttribute>
			<tiles:putAttribute name="main-footer">
				<div id="status-holder"></div>
			</tiles:putAttribute>
		</tiles:insertDefinition>
	</tiles:putAttribute>
</tiles:insertDefinition>



