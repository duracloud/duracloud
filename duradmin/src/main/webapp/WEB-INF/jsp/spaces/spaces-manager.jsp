<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:insertDefinition name="app-base">
  <tiles:putAttribute name="title">
    <spring:message code="spaces" />
  </tiles:putAttribute>
  <tiles:putAttribute name="header-extensions">
    <script type="text/javascript">
			  var user = null, storeProviders = null;
			  $(document).ready(function(){
					storeProviders =
						 [
							<c:forEach var="storeOption" items="${contentStores}">
							{
                            id: ${storeOption.storeId},
                            label: '<spring:message code="${fn:toLowerCase(storeOption.storageProviderType)}"/>',
                            type: '${fn:toLowerCase(storeOption.storageProviderType)}',
                            writable: ${storeOption.writable}
							},
							</c:forEach>
						];

					user = {
					        username: '${user.username}',
					        authorities: [
							<c:forEach var="ga" items="${user.authorities}">
								'${ga.authority}',
							</c:forEach>
							],
					};
			  });

              <c:if test="${error != null}">
                  alert("<c:out value="${error}"/>");
              </c:if>
			</script>

    <script
      type='text/javascript'
      src='${pageContext.request.contextPath}/jwplayer/swfobject.js'></script>

    <script
      type="text/javascript"
      src="${pageContext.request.contextPath}/jquery/dc/widget/ui.propertiesviewer.js"></script>
    <script
      type="text/javascript"
      src="${pageContext.request.contextPath}/jquery/dc/widget/ui.tagsviewer.js"></script>
    <script
      type="text/javascript"
      src="${pageContext.request.contextPath}/jquery/dc/widget/ui.acleditor.js"></script>
    <script
      type="text/javascript"
      src="${pageContext.request.contextPath}/jquery/dc/widget/ui.snapshot.js"></script>
    <script
      type="text/javascript"
      src="${pageContext.request.contextPath}/jquery/dc/widget/ui.streaming.js"></script>
    <script
      type="text/javascript"
      src="${pageContext.request.contextPath}/jquery/dc/widget/ui.restore.js"></script>
    <script
      type="text/javascript"
      src="${pageContext.request.contextPath}/jquery/dc/widget/ui.historypanel.js"></script>
    <script
      type="text/javascript"
      src="${pageContext.request.contextPath}/jquery/dc/widget/ui.onoffswitch.js"></script>

    <script
      type="text/javascript"
      src="${pageContext.request.contextPath}/jquery/dc/widget/ui.flyoutselect.js"></script>
    <script
      type="text/javascript"
      src="${pageContext.request.contextPath}/jquery/dc/widget/ui.uploader.js"></script>

    <script
      type="text/javascript"
      src="${pageContext.request.contextPath}/jquery/dc/api/dc.util.paralleltasks.js"></script>

    <script
      type="text/javascript"
      src="${pageContext.request.contextPath}/jquery/dc/ext/jquery.dc.chart.js"></script>

    <script
      type="text/javascript"
      src="${pageContext.request.contextPath}/jquery/plugins/jquery.flot/jquery.flot.js"></script>
    <script
      type="text/javascript"
      src="${pageContext.request.contextPath}/jquery/plugins/jquery.flot/jquery.flot.pie.js"></script>
    <script
      type="text/javascript"
      src="${pageContext.request.contextPath}/jquery/plugins/jquery.tablesorter/jquery.tablesorter.min.js"></script>

	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/jquery/plugins/jquery.datatables/jquery.dataTables.css" />
    <style>
	    .dataTables_wrapper .dataTables_length,
	    .dataTables_wrapper .dataTables_filter,
	    .dataTables_wrapper .dataTables_info,
		.dataTables_wrapper .dataTables_processing,
		.dataTables_wrapper .dataTables_paginate {
			color: #FFF;
		}
		table.dataTable tbody tr {
			background-color: unset;
		}
		.dataTables_wrapper .dataTables_paginate .paginate_button.disabled, .dataTables_wrapper .dataTables_paginate .paginate_button.disabled:hover, .dataTables_wrapper .dataTables_paginate .paginate_button.disabled:active {
			color: #777 !important;
		}
		.dataTables_wrapper .dataTables_paginate .paginate_button {
			color: #FFF !important;
		}
    </style>

    <script type="text/javascript" src="${pageContext.request.contextPath}/jquery/plugins/jquery.datatables/jquery.dataTables.min.js"></script>

    <script
      type="text/javascript"
      src="${pageContext.request.contextPath}/js/spaces-manager.js"></script>

    <style>
      .fail {
        background: #c00;
        padding: 2px;
        color: #fff;
      }

      .hidden {
        display: none !important;
      }
