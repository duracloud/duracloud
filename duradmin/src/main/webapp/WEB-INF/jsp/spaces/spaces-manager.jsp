<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:insertDefinition name="app-base">
	<tiles:putAttribute name="title">
		<spring:message code="spaces" />
	</tiles:putAttribute>
	<tiles:putAttribute name="header-extensions">
		<script type="text/javascript">
			  var storeProviders = null;
			  $(document).ready(function(){
					storeProviders =
						 [
							<c:forEach var="storeOption" items="${contentStores}">
							{
							id: ${storeOption.storeId},
							label: '<spring:message code="${fn:toLowerCase(storeOption.storageProviderType)}"/>',
                            type: '${fn:toLowerCase(storeOption.storageProviderType)}'
							},
							</c:forEach>				
						];
			  });

              <c:if test="${error != null}">
                  alert("<c:out value="${error}"/>");
              </c:if>      
			</script>
			
		<link rel="stylesheet"
			href="${pageContext.request.contextPath}/jquery/plugins/jquery.fancybox-1.3.1/fancybox/jquery.fancybox-1.3.1.css"
			type="text/css" media="screen" />
		<script type="text/javascript"
			src="${pageContext.request.contextPath}/jquery/plugins/jquery.fancybox-1.3.1/fancybox/jquery.fancybox-1.3.1.js"></script>
		<script type="text/javascript"
			src="${pageContext.request.contextPath}/jquery/plugins/jquery.fancybox-1.3.1/fancybox/jquery.easing-1.3.pack.js"></script>
		
        <script type='text/javascript' src='${pageContext.request.contextPath}/jwplayer/swfobject.js'></script>

		<script type="text/javascript"
			src="${pageContext.request.contextPath}/jquery/dc/widget/ui.metadataviewer.js"></script>
		<script type="text/javascript"
			src="${pageContext.request.contextPath}/jquery/dc/widget/ui.tagsviewer.js"></script>
		<script type="text/javascript"
			src="${pageContext.request.contextPath}/jquery/dc/widget/ui.flyoutselect.js"></script>
		<script type="text/javascript"
			src="${pageContext.request.contextPath}/jquery/dc/api/dc.util.paralleltasks.js"></script>

			
		<script type="text/javascript"
			src="${pageContext.request.contextPath}/js/spaces-manager.js"></script>

	</tiles:putAttribute>
	<tiles:putAttribute name="body">
		<tiles:insertDefinition name="app-frame">
			<tiles:putAttribute name="mainTab" value="spaces" />
			<tiles:putAttribute name="main-content">
				<div class="center-north" id="center-pane-north">
					<div class="provider-float">Provider: <span id="provider-select-box"
						class="provider-widget"></span></div>
	
                    <div id="provider-logo" class="float-l"></div>
				</div>
				<div id="list-browser" class="list-browser" style="visibility:hidden">
				<div id="spaces-list-view" class="dc-list-item-viewer">
				<div class="north header list-header clearfix">
				<div id="header-spaces-list" class="header-section clearfix">
				<button class="featured float-r add-space-button"><i class="pre plus"></i>Add Space</button>
                    <h2>Spaces</h2>
				</div>
				<div class="header-section"><span class="float-r"><input
					id="space-filter" class="dc-item-list-filter " value="" placeholder="filter"
					type="text" /></span> <input id="check-all-spaces" class="dc-check-all"
					type="checkbox" /> <span id="space-list-status" class="dc-status"
					style="display: none"></span></div>
				</div>

				<div class="center dc-item-list-wrapper">
				<div class="dc-item-list" id="spaces-list"></div>
				</div>
				</div>
				<div id="content-item-list-view" class="dc-list-item-viewer">
				<div class="north header list-header clearfix">
				<div id="header-content-list" class="header-section clearfix">
				<button class="featured float-r add-content-item-button"><i class="pre plus"></i>Add Content Item</button>
				<h2>Content Items</h2>   
                </div>
				<div class="header-section">
					<span class="float-r">
						<input id="content-item-filter" class="dc-item-list-filter" value="" placeholder="type prefix" 	type="text" />
					</span> 
					<input id="check-all-content-items" class="dc-check-all" type="checkbox" />
					<span id="content-item-list-status" class="dc-status" ></span>
					<span id="content-item-list-controls" class="dc-item-list-controls" ></span>

					</div>
				</div>

				<div class="center dc-item-list-wrapper">
				<div class="dc-item-list" id="content-item-list"></div>
				</div>
				</div>
				</div>
				<div id="detail-pane" class="detail-pane"></div>

				<!-- Space Detail Pane:  The div is invisible and used as a prototype for displaying specific space details.-->
				<div id="spaceDetailPane" class="dc-detail-pane" style="display: none">
					<div class="north header">
						<h1>Space Detail</h1>
						<h3 class="object-name">Space Name Here</h3>
						<div class="toggle-control switch-holder">
							<div class="r access-switch"></div>
						</div>
						<div class="button-bar">
							<button class="featured add-content-item-button"><i class="pre plus"></i>Add Content Item</button>
							<button class="delete-space-button dc-delete-button"><i class="pre trash"></i>Delete Space</button>
						</div>
					</div>
					<div class="center"></div>
					<span class="object-id"></span>
				</div>

				<div id="genericDetailPane" style="display: none">
				<div class="north header"></div>
				<div class="center"></div>
				</div>

				<div id="spaceMultiSelectPane" style="display: none">
				<div class="north header">
				<h1>Spaces</h1>
				<h2 class="object-name">Space(s) selected</h2>
				<div class="button-bar">
					<button class="featured delete-space-button dc-delete-button"><i class="pre trash"></i>Delete Selected Spaces</button>
					<button class="add-remove-metadata-button"><i class="pre pencil"></i>Edit Metadata</button>
					
				</div>

				</div>
				<div class="center">
				</div>
				</div>

				<div id="contentItemMultiSelectPane" style="display: none">
				<div class="north header">
				<h1>Content Items</h1>
				<h2 class="object-name">Content item(s) selected</h2>
				<div class="button-bar">
					<button class="featured delete-content-item-button dc-delete-button"><i class="pre trash"></i>Delete</button>
					<button class="edit-selected-content-items-button"><i class="pre pencil"></i>Edit</button>
					<button class="add-remove-metadata-button"><i class="pre pencil"></i>Edit Metadata</button>
	
				</div>

				</div>
				<div class="center"></div>
				</div>

				<!-- an invisible  prototype for content items details.-->
				<div id="contentItemDetailPane" style="display: none">
					<div class="north header">
						<h1>Content Detail</h1>
						<h3>
							<a class="durastore-link" title="Links directly to content in DuraStore. This link will be publicly available only when the space is 'Open'.">
								<span class="object-name">Object Name here</span>
							</a>
						</h3>
						<div class="mime-type" id="mime-image">						
							<div class="mime-type-image-holder float-l"></div>
							<span class="label">Mime Type:</span> <span class="value">image/jpg</span>
						</div>		

						<div class="button-bar">				
							<button class="featured edit-content-item-button"><i class="pre pencil"></i>Edit</button>
							<a class="button download-content-item-button"><i class="pre download"></i>Download</a>
							<a class="button view-content-item-button" target="_blank" style="display:none"><i class="pre download"></i>View</a>
							<button class="delete-content-item-button dc-delete-button"><i class="pre trash"></i>Delete</button>						
						</div>
					</div>
					<div class="center"></div>
					<span class="object-id"></span>
				</div>
				
				<div id="add-space-dialog" class="" title="Add Space" style="display:none">
				<h1>Add Space</h1>
				<p class="hint">Add a Space to the current provider. All fields
				are required.</p>
				<div class="hint">
					<h2>Space ID's must follow these rules:</h2>
					<ul class="bullets">
						<li>Must be 3 to 63 characters long</li>
						<li>ONLY use lowercase letters, numbers, periods, and dashes</li>
						<li>Do NOT use spaces or underscores</li>
						<li>Do NOT start or end with a dash</li>
						<li>Do NOT format as an IP address, like 10.1.10.8</li>
						<li>Do NOT use these combinations of period and dashes: '..' '-.' or '.-'</li>
                        <li>The last period may not be immediately followed by a number</li>
					</ul>
				</div>
				<form id="add-space-form">
				<div id="form-fields" class="form-fields">
				<fieldset>
				<ul>
					<li class="row clearfix first-of-type"><label for="spacename">Space ID</label><input
						type="text" name="spaceId" id="spaceId" class="field" /></li>
					<li class="row clearfix"><label for="access">Access</label><span
						name="access" class="access-switch">access control here</span></li>
					<input type="hidden" name="access" id="access" />
				</ul>
				</fieldset>
				</div>
				</form>

				</div>

				<div id="add-content-item-dialog" class="dialog"
					title="Add Content Item" style="display:none">
				<h1>Add Content Item</h1>
				<p class="hint">Add a Content Item to the currently selected
				Space. All fields are required.</p>
				<form enctype="multipart/form-data" id="add-content-item-form"
					action="/duradmin/spaces/content/upload" method="POST"><input
					id="spaceId" name="spaceId" type="hidden" /> <input id="storeId"
					name="storeId" type="hidden" />

				<div id="form-fields" class="form-fields">
				<fieldset>
				<ul>
					<li class="row clearfix first-of-type"><label for="contentId">Item
					Id</label><input type="text" name="contentId" id="contentId" class="field" /></li>
					<li class="row clearfix"><label for="contentMimetype">Mime
					Type</label><input type="text" name="contentMimetype" id="contentMimetype"
						class="field" /></li>
					<li class="row clearfix"><label for="file">File</label><input
						class="field" type="file" name="file" id="file" class="field" /></li>
				</ul>
				<input type="hidden" id="key" name="key" /></fieldset>
				</div>
				</form>
				</div>

				<div id="edit-content-item-dialog" class="dialog" style="display:none"
					title="Edit Content Item">
				<h1 class="dc-dialog-title">Edit Content Item</h1>
				<p class="hint">All fields are required.</p>
				<form  id="edit-content-item-form" onsubmit="return false;">
				<input type="hidden" name="spaceId"/>
				<input type="hidden" name="contentId"/>
				<input type="hidden" name="storeId"/>
				<div id="form-fields" class="form-fields">
				<fieldset>
				
				<ul>
					<li class="row clearfix"><label for="mimetype">Mime
					Type</label><input type="text" name="contentMimetype" id="contentMimetype" class="field" /></li>
				</ul>
				</fieldset>
				</div>
				</form>
				</div>


				<div id="add-remove-metadata-dialog" class="dialog" style="display:none;overflow:auto"
					title="Add/Remove Metadata and Tags">
				<h1 class="dc-dialog-title">Add/Remove Metadata and Tags</h1>
				<p class="hint"></p>
					<div class="center">
					
					</div>				
				</div>


			</tiles:putAttribute>

			<tiles:putAttribute name="main-footer">
				<div id="status-holder">
					<a id="view-uploads">
						<span id="upload-status-label">Upload Status:</span> 
					</a>
				</div>
				<div id="upload-viewer" style="display:none">
					<h1>Uploads</h1>
					<div id="upload-list-wrapper"></div>
				</div>

			</tiles:putAttribute>
		</tiles:insertDefinition>
	</tiles:putAttribute>

</tiles:insertDefinition>



