/**
 * Spaces Manager
 * 
 * @author Daniel Bernstein
 */
var centerLayout, listBrowserLayout, spacesListPane, contentItemListPane,detailPane, spacesManagerToolbar;

$.require("jquery.fancybox-1.3.1.pack.js");
$.require("jquery.easing-1.3.pack.js");
$.require("ui.metadataviewer.js");
$.require("ui.tagsviewer.js");
$.require("ui.flyoutselect.js");
$.require("dc.util.paralleltasks.js");

$(document).ready(function() {
	

	
	centerLayout = $('#page-content').layout({
	// minWidth: 300 // ALL panes
		north__size: 			50	
	,	north__paneSelector:     ".center-north"
	,   north__resizable:   false
	,   north__slidable:    false
	,   north__spacing_open:			0			
	,	north__togglerLength_open:		0			
	,	north__togglerLength_closed:	0			

	,   west__size:				800
	,	west__minSize:			600
	,   west__paneSelector:     "#list-browser"
	,   west__onresize:         "listBrowserLayout.resizeAll"
	,	center__paneSelector:	"#detail-pane"
	,   center__onresize:       "detailPane.resizeAll"
	});


	listBrowserLayout = $('#list-browser').layout({
	    	west__size:				300
	    ,	west__minSize:			260
		,   west__paneSelector:     "#spaces-list-view"
	// , west__onresize: "spacesListPane.resizeAll"
		,	center__paneSelector:	"#content-item-list-view"
		,   center__onresize:       "contentItemListPane.resizeAll"
	});
	

	var spacesAndContentLayoutOptions = {
			north__paneSelector:	".north"
		,   north__size: 			60
		,	center__paneSelector:	".center"
		,   resizable: 				false
		,   slidable: 				false
		,   spacing_open:			0			
		,	togglerLength_open:		0	
	};
			
	spacesListPane = $('#spaces-list-view').layout(spacesAndContentLayoutOptions);
	contentItemListPane = $("#content-item-list-view").layout(spacesAndContentLayoutOptions);

	// detail pane's layout options
	var spaceDetailLayoutOptions = {
			north__paneSelector:	".north"
				,   north__size: 			200
				,	center__paneSelector:	".center"
				,   resizable: 				false
				,   slidable: 				false
				,   spacing_open:			0
				,	togglerLength_open:		0
				
	};
	
	// content item detail layout is slightly different from
	// the space detail - copy and supply overrides
	var contentItemDetailLayoutOptions = $.extend(true,{}, 
													   spaceDetailLayoutOptions, 
													   {north__size:200});
	
	
	detailPane = $('#detail-pane').layout(spaceDetailLayoutOptions);
	
	// //////////////////////////////////////////
	// sets contents of object-name class
	// /
	var setObjectName = function(pane, name){
		$(".object-name", pane).empty().prepend(name);	
	};
			
	// /////////////////////////////////////////
	// /check/uncheck all spaces
	$(".dc-check-all").click(
		function(evt){
			var checked = $(evt.target).attr("checked");
			$(evt.target)
				.closest(".dc-list-item-viewer")
				.find(".dc-item-list")
				.selectablelist("select", checked);
	});

	
	var showMultiSpaceDetail = function(){
		var detail = $("#spaceMultiSelectPane").clone();
		//loadMetadataPane(multiSpace);
		//loadTagPane(multiSpace);
		$("#detail-pane").replaceContents(detail, spaceDetailLayoutOptions);

		clearContents();
		// attach delete button listener
		$(".delete-space-button",detail).click(function(evt){
			if(!dc.confirm("Are you sure you want to delete multiple spaces?")){
				return;
			}
			dc.busy("Deleting spaces");
			var spaces = $("#spaces-list").selectablelist("getSelectedData");
			var job = dc.util.createJob("delete-spaces");	

			for(i in spaces){
				job.addTask({
					_space: spaces[i],
					execute: function(callback){
						var that = this;
						dc.store.DeleteSpace(this._space, {
							success:function(){
								callback.success();
								$("#spaces-list").selectablelist("removeById", that._space.spaceId);
							},
							failure: function(message){
								callback.failure();
							},
						});
					},
				});
			}

			job.execute(
				{ 
					changed: function(job){
						dc.log("changed:" + job)
						var p = job.getProgress();
						dc.busy("Deleting spaces: " + p.successes );
					},
					cancelled: function(job){
						dc.log("cancelled:" + job);
						dc.done();
					}, 
					done: function(job){
						dc.log("done:" + job);
						dc.done();
				}, 
			});
		});

	};

	var showMultiContentItemDetail = function(){
		var detail = $("#contentItemMultiSelectPane").clone();
		// attach delete button listener
		$(".delete-content-item-button",detail).click(function(evt){
			if(!dc.confirm("Are you sure you want to delete multiple content items?")){
				return;
			}
			dc.busy("Deleting Content Items...");
			var contentItems = $("#content-item-list").selectablelist("getSelectedData");
			var job = dc.util.createJob("delete-content-items");	

			for(i in contentItems){
				job.addTask({
					_contentItem: {
						contentId: contentItems[i].contentId, 
						spaceId:getCurrentSpaceId(),
						storeId:getCurrentProviderStoreId() 
					},
					execute: function(callback){
						var that = this;
						dc.store.DeleteContentItem(this._contentItem, {
							success:function(){
								callback.success();
								$("#content-item-list").selectablelist("removeById", that._contentItem.contentId);
							},
							failure: function(message){
								callback.failure();
							},
						});
					},
				});
			}

			job.execute(
				{ 
					changed: function(job){
						dc.log("changed:" + job)
						var p = job.getProgress();
						dc.busy("Deleting content items: " + p.successes );
					},

					cancelled: function(job){
						dc.log("cancelled:" + job);
						dc.done();
					}, 
					done: function(job){
						dc.log("done:" + job);
						dc.done();
				}, 
			});

			
		});


		$("#detail-pane").replaceContents(detail, contentItemDetailLayoutOptions);

	
	};

	var showGenericDetailPane = function(){
		$("#detail-pane").replaceContents($("#genericDetailPane").clone(), spaceDetailLayoutOptions);
	};

	// ////////////////////////////////////////
	// //functions for loading metadata, tags and properties

	var loadMetadataPane = function(target, extendedMetadata){
		var viewerPane = $.fn.create("div")
						.metadataviewer({title: "Metadata"})
						.metadataviewer("load",extendedMetadata);

		$(".center", target).append(viewerPane);
		return viewerPane;
	};

	var loadTagPane = function(target, tags){
		var viewerPane = $.fn.create("div")
						.tagsviewer({title: "Tags"})
						.tagsviewer("load",tags);
		$(".center", target).append(viewerPane);
		return viewerPane;
	};
	
	var loadProperties = function(target, /* array */ properties){
		$(".center", target)
			.append($.fn.create("div")
						.tabularexpandopanel(
								{title: "Details", data: properties}));
	};

	var loadVideo = function(target, contentItem){		
		loadMedia(target,contentItem, "Watch","video");
	}	

	var loadAudio = function(target, contentItem){		
		loadMedia(target,contentItem, "Listen","audio");
	}	

	var loadMedia = function(target, contentItem, title,/*audio or video*/type){		
		
		
		var viewer = $.fn.create("div").attr("id", "mediaspace");
		
		var div = $.fn.create("div")
		.expandopanel({title: title});
		
		$(div).expandopanel("getContent").css("text-align", "center").append(viewer);
		$(".center", target).append(div);

		var streamHost = "s3i8j0alxo57q2.cloudfront.net";
		
		dc.service.GetDeployedServices({
			success: function(data){
				var services = data.services;
				var streamingService = null;
				var i;
				for(i in services){
					var service = services[i];
					if(service.contentId.indexOf('mediastreamingservice') > -1){
						streamingService = service;
						var deployments = streamingService.deployments;
						var sourceMediaSpace = false;
						
						var j;
						for(j in deployments){
							var deployment = deployments[j];

							var userConfigs = deployment.userConfigs;
							var m;
							for(m in userConfigs){
								var uc = userConfigs[m];
								if(uc.name == "mediaSourceSpaceId" && uc.displayValue == contentItem.spaceId){
									sourceMediaSpace = true;
									break;
								}
							}

							if(sourceMediaSpace){
								dc.service.GetServiceDeploymentConfig(streamingService, deployment,{
									success: function(data) {
										var streamingHost = null;
										var k;
										for(k in data.properties){
											var prop = data.properties[k];
											if(prop.name == 'Streaming Host'){
												streamingHost = prop.value;
												setTimeout(function(){
													//async necessary to let the DOM update itself so that the mediaspace dom element is present.
													var so = new SWFObject('/duradmin/jwplayer/player.swf','ply','350','216','9','#ffffff');
												    so.addParam('allowfullscreen','true');
												    so.addParam('allowscriptaccess','always');
												    so.addParam('wmode','opaque');
												    so.addVariable('skin','/duradmin/jwplayer/stylish.swf');
												    so.addVariable('file', contentItem.contentId);
												    so.addVariable('streamer', 'rtmp://' +streamingHost+ '/cfx/st');
												    so.write('mediaspace');
												},1000);
											}
										}
									},
									
									failure: function(text){
										alert("failed to get deployment config for " + streamingService.serviceId + " deployed on " + deployment.hostname + ": " + text);
									},
									
								});
							}
						}
						
						if(!sourceMediaSpace){
							viewer.html("<p>No streaming service is running against this space." + 
							"Please reconfigure the streaming service to use this space as the source.</p>");
							//viewer.append("<p>The player below will work on HTML5 compliant browsers only.");
							//viewer.append(createHTML5MediaTag(contentItem,type));
						}
					}
				}
				
				if(streamingService == null){
					viewer.html("<p>The media stream services must be running to stream audio/video files for this space.</p");
					//viewer.append("<p>The player below will work on HTML5 compliant browsers only.");
					//viewer.append(createHTML5MediaTag(contentItem, type));
				}
			},
			failure: function(text){
				alert("failed to get deployed services: " + text);
			},
		});

	};

	var createHTML5MediaTag = function(contentItem,type){
		return type == 'audio' ? createHTML5AudioTag(contentItem) : createHTML5VideoTag(contentItem);
	}

	var createHTML5AudioTag = function(contentItem){
		return $.fn.create("audio")
		.attr("src", contentItem.viewerURL)
		.attr("loop", "false")
		.attr("preload", "false")
		.attr("controls", "true");
	};

	var createHTML5VideoTag = function(contentItem){
		return $.fn.create("video")
		.attr("src", contentItem.viewerURL)
		.attr("loop", "false")
		.attr("preload", "false")
		.attr("width", "350")
		.attr("height", "216")
		.attr("controls", "true");
	};

	
	var loadPreview = function(target, contentItem){
		
		var mimetype = contentItem.metadata.mimetype;
		var isExternalViewer =  contentItem.viewerURL.indexOf('djatok') > 0;
		var isImage = (contentItem.metadata.mimetype.indexOf('image') == 0)
		var viewerType = 'iframe';
		var options = {
				'transitionIn'	:	'elastic',
				'transitionOut'	:	'elastic',
				'speedIn'		:	600, 
				'speedOut'		:	200, 
				'overlayShow'	:	false};
		
		if(isExternalViewer || !isImage){
			options['width'] = $(document).width()*0.8;
			options['height'] = $(document).height()*0.8;
			options['type'] = 'iframe';
		}else{
			options['type'] = 'image';
		}
	
		var div = $.fn.create("div")
					  .expandopanel({title: "Preview"});
		
		$(".view-content-item-button", target)
			.css("display","inline-block")
			.attr("href", contentItem.viewerURL);

		var thumbnail = $.fn.create("img")
							.attr("src", contentItem.thumbnailURL)
							.addClass("preview-image");

		var viewerLink = $.fn.create("a").append(thumbnail)
							.attr("href", contentItem.viewerURL)
							.fancybox(options);
	
		var wrapper = $.fn.create("div")
							.addClass("preview-image-wrapper")
							.append(viewerLink);

		if(isImage && isExternalViewer){
			var warning = $.fn.create("div").addClass("warning").hide();
			$(div).expandopanel("getContent").append(warning);
			dc.store.GetSpace(
				 contentItem.storeId,
				 contentItem.spaceId,
				 {
					success: function(space){
					 	if(space.metadata.access == 'CLOSED'){
					 		var button = $.fn.create("button")
					 			.addClass("featured")
					 			.css("margin-left","10px")
					 			.html("Open Space");
					 		
					 		button.click(function(){
			 					toggleSpaceAccess(
			 						space, 
			 						{
			 							success:function(){
			 								loadContentItem(contentItem);
			 							},
			 							failure:function(){alert("operation failed")},
			 						})					 				
				 			});

					 		
					 		warning.append("<span>To use the JP2 Viewer you must open this space.</span>")
					 			   .append(button).show();
					 	}
				 	},
				 	failure: function(){},
				 });

			
		}
		$(div).expandopanel("getContent").append(wrapper);
		
		$(".center", target).append(div);
	};

	var options = {
			'type'			:   'inline',
			'transitionIn'	:	'elastic',
			'transitionOut'	:	'elastic',
			'speedIn'		:	600, 
			'speedOut'		:	200, 
			'overlayShow'	:	false,
			'content'			:   "Test Content"	
			
	};	
	
	var getStoreName = function(storeId) {
		for(i in storeProviders){
			var store = storeProviders[i];
			if(storeId == store.id){
				return store.label;
			}
		}
		
		return 'no provider found with id = ' + storeId;
	};
	
	var createTaskPanel = function(task) {
		var props = task.properties;
		var percentComplete = parseInt(parseInt(props.bytesRead)/parseInt(props.totalBytes)*100);
		var state = props.state;
		var modifierClass = null;
		if(state == "cancelled"){
			modifierClass = "dc-cancelled";
		}else if(state == "failure"){
			modifierClass = "dc-failure";
		}else{
			modifierClass = "";
		}
		
		var item = 	$.fn.create("div")
						.addClass("upload-item clearfix")
						.append(
							$.fn.create("span").addClass("upload-item-id").html(props.contentId)
						).append(
								$.fn.create("span").addClass("upload-item-store-space").html("Store: " + getStoreName(parseInt(props.storeId)) + " | Space: " + props.spaceId)
						).append(
								$.fn.create("div").addClass("dc-progressbar-wrapper")
									.append(
											$.fn.create("div").addClass("dc-progressbar " + modifierClass)
												.append(
														$.fn.create("div").addClass("dc-progressbar-value " + modifierClass)))
						).append(
							$.fn.create("div").addClass("dc-controls")
						);

		// configure progress bar
		item.find(".dc-progressbar-value")
			.css("width", percentComplete+"%")
			.html(parseInt(parseInt(props.bytesRead)/1024) + 
					" of " + 
						parseInt(parseInt(props.totalBytes)/1024) + 
							" KB / " + 
								percentComplete + "%");			
		
		var actionCell = item.find(".dc-controls");	
		actionCell.append(
				$.fn.create("span")
					.addClass("dc-progress-state").html(state));
		
		if(state == 'success'){
			item.click(function(){
				$("#upload-viewer").dialog("close");
				loadWhatYouCan(props);
			}).css("cursor", "pointer");
			
		}
		
		if(state == 'running'){
			actionCell.append(
					$.fn.create("button")
					.html("Cancel")
					.click(function(evt){ 
						var that = this;
						evt.stopPropagation();
						dc.ajax({
							url: "/duradmin/spaces/upload",
							data: "action=cancel&taskId="+task.id,
							type:"POST",
							success: function(){
								$(that).closest(".upload-item").fadeOut("slow");
							},
							
						    failure: function(textStatus){
								alert("failed to remove task");
							},
						});	
					}));
		}else{
			actionCell.append($.fn.create("button").html("Remove").click(function(evt){
				var that = this;
				evt.stopPropagation();
				dc.ajax({
					url: "/duradmin/spaces/upload",
					data: "action=remove&taskId="+task.id,
					type:"POST",
					success: function(){
						$(that).closest(".upload-item").fadeOut("slow");
					},
					
				    failure: function(textStatus){
						alert("failed to remove task");
					},
				});	
			}));
		}
		
		return item;
		
	};
	
	
	$("#view-uploads").click(function(){
		$("#upload-viewer").dialog("open");
	});

	var runPoller = false;
	
    $("#upload-viewer").dialog({
		autoOpen: false,
		show: 'blind',
		hide: 'blind',
		resizable: false,
		height: 400,
		closeOnEscape:true,
		modal: false,
		width:500,
		closable: false,
		buttons: {
			"Close": function(){
				$(this).dialog("close");
				runPoller = false;
			}
		},
		beforeclose: function(event, ui){
		},
		open: function(event,ui){
			runPoller = true;
			var poll = function(){
				if(runPoller){
					poller();
					setTimeout(function(){
						poll();
					},2000);
				}
			};
			poll();
		},
	});

	var poller = function(pollInterval){
		dc.ajax({
			cache:false,
			url: "/duradmin/spaces/upload",
			success: function(data){
				
				var link = $("#view-uploads");
				$("#progress-bar", link).remove();
				$("#uploads-status-text", link).remove();
				
				if(data != null && data.taskList != null && data.taskList.length > 0){
					var inprogress = false;
					var error = false;
					for(i in data.taskList){
						var props = data.taskList[i].properties;
						if(props.state =='running'){
							inprogress = true;
						}
					}

					if(inprogress){
						link.append($.fn.create("span").attr("id", "progress-bar"));
					}else{
						link.append($.fn.create("span").attr("id", "uploads-status-text").html("Done"));
					}
				}else{
					link.append($.fn.create("span").attr("id", "uploads-status-text").html(" ---- "));
				}
				
				var upload = $("#upload-list-wrapper");
				upload.empty();
				for(i in data.taskList){
					var t = data.taskList[i];
					upload.append(createTaskPanel(t));
				}							
				if(pollInterval != undefined && pollInterval > 0){
					setTimeout(poller, pollInterval);
				}
			}
		});
	};
	
	poller(60*1000);

	// /////////////////////////////////////////
	// /open add space dialog
	$.fx.speeds._default = 10;


	
	$('#add-space-dialog').dialog({
		autoOpen: false,
		show: 'blind',
		hide: 'blind',
		resizable: false,
		height: 370,
		closeOnEscape:true,
		modal: true,
		width:500,
		buttons: {
			'Add': function(evt) {
				if($("#add-space-form").valid()){
					var space = {
						storeId: getCurrentProviderStoreId(),
						spaceId: $("#add-space-dialog #spaceId").val(),
						access:  $("#add-space-dialog #access").val(),
					};
					dc.store.AddSpace(
						space,
						{
							begin: function(){
								dc.busy( "Adding space...");
							},
							success: function(space){
								dc.done();
								addSpaceToList(space);
								spacesArray.push(space);
								spacesArray.sort(function(a,b){
								   return a.spaceId > b.spaceId;
								});
							
								$("spaces-list").selectablelist("setCurrentItemById", space.spaceId);
								scrollToCurrentSpace();
								
							},
							failure: function(text){
								alert("add space failed: " + text);
							},
						}
					);
				
					$("#add-space-dialog").dialog("close");

				}
			},
			Cancel: function(evt) {
				$(this).dialog('close');
			}
		},
		
		close: function() {
	
		},
		
		open: function(e){
			$("#add-space-dialog .access-switch").accessswitch({})
				.bind("turnOn", function(evt, future){
					future.success();
					evt.stopPropagation();
					$("#add-space-dialog #access").val("OPEN");
					
				}).bind("turnOff", function(evt, future){
					future.success();
					evt.stopPropagation();
					$("#add-space-dialog #access").val("CLOSED");
				}).accessswitch("on");
			
			$("#add-space-form").validate({
				rules: {
					spaceId: {
						rangelength: [3,63],
						startswith: true,
						endswith: true,
						spacelower: true,
						notip: true,
						misc: true,
					},
				},
				messages: {
						
				}
			});
			
			$.validator
				.addMethod("startswith", function(value, element) { 
				  return  /^[a-z0-9]/.test(value); 
				}, "Invalid");
			$.validator
				.addMethod("endswith", function(value, element) { 
				  return  /[a-z0-9.]$/.test(value); 
				}, "Invalid");
			$.validator.addMethod("spacelower", function(value,element){return /^[a-z0-9.-]*$/.test(value);}, 
					"Invalid");
			$.validator.addMethod("notip", function(value,element){return !(/^[0-9]+.[0-9]+.[0-9]+.[0-9]+$/.test(value));}, 
					"Invalid");
			$.validator.addMethod("misc", function(value,element){return !(/^.*([.][-]|[-][.]|[.][.]).*$/.test(value));}, 
					"Invalid");
			$("#add-space-form").resetForm();
		}
		
	});

	$('.add-space-button').live("click",
			function(evt){
				$("#add-space-dialog").dialog("open");
			}
		);

	var getCurrentSpaceId = function(){
		var currentItem = $("#spaces-list").selectablelist("currentItem");
		var spaceId = currentItem.data.spaceId;
		return spaceId;
	};

	var getCurrentProviderStoreId = function(){
		var provider = $("#provider-select-box").flyoutselect("value");
		return provider.id;
	};


	$('#add-content-item-dialog').dialog({
		autoOpen: false,
		show: 'blind',
		hide: 'blind',
		height: 250,
		resizable: false,
		closeOnEscape:true,
		modal: true,
		width:500,
		buttons: {
			'Add': function() {
				var that = this;
				if($("#add-content-item-form").valid()){
					var form = $("#add-content-item-form");
					var contentItem = {
							 storeId: getCurrentProviderStoreId(), 
							 spaceId: getCurrentSpaceId(), 
							 contentId: $("#contentId", form).val()};

					var filename = $("#file", form).val();
					
					if(contentItem.contentId == null || contentItem.contentId.trim() == ''){
						contentItem.contentId = filename;
					}
					$("#spaceId", form).val(contentItem.spaceId);
					$("#storeId", form).val(contentItem.storeId);
					var dialog = $("#add-content-item-dialog").find(".ui-dialog");
					dialog.hide();
					dc.store.CheckIfContentItemExists(
						contentItem, 
						{ 
							success: function(exists){
								if(exists){
									if(!confirm("A content ID with this name already exists. Overwrite?")){
										dialog.show();
										return;
									}
								}
	
								$(that).dialog("enable");
								$(that).dialog("close");
	
								var updateFunc =  function(data){
									poller();
									var contentId = data.contentItem.contentId;
									if($("#content-item-list [id='"+contentId+"']").size() == 0){
										addContentItemToList(data.contentItem);
									}
								};
									
								var key = escape(contentItem.storeId+"/"+contentItem.spaceId+"/"+ contentItem.contentId);
								var callback = {
									key: key,
									begin: function(){
										$("#upload-viewer").dialog("open");
									},
									failure: function(){
										alert("upload failed for " + key);
									},
									success: updateFunc,
								};
	
								dc.store.AddContentItem(form, callback);
							},
							
							failure: function(message){
								alert("check for existing content item failed: " + message);
							}
						}
					);
				}
			},
			Cancel: function() {
				$(this).dialog('close');
			}
		},
		close: function() {

		},
		  open: function(e){
			var overwrite = false;
			var that = this;
			$("#add-content-item-form").validate({
				rules: {
					contentItemId: {
						required:true,
						minlength: 1,
						illegalchars: true,
					},
					
					file: {
						required:true,
					}
					
				},
				messages: {
						
				}
			});
			
			$.validator
				.addMethod("illegalchars", function(value, element) { 
				  return  /^[^\\?]*$/.test(value); 
				}, "A Content ID cannot contain  '?' or '\\'");
			
			$("#add-content-item-form").resetForm();
		}
	});
	
	$('#add-space-help-content').expandopanel({
		
	});
	
	var updateContentItem = function(){
		var form = $("#edit-content-item-form");
		var data = form.serialize();
		if(form.valid()){
			var callback = {
				success: function(contentItem){
					dc.done();
					loadContentItem(contentItem);
				},
				failure: function(){
					dc.done();
					alert("an error occurred");
				},
			};
			$('#edit-content-item-dialog').dialog("close");
			dc.busy("Updating...");
			dc.store.UpdateContentItemMimetype(data, callback)
		}
	};


	$('#edit-content-item-dialog').dialog({
		autoOpen: false,
		show: 'blind',
		hide: 'blind',
		height: 250,
		resizable: false,
		closeOnEscape:true,
		modal: true,
		width:500,
		buttons: {
			'Save': updateContentItem,
			Cancel: function() {
				$(this).dialog('close');
			}
		},
		close: function() {

		},
		open: function(e){
			var form = $("#edit-content-item-form",this);
			form.validate({
				rules: {
					contentMimetype: {
					    required:true,
						minlength: 3,
					},
				},
				messages: {
						
				}
			});
			
			$("input",this).bindEnterKey(updateContentItem);
			
		}
	});
	
	
	$('.add-content-item-button').live("click",
			function(evt){
				$("#add-content-item-dialog").dialog("open");
			});
	
	$('.edit-content-item-button').live("click",
			function(evt){
				$("#edit-content-item-dialog").dialog("open");
			});

	
	var scrollToCurrentSpace = function(){
		var spacesList = $("#spaces-list");
		var current = spacesList.selectablelist("currentItem"); 
		
		if(current != null && current != undefined && 
				current.data != null && current.data != undefined ){
			 spacesList
			 	.closest(".dc-item-list-wrapper")
			 	.scrollTo(current.item);
		}
	};
	
	// ///////////////////////////////////////////////////////////
	// Spaces / Content Ajax calls
	// ///////////////////////////////////////////////////////////
	var notEmpty = function(value){
		return value != null && value != undefined && value.length != 0;
	};

	var loadWhatYouCan = function(obj){
		var storeId = obj.storeId;
		var spaceId = obj.spaceId;
		var contentId = obj.contentId;
		
		if(notEmpty(storeId)){
			changeProviderStore(storeId);
			refreshSpaces(storeId, function(){
				if(notEmpty(spaceId)){
					var spacesList = $("#spaces-list");
					spacesList.selectablelist("setCurrentItemById", spaceId, false);
					scrollToCurrentSpace();
					getSpace(spaceId, 
						function(space){
							if(notEmpty(contentId)){
								getContentItem(storeId,spaceId,contentId);
								loadContentItems(space.contents);
							}else{
								loadSpace(space);
							}
						}
					);
				}
			});
			
		}


	};
	
	var changeProviderStore = function(storeId){
		$("#"+PROVIDER_SELECT_ID).flyoutselect("setValueById", storeId, false);
	};
	
	
	var deleteContentItem = function(evt, contentItem){
		evt.stopPropagation();
		if(!dc.confirm("Are you sure you want to delete \n" + contentItem.contentId + "?")){
			return;
		}
		dc.store.DeleteContentItem(contentItem, {
			begin: function(){
				dc.busy( "Deleting content item...");
			},
			success:function(){
				dc.done();
				$("#content-item-list").selectablelist("removeById", contentItem.contentId);
			},
			failure: function(message){
				dc.done();
				alert("failed to delete contentItem: " + message);
			},
		});
	};
	
	var deleteSpace = function(evt, space) {
		evt.stopPropagation();
		if(!dc.confirm("Are you sure you want to delete \n" + space.spaceId + "?")){
			return;
		}
		
		dc.store.DeleteSpace(space, {
			begin: function(){
				dc.busy( "Deleting space...");
			},
			
			success:function(){
				dc.done();

				$("#spaces-list").selectablelist("removeById", space.spaceId);
			},
			
			failure: function(message){
				dc.done();
				alert("failed to delete space!");
			},
		});
	};
	/**
	 * loads the space data into the detail pane
	 */
	var loadSpace = function(space){
		var detail = $("#spaceDetailPane").clone();
		setObjectName(detail, space.spaceId);
		
		// attach delete button listener
		$(".delete-space-button",detail).click(function(evt){
			deleteSpace(evt,space);
		});
		
		
		
		// create access switch and bind on/off listeners
		$(".access-switch", detail).accessswitch({
				initialState: (space.metadata.access=="OPEN"?"on":"off")
			}).bind("turnOn", function(evt, future){
				toggleSpaceAccess(space, future);
			}).bind("turnOff", function(evt, future){
				toggleSpaceAccess(space, future);
			});

		
		
		
		loadProperties(detail, extractSpaceProperties(space));

		var mp = loadMetadataPane(detail, space.extendedMetadata);
		
		$(mp).bind("add", function(evt, future){
				var value = future.value;
				addSpaceMetadata(space.spaceId, value.name, value.value, future);
			}).bind("remove", function(evt, future){
				removeSpaceMetadata(space.spaceId, future.value.name,future);
			});
		
		var tag = loadTagPane(detail, space.metadata.tags);

		$(tag).bind("add", function(evt, future){
			var value = future.value[0];
			addSpaceTag(space.spaceId, value, future);
		}).bind("remove", function(evt, future){
			var value = future.value[0];
			removeSpaceTag(space.spaceId, value, future);
		});

		$("#detail-pane").replaceContents(detail, spaceDetailLayoutOptions);

		loadContentItems(space.contents);
		
	};

	
	var extractSpaceProperties = function(space){
		return [ 
					['Items', space.metadata.count],
					['Created', space.metadata.created],
			   ];
	};

	var extractContentItemProperties = function(contentItem){
		var m = contentItem.metadata;
		return [
			        ["Space", contentItem.spaceId],
			        ["Size", m.size],
			        ["Modified", m.modified],
			        ["Checksum", m.checksum],
		       ];
	};

	var isPdf = function(mimetype){
		return(mimetype.toLowerCase().indexOf("pdf") > -1);
	};
	
	var loadContentItem = function(contentItem){
		
		setHash(contentItem);
		var pane = $("#contentItemDetailPane").clone();
		setObjectName(pane, contentItem.contentId);
		
		$(".download-content-item-button", pane).attr("href", contentItem.downloadURL);

		// attach delete button listener
		$(".delete-content-item-button",pane).click(function(evt){
			deleteContentItem(evt,contentItem);
		});
		
		
		var mimetype = contentItem.metadata.mimetype;
		
		if(mimetype.indexOf("image") == 0){
			loadPreview(pane, contentItem);
		}else if(mimetype.indexOf("video") == 0){
			loadVideo(pane, contentItem);
		}else if(mimetype.indexOf("audio") == 0){
			loadAudio(pane, contentItem);
		}else {
			$(".view-content-item-button", pane).attr("href", contentItem.viewerURL).css("display", "inline-block");
		}
		
		loadProperties(pane, extractContentItemProperties(contentItem));
		// load the details panel
		var mimetype = contentItem.metadata.mimetype;
		$(".mime-type .value", pane).text(mimetype);
		$(".mime-type-image-holder", pane).addClass(dc.getMimetypeImageClass(mimetype));

		var mp = loadMetadataPane(pane, contentItem.extendedMetadata);
		
		
		$(mp).bind("add", function(evt, future){
				var value = future.value;
				addContentItemMetadata(contentItem.spaceId, contentItem.contentId, value.name, value.value, future);
			}).bind("remove", function(evt, future){
				removeContentItemMetadata(contentItem.spaceId, contentItem.contentId, future.value.name,future);
			});
		
		var tag = loadTagPane(pane, contentItem.metadata.tags);

		$(tag).bind("add", function(evt, future){
			var value = future.value[0];
			addContentItemTag(contentItem.spaceId, contentItem.contentId, value, future);
		}).bind("remove", function(evt, future){
			var value = future.value[0];
			removeContentItemTag(contentItem.spaceId, contentItem.contentId, value, future);
		});

		// prepare edit dialog
		var editDialog = $("#edit-content-item-dialog");
		editDialog.find("input[name=storeId]").val(contentItem.storeId);
		editDialog.find("input[name=spaceId]").val(contentItem.spaceId);
		editDialog.find("input[name=contentId]").val(contentItem.contentId);
		editDialog.find("input[name=contentMimetype]").val(mimetype);
			
		$("#detail-pane").replaceContents(pane,contentItemDetailLayoutOptions);
	};

	var contentItemListStatusId = "#content-item-list-status";
	var getFilterText = function(){
		var val = $("#content-item-filter").val();
		if(val == DEFAULT_FILTER_TEXT){
			val = null;
		}
		return val;
	};
	
	var getSpace = function(spaceId, loadHandler){
		clearContents();
		$("#detail-pane").fadeOut("slow");
		dc.store.GetSpace(
			getCurrentProviderStoreId(),
			spaceId, 
			{
				begin: function(){
					dc.busy("Loading space...");
					$(contentItemListStatusId).html("Loading...").fadeIn("slow");
				},
				success: function(space){
					dc.done();
					if(space == undefined || space == null){
						$(contentItemListStatusId).html("Error: space not found.").fadeIn("slow");
					}else{
						setHash(space);
						loadHandler(space);
					}
					$(contentItemListStatusId).fadeOut("fast");
				}, 
				failure:function(info){
					dc.done();
					alert("Get Space failed: " + info);
				},
			}
		);
	};
	
	var getContentItem = function(storeId, spaceId, contentId){
		dc.store.GetContentItem(storeId,spaceId,contentId,{
			begin: function(){
				dc.busy("Loading...");
			},
			
			failure: function(text){
				dc.done();
				alert("get item failed: " + text);
			},

			success: function(data){
				dc.done();
				loadContentItem(data);
			},
		});
	};
	
	
	$("#content-item-list-view").find(".dc-item-list-filter").bind("keyup", $.debounce(500, function(evt){
		reloadContents(getCurrentSpaceId(), null, function(space){loadContentItems(space.contents)});
	}));
	
	var reloadContents = function(spaceId, marker, handler){
		$("#content-item-list").selectablelist("clear");
		var prefix = getFilterText();
		dc.store.GetSpace(
				getCurrentProviderStoreId(),
				getCurrentSpaceId(), 
				{
					begin: function(){
						$(contentItemListStatusId).html("Loading contents...").fadeIn("slow");
					},
					success: function(space){
						dc.done();
						if(space == undefined || space == null){
							$(contentItemListStatusId).html("Error: space not found.").fadeIn("slow");
						}else{
							handler(space);
							$(contentItemListStatusId).fadeOut("fast");
						}
					}, 
					failure:function(info){
						dc.done();
						alert("Get Space failed: " + info);
					},
				},
				{
					prefix: prefix,
					marker: marker,
				});
	};

	var refreshButtonState = null;
	(function(){
		var list = $("#content-item-list");
		var previousList = new Array();
		$("#content-item-list-view").find(".dc-item-list-filter").bind("change",function(){
			previousList = new Array();
			refreshButtonState();
		});
		
		var previousButton = $("#content-item-list-view .previous"); 
		var nextButton = $("#content-item-list-view .next")
		
		refreshButtonState = function(){
			if(previousList.length == 0 ){
				previousButton.removeClass("featured");
			}else{
				previousButton.addClass("featured");
			}

			if(list.selectablelist("length") == 0){
				nextButton.removeClass("featured");
			}else{
				nextButton.addClass("featured");
			}
		};
		
		previousButton.click(function(){
			var that = this;
			var marker = null;
			if(previousList.length > 1){
				//get the second to last marker (marks the previous page)
				marker = previousList[previousList.length-2];
			}
			
			if(previousList.length > 0){
				reloadContents(
						getCurrentSpaceId(), 
						marker,
						function(space){
							//pop the first one off the end- it's the current page
							previousList.pop();
							loadContentItems(space.contents,true);
							refreshButtonState();
						});
			}
		});
		
		nextButton.click(function(){
			var that = this;
			var marker = list.selectablelist("lastItemData").contentId;
			if(marker != null){
				//get the second to last marker (marks the previous page)
				reloadContents(
					getCurrentSpaceId(), 
					marker,
					function(space){
						loadContentItems(space.contents,true);	
						previousList.push(marker);
						refreshButtonState();
					});
			}
		});
		

		
	})();

	var loadContentItems = function(contentItems, fadeIn){
		$("#content-item-list").selectablelist("clear");
		
		if(fadeIn == undefined) fadeIn = true;
		
		if(fadeIn){
			$("#content-item-list-view button,#content-item-list-view input").fadeIn();
		}
		
		for(i in contentItems){
			var ci = {
				contentId:contentItems[i],
				spaceId:getCurrentSpaceId(),
				storeId:getCurrentProviderStoreId()
			};
			
			addContentItemToList(ci);
		}

		refreshButtonState();

	}
	
	var addContentItemToList = function(contentItem){
		var node =  document.createElement("div");
		var actions = document.createElement("div");
		
		var deleteButton = $("<button class='delete-space-button featured icon-only'><i class='pre trash'></i></button>");
		deleteButton.click(function(evt){
			deleteContentItem(evt,
								contentItem);
		});
		$(actions).append(deleteButton);
		$(node).attr("id", contentItem.contentId)
			   .html(contentItem.contentId)
			   .append(actions);
		$("#content-item-list").selectablelist('addItem',node, contentItem);	   
	
	};
	
	var toggleSpaceAccess = function(space, callback){
		var access = space.metadata.access;
		var newAccess = (access == "OPEN") ? "CLOSED":"OPEN";
		dc.busy( "Changing space access..."); 
		dc.ajax({ url: "/duradmin/spaces/space?storeId="+space.storeId+"&spaceId="+escape(space.spaceId), 
			data: "access="+newAccess+"&action=put&method=changeAccess",
			type: "POST",
			cache: false,
			context: document.body, 
			success: function(data){
				dc.done();
				callback.success(data.space);
			},
		    failure: function(textStatus){
				dc.done();
	    		callback.failure(textStatus);
		    },
		});		
	};

	var createSpaceMetadataCall = function(spaceId, data, method,callback){
		var newData = data + "&method=" + method;
		var storeId = getCurrentProviderStoreId();
		return {
			url: "/duradmin/spaces/space?storeId="+storeId+"&spaceId="+escape(spaceId) +"&action=put", 
			type: "POST",
			data: newData,
			cache: false,
			context: document.body, 
			success: function(data){
				callback.success();
			},
		    error: function(xhr, textStatus, errorThrown){
	    		//dc.error("get spaces failed: " + textStatus + ", error: " + errorThrown);
	    		callback.failure(textStatus);
		    },
		};
	};
	

	
	var addSpaceMetadata = function(spaceId, name, value, callback){
		var data = "metadata-name=" + escape(name) +"&metadata-value="+escape(value);
		dc.ajax(createSpaceMetadataCall(spaceId, data, "addMetadata", callback));		
	};

	var removeSpaceMetadata = function(spaceId, name,callback){
		var data = "metadata-name=" + escape(name);
		dc.ajax(createSpaceMetadataCall(spaceId, data, "removeMetadata", callback));		
	};

	var addSpaceTag = function(spaceId, tag, callback){
		var data = "tag="+ escape(tag);
		dc.ajax(createSpaceMetadataCall(spaceId, data, "addTag", callback));		
	};

	var removeSpaceTag = function(spaceId, tag,callback){
		var data = "tag="+escape(tag);
		dc.ajax(createSpaceMetadataCall(spaceId, data, "removeTag", callback));		
	};

	// ///////////////////////////////////////////////////////////////////////////////
	// /content metadata functions
	var createContentItemMetadataCall = function(spaceId, contentId, data, method,callback){
		var newData = data + "&method=" + method;
		var storeId = getCurrentProviderStoreId();
		return {
			url: "/duradmin/spaces/content?storeId="+storeId+"&spaceId="+escape(spaceId) +"&contentId="+escape(contentId) +"&action=put", 
			type: "POST",
			data: newData,
			cache: false,
			context: document.body, 
			success: function(data){
				callback.success();
			},
		    failure: function(textStatus){
	    		callback.failure(textStatus);
		    },
		};
	};
	
	var addContentItemMetadata = function(spaceId, contentId, name, value, callback){
		var data = "metadata-name=" + escape(name) +"&metadata-value="+escape(value);
		dc.ajax(createContentItemMetadataCall(spaceId, contentId, data, "addMetadata", callback));		
	};

	var removeContentItemMetadata = function(spaceId, contentId, name,callback){
		var data = "metadata-name=" + escape(name);
		dc.ajax(createContentItemMetadataCall(spaceId,contentId, data, "removeMetadata", callback));		
	};

	var addContentItemTag = function(spaceId, contentId, tag, callback){
		var data = "tag="+ escape(tag);
		dc.ajax(createContentItemMetadataCall(spaceId,contentId, data, "addTag", callback));		
	};

	var removeContentItemTag = function(spaceId, contentId, tag,callback){
		var data = "tag="+escape(tag);
		dc.ajax(createContentItemMetadataCall(spaceId, contentId, data, "removeTag", callback));		
	};
	

	$("#content-item-list").selectablelist({selectable: true});
	$("#spaces-list").selectablelist({selectable: true});

	var DEFAULT_FILTER_TEXT = "filter";

	$(".dc-item-list-filter").focus(function(){
		if($(this).val() == DEFAULT_FILTER_TEXT){
			$(this).val('');
		};
	}).blur(function(){
		if($(this).val() == ""){
			$(this).val(DEFAULT_FILTER_TEXT);
		};
	}).val(DEFAULT_FILTER_TEXT);
	
	// /////////////////////////////////////////
	// /click on a space list item

	$("#spaces-list").bind("currentItemChanged", function(evt,state){
		if(state.selectedItems.length < 2){
			if(state.item !=null && state.item != undefined){
				getSpace($(state.item).attr("id"), loadSpace);
			}else{
				showGenericDetailPane();
			}
		}else{
			showMultiSpaceDetail();
		}
	});

	$("#spaces-list").bind("selectionChanged", function(evt,state){
		if(state.selectedItems.length == 0){
			showGenericDetailPane();
		}else if(state.selectedItems.length == 1){
			getSpace($(state.item).attr("id"),loadSpace);
		}else{
			showMultiSpaceDetail();
		}
	});


	$("#spaces-list").bind("itemRemoved", function(evt,state){
		clearContents();
		
		showGenericDetailPane();
	});
	// /////////////////////////////////////////
	// /click on a content list item
	$("#content-item-list").bind("currentItemChanged", function(evt,state){
		if(state.selectedItems.length < 2){
			if(state.item != null && state.item != undefined){
				var spaceId = getCurrentSpaceId();
				getContentItem(getCurrentProviderStoreId(),spaceId,$(state.item).attr("id"));
			}else{
				showGenericDetailPane();
			}
		}else{
			showMultiContentItemDetail();
		}
	});

	
	
	$("#content-item-list").bind("selectionChanged", function(evt,state){
		if(state.selectedItems.length == 0){
			showGenericDetailPane();
		}else if(state.selectedItems.length == 1){
			var spaceId = "YYYYYYY";
			/**
			 * @FIXME
			 */
			getContentItem(getCurrentProviderStoreId(),spaceId,$(state.item).attr("id"));
		}else{
			showMultiContentItemDetail();
		}
	});

	// /////////////////////////////////////////
	// /click on a space list item
	var spacesArray = new Array();

	
	$("#spaces-list-view").find(".dc-item-list-filter").bind("keyup", $.debounce(500,function(evt){
			loadSpaces(spacesArray, evt.target.value);
	}));

	var clearContents = function(){
		$("#content-item-list").selectablelist("clear");
		$("#content-item-list-view button").fadeOut();
		$("#content-item-list-view input").val('').fadeOut();
	};

	var clearSpaces = function(){
		$("#spaces-list").selectablelist("clear");
		showGenericDetailPane();
	};

	
	var addSpaceToList = function(space){
		var node =  $.fn.create("div");
		var actions = $.fn.create("div");
		actions.append("<button class='delete-space-button featured icon-only'><i class='pre trash'></i></button>");
		node.attr("id", space.spaceId)
			   .html(space.spaceId)
			   .append(actions);
		$("#spaces-list").selectablelist('addItem',node,space);	   
		
		$(".delete-space-button", node).click(function(evt){
			deleteSpace(evt,space);
		});
	};
	
	var loadSpaces = function(spaces,filter) {
		$("#spaces-list").selectablelist("clear");
		
		var firstMatchFound = false;
		for(s in spaces){
			var space = spaces[s];
			if(filter === undefined || filter == DEFAULT_FILTER_TEXT || space.spaceId.toLowerCase().indexOf(filter.toLowerCase()) > -1){
				addSpaceToList(space);
				if(!firstMatchFound){
					//$("#spaces-list").selectablelist('setCurrentItemById',space.spaceId);	   
					firstMatchFound = true;
				}
			}
			

		}
		
		
	};
	
	var refreshSpaces = function(providerId, /*optional*/successFunc){
		clearContents();
		clearSpaces();
		dc.store.GetSpaces(providerId,{
			begin: function(){
				dc.busy("Loading spaces...");
				$("#space-list-status").html("Loading...").fadeIn("slow");
			},
			success: function(spaces){
				dc.done();
				spacesArray = new Array();
				for(s in spaces){
					spacesArray[s] = {spaceId: spaces[s], storeId: providerId};
				}
				// clear content filters
				$("#content-item-filter").val(DEFAULT_FILTER_TEXT);
				loadSpaces(spacesArray, $("#space-filter").val());
				$("#space-list-status").fadeOut("fast");
				
				if(successFunc != undefined){
					successFunc();
				}
			},
			failure: function(xhr, message){
				dc.done();
				alert("error:" + message);
				$("#space-list-status").fadeOut("fast");
			}
			
		});
	};

	var PROVIDER_SELECT_ID = "provider-select-box";
	
	var setHash = function(obj){
		window.location.hash = buildHash(obj);
	};
	
	var buildHash = function(obj) {
		var hash = obj.storeId;
		var spaceId = obj.spaceId;
		if(spaceId != null && spaceId != undefined){
			hash += "/" + spaceId;
		}
		
		var contentId = obj.contentId;
		if(contentId != null && contentId != undefined){
			hash += "/" + contentId;
		}
		
		return hash;
	};

	var parseHash = function(hash) {
		var pHash = {
			storeId: null,
			spaceId: null,
			contentId: null,
		};
		
		pHash.toString = function(){
			return "storeId: " + this.storeId + ", spaceId: " + this.spaceId + ", contentId: " + this.contentId;
		};

		if(hash != undefined && hash != null){
			var first = hash.indexOf("/");
			if(first > 1){
				pHash.storeId = hash.slice(1, first);
				var second = first + hash.substring(first+1).indexOf("/");
				if(second > first){
					pHash.spaceId = hash.slice(first+1, second+1);
					if(hash.length > second){
						pHash.contentId = unescape(hash.substring(second+2));
					}
				}else{
					pHash.spaceId = hash.substring(second+2);
				}
			}else if(hash.length > 1){
				pHash.storeId = hash.substring(1);
			}
		}
		
		//alert(pHash);
		return pHash;
	};

	var isUploadManagerBusy = function(){
		var inprogress = false;
		$.ajax({
			async:false,
			cache:false,
			url: "/duradmin/spaces/upload",
			success: function(data){
				if(data != null && data.taskList != null && data.taskList.length > 0){
					for(i in data.taskList){
						var props = data.taskList[i].properties;
						if(props.state =='running'){
							inprogress = true;
							break;
						}
					}
				}
			}
		});
		return inprogress;
	};
	
	var initSpacesManager =  function(){
		////////////////////////////////////////////
		//alert user if they are navigating away from 
		//spaces manager while an upload is still underway
		////////////////////////////////////////////
		$("a").each(function(i,element){
			if($(element).attr("href")  != undefined){
				$(element).click(function(e){
					if(isUploadManagerBusy()){
						return confirm(
								"The upload manager is uploading files.  " +
								"\nIf you navigate away from this page, the uploads in process may not complete.");
					}
					return true;
				});
			}
		});
		
		////////////////////////////////////////////
		// initialize provider selection
		////////////////////////////////////////////
		var PROVIDER_COOKIE_ID = "providerId";
		var options = {
			data: storeProviders, // this variable is defined in a script in
									// the head of spaces-manager.jsp
			selectedIndex: 0
		};

		var currentProviderId = options.data[options.selectedIndex].id;
		var cookie = dc.cookie(PROVIDER_COOKIE_ID);
		
		if(cookie != undefined){
			for(i in options.data)
			{
				var pid = options.data[i].id;
				if(pid == cookie){
					options.selectedIndex = i;
					currentProviderId = pid; 
					break;
				}
			}
		}
		
		
		$("#"+PROVIDER_SELECT_ID).flyoutselect(options).bind("changed",function(evt,state){
			dc.cookie(PROVIDER_COOKIE_ID, state.value.id);
			//dc.debug("value changed: new value=" + state.value.label);
			refreshSpaces(state.value.id);
		});		 


		var phash = parseHash(window.location.hash);
		

		if(phash.storeId != null){
			loadWhatYouCan(phash);
			return;
		}

		refreshSpaces(currentProviderId);
	};
	
	
	initSpacesManager();

	// hides the title bar on all dialogs;
	$(".ui-dialog-titlebar").hide();
});