</style>

  </tiles:putAttribute>
  <tiles:putAttribute name="body">
    <tiles:insertDefinition name="app-frame">
      <tiles:putAttribute
        name="mainTab"
        value="spaces" />
      <tiles:putAttribute name="main-content">
        <div
          class="center-pane-north center-north">
          <div
            <c:if test="${fn:length(contentStores) < 2}">style="display:none;"</c:if>>
            <div class="provider-float">
              Provider: <span
                id="provider-select-box"
                class="provider-widget"></span>
            </div>
          </div>
          <div
            id="provider-logo"
            class="float-l"></div>
        </div>
        <div
          id="list-browser"
          class="list-browser"
          style="visibility: hidden">
          <div
            id="spaces-list-view"
            class="dc-list-item-viewer">
            <div class="north header list-header clearfix">
              <div
                id="header-spaces-list"
                class="header-section clearfix">
                <button class="featured float-r add-space-button">
                  <i class="pre plus"></i>Add Space
                </button>
                <h2>Spaces</h2>
              </div>
              <div class="header-section">
                <span class="float-r">
                <input
                  aria-label="Filter"
                  id="space-filter"
                  class="dc-item-list-filter "
                  value=""
                  placeholder="filter"
                  type="text" />
                </span>
                <sec:authorize access="hasRole('ROLE_ROOT')">
                  <c:set var="rootUser" value="rootUser" />
                </sec:authorize>
                <c:if test="${not empty rootUser}">
                  <input
                  id="check-all-spaces"
                  class="dc-check-all"
                  type="checkbox"
                  aria-label="Check all"/>
                </c:if>
                <span
                  id="space-list-status"
                  class="dc-status"
                  style="display: none"></span>
              </div>
            </div>

            <div class="center dc-item-list-wrapper">
              <div
                class="dc-item-list"
                id="spaces-list"></div>
            </div>
          </div>
          <div
            id="content-item-list-view"
            class="dc-list-item-viewer">
          </div>
        </div>
        <div
          id="detail-pane"
          class="detail-pane"></div>

        <div id="contentItemListPane"
                  style="display: none">
            <div class="north header list-header clearfix">
              <div
                id="header-content-list"
                class="header-section clearfix">

                <button class="float-r add-content-item-button"><i class="pre plus"></i>Upload</button>

                <button class="float-r refresh-space-button">
                  <i class="pre refresh"></i>Refresh
                </button>

                <h2>Content Items</h2>
              </div>
              <div class="header-section">
                <input
                  id="content-item-filter"
                  aria-label="Prefix"
                  class="dc-item-list-filter"
                  value=""
                  placeholder="type prefix"
                  type="text" /> </span> <input
                  id="check-all-content-items"
                  class="dc-check-all"

                  type="checkbox" aria-label="Check all" /> <span
                  id="content-item-list-status"
                  class="dc-status"></span> <span
                  id="content-item-list-controls"
                  class="dc-item-list-controls"></span>

              </div>
            </div>

            <div class="center dc-item-list-wrapper">
              <div
                class="dc-item-list"
                id="content-item-list"></div>
            </div>
        </div>

        <div id="snapshotItemListPane"
                  style="display: none">
            <div class="north header list-header clearfix">
              <div
                id="header-content-list"
                class="header-section clearfix">

                <button class="float-r refresh-space-button">
                  <i class="pre refresh"></i>Refresh
                </button>

                <h2>Snapshot Content Items</h2>
              </div>
              <div class="header-section">
                <span class="float-r"> <input
                  id="content-item-filter"
                  class="dc-item-list-filter"
                  value=""
                  placeholder="type prefix"
                  type="text" /> </span> <span
                  id="content-item-list-status"
                  class="dc-status"></span> <span
                  id="content-item-list-controls"
                  class="dc-item-list-controls"></span>

              </div>
            </div>

            <div class="center dc-item-list-wrapper">
              <div
                class="dc-item-list"
                id="content-item-list"></div>
            </div>
        </div>

        <!-- Space Detail Pane:  The div is invisible and used as a prototype for displaying specific space details.-->
        <div
          id="spaceDetailPane"
          class="dc-detail-pane"
          style="display: none">
          <div class="north header">
            <h1>Space Detail</h1>
            <h2 class="object-name">Space Name Here</h2>
            <div class="button-bar">
              <!--
                            <button class="featured add-content-item-button"><i class="pre plus"></i>Add One Item</button>
                            <a class="button featured bulk-add-content-item"><i class="pre plus"></i>Add Many Items</a>
							 -->

              <button class="delete-space-button dc-delete-button">
                <i class="pre trash"></i>Delete
              </button>
              <a class=" download-audit-button button" >
                <i class="pre download" title="download manifest"></i>Audit Log
              </a>

              <a class=" download-manifest-button button" data-dropdown="#manifest-dropdown">
                <i class="pre download" title="download manifest"></i>Manifest
              </a>

            </div>
          </div>
          <div class="center"></div>
          <span class="object-id"></span>
        </div>

                <!-- Snapshot Detail Pane:  The div is invisible and used as a prototype for displaying specific snapshot details.-->
        <div
          id="snapshotDetailPane"
          class="dc-detail-pane"
          style="display: none">
          <div class="north header">
            <h1>Snapshot Detail</h1>
            <h3 class="object-name">Snapshot Name Here</h3>
            <div class="button-bar">

              <a id="restoreLink" class="button">View Restored Space</a>
              <a id="metadataLink" class="button"><i class="pre download"></i>Download Metadata</a>
              <button
                id="restoreButton"
                class="featured">
                <i class="pre copy"></i>Restore Snapshot
              </button>
              <button
                id="requestRestoreButton">
                <i class="pre copy"></i>Request Restore
              </button>

            </div>
          </div>
          <div class="center"></div>
          <span class="object-id"></span>
        </div>

        <div
          id="snapshotItemDetailPane"
          class="dc-detail-pane"
          style="display: none">
          <div class="north header">
            <h1>Snapshot Item Detail</h1>
            <h3 class="object-name">Snapshot Name Here</h3>
            <div class="button-bar">
            </div>
          </div>
          <div class="center"></div>
          <span class="object-id"></span>
        </div>


        <div
          id="genericDetailPane"
          style="display: none">
          <div class="north header"></div>
          <div class="center"></div>
        </div>

        <div
          id="spacesDetailPane"
          style="display: none">
          <div class="north header">
            <h1>Spaces</h1>
          </div>
          <div class="center"></div>
        </div>

        <div
          id="spaceMultiSelectPane"
          style="display: none">
          <div class="north header">
            <h1>Spaces</h1>
            <h2 class="object-name">Space(s) selected</h2>
            <div class="button-bar">
              <button class="featured delete-space-button dc-delete-button">
                <i class="pre trash"></i>Delete Selected Spaces
              </button>
            </div>

          </div>
          <div class="center"></div>
        </div>

        <div
          id="contentItemMultiSelectPane"
          style="display: none">
          <div class="north header">
            <h1 class="multiContentItemSelectPaneTitle">Content Items</h1>
            <h2 class="object-name">{count} content item(s) selected</h2>
            <div class="button-bar">
              <button
                class="featured delete-content-item-button dc-delete-button">
                <i class="pre trash"></i>Delete
              </button>
              <button class="edit-selected-content-items-button">
                <i class="pre pencil"></i>Edit
              </button>
              <button class="add-remove-properties-button">
                <i class="pre pencil"></i>Edit Properties
              </button>
              <button class="copy-content-item-button">
                <i class="pre copy"></i>Copy
              </button>

            </div>

          </div>
          <div class="center"></div>
        </div>

        <!-- an invisible  prototype for content items details.-->
        <div
          id="contentItemDetailPane"
          style="display: none">
          <div class="north header">
            <h1>Content Detail</h1>
            <h2>
              <a
                class="durastore-link"
                title="Links directly to content in DuraStore. This link will be publicly available only when the space is 'Open'.">
                <span class="object-name">Object Name here</span> </a>
            </h2>
            <div
              class="mime-type"
              id="mime-image">
              <div class="mime-type-image-holder float-l"></div>
              <span class="label">Mime Type:</span> <span class="value">image/jpg</span>
            </div>

            <div class="button-bar">
              <button class="featured edit-content-item-button">
                <i class="pre pencil"></i>Edit
              </button>
              <button class="copy-content-item-button">
                <i class="pre copy"></i>Copy
              </button>

              <a class="button download-content-item-button"><i
                class="pre download"></i>Download</a> <a
                class="button view-content-item-button"
                target="_blank"
                style="display: none"><i class="pre view"></i>View</a>
              <button class="delete-content-item-button dc-delete-button">
                <i class="pre trash"></i>Delete
              </button>
            </div>
          </div>
          <div class="center"></div>
          <span class="object-id"></span>
        </div>

        <div
          id="add-space-dialog"
          class=""
          title="Add Space"
          style="display: none">
          <h1>Add Space</h1>
          <p class="hint">Add a Space to the current provider. All fields
            are required.</p>
          <div class="hint">
            <h2>Space ID's must follow these rules:</h2>
            <ul class="bullets">
              <li>Must be between 3 and 42 characters long</li>
              <li>Includes ONLY lowercase letters, numbers, periods, and
                dashes</li>
              <li>Must start with a lowercase letter</li>
              <li>Does NOT include spaces or underscores</li>
              <li>Does NOT end with a dash</li>
              <li>Does NOT combine periods and dashes as '..' '-.' or '.-'</li>
              <li>Does NOT immediately follow the last period with a number</li>
              <li>Is NOT a reserved name: init, stores, spaces, security,
                task</li>
            </ul>
          </div>
          <form id="add-space-form">
            <div
              id="form-fields"
              class="form-fields">
              <fieldset>
                <ul>
                  <li class="row clearfix first-of-type"><label id="spaceIdLbl"
                    for="spaceId">Space ID</label><input
                    type="text"
                    name="spaceId"
                    id="spaceId"
                    aria-labelledby="spaceIdLbl"
                    class="field" />
                  </li>
                  <li class="row clearfix"><label for="publicFlag">Public
                      Access?</label> <input
                    type="checkbox"
                    name="publicFlag"
                    class="field"
                    id="publicFlag"
                    value="true" /></li>
                </ul>
              </fieldset>
            </div>
          </form>

        </div>

        <div
          id="delete-space-dialog"
          class=""
          title="Delete Space"
          style="display: none">
          <h1>Delete Space</h1>
          <div class="hint">
            <h2>Choosing to DELETE the space named <div id="spaceId" style="display: inline-block;font-weight: bold;"></div> will remove <div id="spaceItemCount" style="display: inline-block;font-weight: bold;"></div> file(s) in the space. THIS ACTION CANNOT BE UNDONE!</h2>
            <br/><br/>
            <h2>Please type the name of the space below.</h2>
          </div>
          <form id="delete-space-form">
            <input type="hidden" name="compareSpaceId" id="compareSpaceId" class="field" />
            <div
              id="form-fields"
              class="form-fields">
              <fieldset>
                <ul>
                  <li class="row clearfix first-of-type"><label
                    for="spaceName">Space Name</label><input
                    type="text"
                    name="spaceId"
                    id="spaceName"
                    class="field" />
                  </li>
                </ul>
              </fieldset>
            </div>
          </form>
        </div>

        <div
          id="add-content-item-dialog"
          class="dialog"
          title="Add Content Item"
          style="display: none">
          <h1>Add Content Item</h1>
          <p class="hint">Add a Content Item to the currently selected
            Space. All fields are required.</p>
          <form
            enctype="multipart/form-data"
            accept-charset="UTF-8"
            id="add-content-item-form"
            action="/duradmin/spaces/content/upload"
            method="POST">
            <input
              id="spaceId"
              name="spaceId"
              type="hidden" /> <input
              id="storeId"
              name="storeId"
              type="hidden" />

            <div
              id="form-fields"
              class="form-fields">
              <fieldset>
                <ul>
                  <li class="row clearfix first-of-type"><label
                    for="addContentId">Item Id</label><input
                    type="text"
                    name="contentId"
                    id="addContentId"
                    class="field" />
                  </li>
                  <li class="row clearfix"><label for="addContentMimetype">Mime
                      Type</label><input
                    type="text"
                    name="contentMimetype"
                    id="addContentMimetype"
                    class="field" />
                  </li>
                  <li class="row clearfix"><label for="file">File</label><input
                    class="field"
                    type="file"
                    name="file"
                    id="file"
                    class="field" />
                  </li>
                </ul>
                <input
                  type="hidden"
                  id="key"
                  name="key" />
              </fieldset>
            </div>
          </form>
        </div>

        <div
          id="snapshot-dialog"
          class="dialog"
          style="display: none; overflow: auto"
          title="Create Snapshot">
          <h1 class="dc-dialog-title">Create a snapshot</h1>
          <p class="hint">This action begins the process of transferring all of
            the content in this space to snapshot storage. Once the
            Create button below is pressed, you will no longer be able to edit
            the contents of this space. You may optionally include a description
            of this snapshot, which will be preserved along with the snapshot
            itself.</p>
          <div class="center">
            <form id="snapshot-properties-form" onsubmit="return false;">
              <div
              id="form-fields"
              class="form-fields">

              <fieldset>
                <ul>
                  <li class="row clearfix"><label for="description">Description</label>
                  <textarea cols="50" rows="5"
                    name="description"
                    id="description"
                    class="field"></textarea>
                  </li>
                </ul>
              </fieldset>
            </div>
            </form>
          </div>
        </div>


        <div
          id="edit-content-item-dialog"
          class="dialog"
          style="display: none"
          title="Edit Content Item">
          <h1 class="dc-dialog-title">Edit Content Item</h1>
          <p class="hint">All fields are required.</p>
          <form
            id="edit-content-item-form"
            onsubmit="return false;">
            <input
              type="hidden"
              name="spaceId" /> <input
              type="hidden"
              name="contentId" /> <input
              type="hidden"
              name="storeId" />
            <div
              id="form-fields"
              class="form-fields">
              <fieldset>

                <ul>
                  <li class="row clearfix"><label for="editContentMimetype">Mime
                      Type</label><input
                    type="text"
                    name="contentMimetype"
                    id="editContentMimetype"
                    class="field" />
                  </li>
                </ul>
              </fieldset>
            </div>
          </form>
        </div>

        <div
          id="copy-content-item-dialog"
          class="dialog"
          style="display: none"
          title="Copy Content Item">
          <h1 class="dc-dialog-title">Copy Content Item</h1>
          <form
            id="copy-content-item-form"
            onsubmit="return false;">
            <input
              type="hidden"
              name="storeId"
              id="storeId" />
            <c:if test="${fn:length(contentStores) == 1 or empty rootUser}">
              <input
                type="hidden"
                name="destStoreId"
                id="destStoreId" />
            </c:if>

            <div
              id="form-fields"
              class="form-fields">
              <fieldset>
                <ul>
                  <c:if test="${fn:length(contentStores) > 1 and not empty rootUser}">
                    <li class="row clearfix"><label for="destStoreId">Storage
                        Provider</label> <select
                      id="destStoreId"
                      name="destStoreId">
                        <c:forEach
                          var="storeOption"
                          items="${contentStores}">
                          <option value="${storeOption.storeId}">
                            <spring:message
                              code="${fn:toLowerCase(storeOption.storageProviderType)}" />
                          </option>
                        </c:forEach>
                    </select></li>
                  </c:if>

                  <li class="row clearfix"><label for="spaceId" id="copySpaceIdLbl">Space</label>
                    <select id="spaceId" name="spaceId" aria-labelledby="copySpaceIdLbl"></select></li>
                  <li class="row clearfix"><label for="contentId" id="copyContentIdLbl">Content
                      Name</label> <input
                    type="text"
                    name="contentId"
                    id="contentId"
                    aria-labelledby="copyContentIdLbl"
                    class="field"
                    style="width: 325px" /></li>
                  <li class="row clearfix"><label for="deleteAfterCopy">Delete
                      original after copy?</label> <input
                    type="checkbox"
                    id="deleteAfterCopy"
                    name="deleteAfterCopy" /></li>
                  <li class="row clearfix"><label for="navigateToCopy">Navigate
                      to new item after copy?</label> <input
                    type="checkbox"
                    id="navigateToCopy"
                    checked="checked"
                    name="navigateToCopy" /></li>
                  <li class="row clearfix"><label for=overwriteExisting>Overwrite
                      existing items w/o prompt?</label> <input
                    type="checkbox"
                    id="overwriteExisting"
                    name="overwriteExisting" /></li>

                </ul>
              </fieldset>
            </div>
          </form>
        </div>

        <div
          id="add-remove-properties-dialog"
          class="dialog"
          style="display: none; overflow: auto"
          title="Add/Remove Properties and Tags">
          <h1 class="dc-dialog-title">Add/Remove Properties and Tags</h1>
          <p class="hint"></p>
          <div class="center"></div>
        </div>

      </tiles:putAttribute>

      <tiles:putAttribute name="main-footer">

        <div id="status-holder">

        </div>
        <div id="upload-viewer" style="display:none">
          <h1>Upload</h1>
          <div class="hint status">
            <p></p>
          </div>
          <div id="upload-progress-panel" style="display:none">
            <progress
              id="uploadprogress"
              min="0"
              max="100"
              value="0">0</progress>
          </div>
          <div id="file-chooser-form" class="form-fields">
             <input  id="file" name="file" type="file" class="field" multiple />
          </div>

          <div id="upload-list-wrapper">
              <div id="dnd-upload">
                <div id="upload-option-divider">
                   OR
                </div>

                <div id="drop-target">
                  <span>
                    Optionally drop files here
                  </span>
                </div>

              </div>

            <form enctype="multipart/form-data">
              <div id="drop-preview">

              </div>
            </form>

          </div>
        </div>
      </tiles:putAttribute>
    </tiles:insertDefinition>
  </tiles:putAttribute>
    <tiles:putAttribute name="footer-extensions">
                 <!-- the dropdown plugin requires that dropdowns appear before the closing tag:
                       suboptimal yes: http://labs.abeautifulsite.net/jquery-dropdown/ -->
                 <div
                id="manifest-dropdown"
                class="dropdown dropdown-tip">
                <ul class="dropdown-menu">
                  <li><a id="manifest-tsv" href="">Tab Separated (TSV)</a></li>
                  <li><a id="manifest-bagit" href="">Bagit</a></li>
                </ul>
              </div>

    </tiles:putAttribute>
</tiles:insertDefinition>

