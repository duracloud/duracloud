/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

/**
 * Spaces Manager
 * 
 * @author Daniel Bernstein
 */

var centerLayout, listBrowserLayout, spacesListPane, contentItemListPane,detailPane, spacesManagerToolbar;

$(function(){
	$.require("dc/widget/ui.metadataviewer.js");
	$.require("dc/widget/ui.tagsviewer.js");
	$.require("dc/widget/ui.flyoutselect.js");
	$.require("dc/api/dc.util.paralleltasks.js");


	//reusable validators that are used with various forms.
	//used in conjunctions with the jquery.validate.js and jquery.form
	$.validator
	.addMethod("mimetype", function(value, element) { 
	  return  value == null || value == '' || /^(\w[-]?)*\w\/(\w[-]?)*\w$/.test(value); 
	}, "Invalid Mimetype");

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

	$.validator
	.addMethod("illegalchars", function(value, element) { 
	  return  /^[^\\?]*$/.test(value); 
	}, "A Content ID cannot contain  '?' or '\\'");
	//end validator definitions

	
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

	/*
	 * sets value of hidden object id class
	 */
	var setObjectId = function(pane, objectId){
		$(".object-id", pane).html(objectId);	
	};

	/*
	 * tests if the specified id matches the id of the object loaded in 
	 * the current detail view.
	 */
	var isObjectAlreadyDisplayedInDetail = function(objectId){
	    return false; // Require refresh of space/content 
        //	return(objectId == $("#detail-pane .object-id").html());
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
            var confirmText = "Are you sure you want to delete multiple spaces?";
            var busyText = "Deleting spaces";
            var spaces = $("#spaces-list").selectablelist("getSelectedData");
            if(spaces.length < 2){
                confirmText = "Are you sure you want to delete this space?";
                busyText = "Deleting space";
            }

            if(!dc.confirm(confirmText)){
                return;
            }
            dc.busy(busyText, {modal: true});

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
						dc.busy("Deleting spaces: " + p.successes, {modal: true});
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

		
		$(".add-remove-metadata-button",detail).click(function(evt){
			prepareMetadataDialog("space");
		});

	};

	var getSelectedContentItems = function(){
		var contentItems =  $("#content-item-list").selectablelist("getSelectedData");
		var spaceId = getCurrentSpaceId();
		var storeId = getCurrentProviderStoreId();
		for(i in contentItems){
			var ci = contentItems[i];
			if(ci.spaceId == undefined){
				ci.spaceId = spaceId;
			}
			
			if(ci.storeId == undefined){
				ci.storeId = storeId;
			}
		};
		
		return contentItems;
	};

	
	var getSelectedSpaces = function(){
		var spaces =  $("#spaces-list").selectablelist("getSelectedData");
		var storeId = getCurrentProviderStoreId();
		var i;
		for(i = 0; i < spaces.length; i++){
			var s = spaces[i];
			
			if(s.storeId == undefined){
				s.storeId = storeId;
			}
		};
		return spaces;
	};

	var showMultiContentItemDetail = function(){
		var detail = $("#contentItemMultiSelectPane").clone();
		// attach delete button listener
		$(".delete-content-item-button",detail).click(function(evt){
			if(!dc.confirm("Are you sure you want to delete multiple content items?")){
				return;
			}
			dc.busy("Deleting content items...", {modal: true});
			var contentItems = getSelectedContentItems();
			var job = dc.util.createJob("delete-content-items");	
			var i;
			for(i = 0; i < contentItems.length; i++){
				job.addTask({
					_contentItem: contentItems[i],
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
						dc.busy("Deleting content items: " + p.successes, {modal: true});
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
		

		//attach mimetype edit listener
				
		$(".edit-selected-content-items-button",detail).click(function(evt){
			openContentItemDialog(function(){
				var form = $("#edit-content-item-form");

				if(form.valid()){
					dc.busy("Preparing to update content items...", {modal: true});

					$('#edit-content-item-dialog').dialog("close");

					var contentItems = getSelectedContentItems();
					var job = dc.util.createJob("update-content-items");	
	
					var contentItem,i;
					for(i = 0; i < contentItems.length; i++){
						contentItem = contentItems[i];
						contentItem.contentMimetype = $("input[name=contentMimetype]", form).val();

						job.addTask({
							_contentItem: contentItem,
							execute: function(callback){
								var that = this;
								var citem = that._contentItem;
								var data = serialize(citem);
								dc.store.UpdateContentItemMimetype(data, {
									success:function(){
										callback.success();
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
								dc.busy("Updating content items: " + p.successes, {modal: true});
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
				}
			});
		});

		
		$(".add-remove-metadata-button",detail).click(function(evt){
			prepareMetadataDialog("contentItem");
		});

		

		$("#detail-pane").replaceContents(detail, contentItemDetailLayoutOptions);

	
	};
	
	var prepareMetadataDialog = function(targetListDataType){
		var items, getFunction;
		if(targetListDataType == "contentItem"){
			items = getSelectedContentItems();
			getFunction = function(ci,callback){
				dc.store.GetContentItem(ci.storeId, ci.spaceId, ci.contentId, callback);
			};
		}else{
			items = getSelectedSpaces();
			getFunction = function(space,callback){
				dc.store.GetSpace(space.storeId, space.spaceId, callback);
			};
		}

		aggregateMetadataFromSelection(items,getFunction,{
			success: function(data){
				loadMetadataDialog(data,targetListDataType);
			},
			failure: function(text){
				alert("unable to load selection:" + text);
			},
		});

	};

	var appendToListIfNew = function(newItems, itemList, equalsFunc) {
		var toAppend = [];
		var i,j,ni,item,append;
		for(i = 0; i < newItems.length; i++){
			ni = newItems[i];
			if(itemList.length == 0){
				toAppend.push(ni);
			}else{
				append = true;
				for(j = 0; j <  itemList.length; j++){
					item = itemList[j];
					if(equalsFunc != undefined){
						if(equalsFunc(ni,item)){
							append = false;
							break;
						}
					}else{
						if(ni == item){
							append = false;
							break;
						}
					}
				}
				
				if(append){
					toAppend.push(ni);
				}
			}
		}

		for(i = 0; i < toAppend.length; i++){
			itemList.push(toAppend[i]);
		}
	};


	var aggregateMetadataFromSelection = function(items, getFunction, fcallback){
		dc.busy("Loading selection...", {modal: true});
		var metadataLists = [];
		var tagLists = [];
		var job = dc.util.createJob("load-content-items");	
		for(i in items){
			job.addTask({
				_item: items[i],
				execute: function(callback){
					getFunction(
							this._item,
							{
								success:function(obj){
									metadataLists.push(obj.extendedMetadata);
									tagLists.push(obj.metadata.tags);
									callback.success();
								},
								failure: function(message){
									callback.failure();
								},
							}					
					);
				},
			});
		}

		job.execute({ 
			changed: function(job){
				dc.debug("changed:" + job)
				var p = job.getProgress();
				dc.busy(p.successes  + " content items loaded...", {modal: true});
			},
			cancelled: function(job){
				dc.debug("cancelled:" + job);
				dc.done();
			}, 
			done: function(job){
				dc.log("done:" + job);
				dc.done();
				var metadata = [];
				var tags = [];
				var i;
				for(i = 0; i < metadataLists.length; i++){
					appendToListIfNew(metadataLists[i],metadata, function(a,b){ return a.name == b.name && a.value == b.value;});
				}
				
				for(i = 0; i < tagLists.length; i++){
					appendToListIfNew(tagLists[i],tags);
				}
				
				fcallback.success({
					metadata: metadata,
					tags: tags,
				});
			}, 
		});

		
	};
	
	var loadMetadataDialog = function(data, targetListType){
		var metadataToBeAdded = [];
		var metadataToBeRemoved = [];
		var tagsToBeAdded = [];
		var tagsToBeRemoved = [];

		var mp = createMetadataPane(data.metadata);
		
		var equals = function(a,b){
			return (a.name == b.name && a.value == b.value);
		};

		var removeValueFromList = function(value, list, equals){
			var i,el;
			for(i in list){
				el = list[i];
				if(equals != undefined ? equals(value, el) : value == el){
					list.splice(i,1);
					return el;
				}
			}
			return null;
		};
		
		
		$(mp).bind("dc-add", function(evt, future){
			evt.stopPropagation();
			var value = future.value;
			future.success();
			//if in the removed list, remove from remove list
			removeValueFromList(value,metadataToBeRemoved, equals);
			removeValueFromList(value,metadataToBeAdded, equals);
			metadataToBeAdded.push(value);
		}).bind("dc-remove", function(evt, future){
			evt.stopPropagation();
			future.success();
			var value = future.value;
			if(removeValueFromList(value, metadataToBeAdded, equals) == null){
				removeValueFromList(value, metadataToBeRemoved, equals);
				metadataToBeRemoved.push(value);
			}
		});

		var tag = createTagPane(data.tags);

		$(tag).bind("dc-add", function(evt, future){
			evt.stopPropagation();
			var value = future.value[0];
			future.success();
			removeValueFromList(value,tagsToBeRemoved);
			removeValueFromList(value,tagsToBeAdded);
			tagsToBeAdded.push(value);
		}).bind("dc-remove", function(evt, future){
			evt.stopPropagation();
			var value = future.value;
			future.success();
			if(removeValueFromList(value, tagsToBeAdded) == null){
				removeValueFromList(value, tagsToBeRemoved);
				tagsToBeRemoved.push(value);
			}
		});

		
		var saveFunction = function(){
			var msg = "Applying the following changes: \n";
			for(i in metadataToBeRemoved){
				var m = metadataToBeRemoved[i];
				msg +="\tremoving: " + m.name + "=" + m.value + "\n";
			}

			for(i in tagsToBeRemoved){
				msg +="\tremoving: " + tagsToBeRemoved[i] + "\n";
			}

			for(i in metadataToBeAdded){
				var m = metadataToBeAdded[i];
				msg +="\tadding: " + m.name + "=" + m.value + "\n";
			}

			for(i in tagsToBeAdded){
				msg +="\tadding: " + tagsToBeAdded[i] + "\n";
			}
			
			if(confirm(msg)){
				var params = {
					metadataToRemove: metadataToBeRemoved, 
					metadataToAdd:    metadataToBeAdded,
					tagsToRemove:     tagsToBeRemoved, 
					tagsToAdd:        tagsToBeAdded,
				};
				
				if(targetListType == "contentItem"){
					params.contentItems = getSelectedContentItems();
					bulkUpdateContentMetadata(params);
				}else{
					params.spaces = getSelectedSpaces();
					bulkUpdateSpaceMetadata(params);
				}

				d.dialog("close");
				dc.busy("Preparing to perform update...", {modal: true});
				
			}
		};
		
		
		var d = initializeMetadataDialog(saveFunction);


		var pane = $(".center", d);
		pane.append(mp);
		pane.append(tag);

		dc.done();
		d.dialog("open");
		
	};
	
	var serialize = function(obj){
		var str = "";
		for(p in obj){
			str += "&" + p + "=" + escape(obj[p]);	
		}
		return str;
	};
	
	var bulkUpdateContentMetadata = function(params){
		var job = dc.util.createJob("bulk-update-content-metadata");	
		var contentItems = params.contentItems;
		var i;
		for(i = 0; i < contentItems.length; i++){
			var contentItem = contentItems[i];
			job.addTask({
				_contentItem: contentItem,
				execute: function(callback){
					var that = this;
					var citem = that._contentItem;
					addRemoveContentItemMetadata(citem.spaceId, citem.contentId, params,callback);
				},
			});
		}
		job.execute(createGenericJobCallback("Updating content items: "));
	};

	var bulkUpdateSpaceMetadata = function(params){
		var job = dc.util.createJob("bulk-update-space-metadata");	
		var spaces = params.spaces;
		var i;
		for(i = 0; i < spaces.length; i++){
			var space = spaces[i];
			job.addTask({
				_space: space,
				execute: function(callback){
					addRemoveSpaceMetadata(this._space.spaceId, params,callback);
				},
			});
		}
		job.execute(createGenericJobCallback("Updating spaces: "));
	};

	var createGenericJobCallback = function(updateText){
		return { 
			changed: function(job){
				dc.log("changed:" + job);
				var p = job.getProgress();
				dc.busy(updateText + p.successes, {modal: true});
			},
			cancelled: function(job){
				dc.log("cancelled:" + job);
				dc.done();
			}, 
			done: function(job){
				var p = job.getProgress();
				dc.log("done:" + job);
				var message = "Successfully updated " + p.successes + " item(s).";
				if(p.failures > 0){
					message +=" However there were some errors: " + p.failures + " were not updated successfully.";
				}
				dc.done(message);
				
			},
		};
	};
	
	var initializeMetadataDialog = function(saveFunction){
		var d = $("#add-remove-metadata-dialog");
		d.dialog({
			autoOpen: false,
			show: 'blind',
			hide: 'blind',
			height: 600,
			resizable: false,
			closeOnEscape:true,
			modal: true,
			width:500,
			buttons: {
				'Save': saveFunction,
				Cancel: function() {
					$(this).dialog('close');
				}
			},
			close: function() {

			},
			open: function(e){
				
			}
		});
		var pane = $(".center", d);
		pane.empty();
		return d;
	};

	var showGenericDetailPane = function(){
		$("#detail-pane").replaceContents($("#genericDetailPane").clone(), spaceDetailLayoutOptions);
	};

	// ////////////////////////////////////////
	// //functions for loading metadata, tags and properties

	var createMetadataPane = function(extendedMetadata){
		var viewerPane = $.fn.create("div")
						.metadataviewer({title: "Metadata"})
						.metadataviewer("load",extendedMetadata);
		return viewerPane;
	};

	var createTagPane = function(tags){
		var viewerPane = $.fn.create("div")
						.tagsviewer({title: "Tags"})
						.tagsviewer("load",tags);
		return viewerPane;
	};

	var loadMetadataPane = function(target, extendedMetadata){
		var viewerPane = createMetadataPane(extendedMetadata);
		$(".center", target).append(viewerPane);
		return viewerPane;
	};

	var loadTagPane = function(target, tags){
		var viewerPane = createTagPane(tags);
		$(".center", target).append(viewerPane);
		return viewerPane;
	};

	var loadProperties = function(target, /* array */ properties){
		var propertiesDiv = $(".detail-properties", target).first();
		
		if(propertiesDiv.size() == 0){
			propertiesDiv = $.fn.create("div").addClass("detail-properties");
			$(".center", target).append(propertiesDiv.tabularexpandopanel(
								{title: "Details", data: properties}));
		}else{
			$(propertiesDiv).tabularexpandopanel("setData", properties);
		}
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

                            var modeSets = deployment.userConfigModeSets;
                            for(k in modeSets){

                                var modes = modeSets[k].modes;
                                for(p in modes){
                                    var mode = modes[p];

                                    var userConfigs = mode.userConfigs;
                                    var m;
                                    for(m in userConfigs){
                                        var uc = userConfigs[m];
                                        if(uc.name == "mediaSourceSpaceId" && uc.displayValue == contentItem.spaceId){
                                            sourceMediaSpace = true;
                                            break;
                                        }
                                    }
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


	var loadPreview = function(target,contentItem,space, j2kBaseURL){
		var viewerType = 'iframe';
		var options = {
				'transitionIn'	:	'elastic',
				'transitionOut'	:	'elastic',
				'speedIn'		:	600, 
				'speedOut'		:	200, 
				'overlayShow'	:	false};

		var open = space.metadata.access == "OPEN";
		var externalViewer = j2kBaseURL != null && open;
		if(externalViewer){
			options['width'] = $(document).width()*0.8;
			options['height'] = $(document).height()*0.8;
			options['type'] = 'iframe';
		}else{
			options['type'] = 'image';
		}
		
		var viewerURL,thumbnailURL;
		
		if(externalViewer){
			viewerURL = dc.store.formatJ2kViewerURL(j2kBaseURL, contentItem);
			thumbnailURL = dc.store.formatThumbnail(contentItem, 1,j2kBaseURL);
		}else{
			viewerURL = dc.store.formatDownloadURL(contentItem,false);
			thumbnailURL = dc.store.formatGenericThumbnail(contentItem);
		}

		var div = $.fn.create("div")
					  .expandopanel({title: "Preview"});
		
		$(".view-content-item-button", target)
			.css("display","inline-block")
			.attr("href", viewerURL);

		var thumbnail = $.fn.create("img")
							.attr("src", thumbnailURL)
							.addClass("preview-image");

		var viewerLink = $.fn.create("a").append(thumbnail)
							.attr("href", viewerURL)
							.fancybox(options);
	
		var wrapper = $.fn.create("div")
							.addClass("preview-image-wrapper")
							.append(viewerLink);

		if(!open && j2kBaseURL != null){
			var warning = $.fn.create("div").addClass("warning");
			$(div).expandopanel("getContent").append(warning);
	 		var button = $.fn.create("button")
	 			.addClass("featured")
	 			.css("margin-left","10px")
	 			.html("Open Space");
	 		button.click(function(){
				toggleSpaceAccess(
					space, 
					{
						success:function(newSpace){
							loadContentItem(contentItem,newSpace);
						},
						failure:function(){alert("operation failed")},
					})					 				
			});
	 		warning.append("<span>To use the JP2 Viewer you must open this space.</span>")
	 			   .append(button);
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

	var getStoreType = function(storeId) {
		for(i in storeProviders){
			var store = storeProviders[i];
			if(storeId == store.id){
				return store.type;
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

	$.fx.speeds._default = 10;

	///////////////////////////////////////////
	///Add Space Dialog Definition Start
	
	
	var addSpaceButtonHandler = function(){
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
						dc.busy( "Adding space...",{modal: true});
					},
					success: function(space){
						dc.done();
						if(space == undefined){
							alert("error: space is undefined");
						}else{
							addSpaceToList(space);
							spacesArray.push(space);
							spacesArray.sort(function(a,b){
							   return a.spaceId > b.spaceId;
							});
						
							$("#spaces-list").selectablelist("setCurrentItemById", space.spaceId);
							scrollToCurrentSpace();
						}
						
					},
				}
			);
			$("#add-space-dialog").dialog("close");
		}
	};
	
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
				//the handler is defined externally 
				//to the dialog definition because it 
				//is referenced in two different execution
				//paths.
				addSpaceButtonHandler();
			},
			Cancel: function(evt) {
				$(this).dialog('close');
			}
		},
		
		close: function() {
	
		},
		
		open: function(e){
			$("#add-space-form").resetForm();

			//wrapping in a setTimeout seems to be necessary 
			//to get this to run properly:  the dialog must be 
			//visible before the focus can be set.
			setTimeout(function(){
				$("#add-space-form #spaceId").focus();
			});
		}
		
	});

	//the dialog is built only once - thus the follow is not called
	//in the open function
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

	//implements enter key behavior
	$("#add-space-form #spaceId").bindEnterKey(
		$.debounce(200, function(evt){
			evt.stopPropagation();
			addSpaceButtonHandler();
		})
	);
	
	///////////////////////////////////////////
	///Add Space Dialog Definition End ^
	///////////////////////////////////////////

	$('.add-space-button').live("click",
			function(evt){
				$("#add-space-dialog").dialog("open");
			}
		);

	var getCurrentSpaceId = function(){
		var currentItem = $("#spaces-list").selectablelist("currentItem");
		if(currentItem != null && currentItem.data != null){
			var spaceId = currentItem.data.spaceId;
			return spaceId;
		}else{
			return null;
		}
	};

	var getCurrentProviderStoreId = function(){
		var provider = $("#provider-select-box").flyoutselect("value");
		return provider.id;
	};

	///////////////////////////////////////////
	///Add Content Item Dialog Definition Start
	///////////////////////////////////////////

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
									var ci = data.contentItem;
									var storeId = getCurrentProviderStoreId();
									var spaceId = getCurrentSpaceId();
									if( ci.storeId == storeId && ci.spaceId == spaceId && $("#content-item-list [id='"+ci.contentId+"']").size() == 0){
										addContentItemToList(ci);
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
					contentMimetype: {
						mimetype: true,
					},
					file: {
						required:true,
					}
					
				},
				messages: {
						
				}
			});
			
			
			$("#add-content-item-form").resetForm();
		}
	});

	///////////////////////////////////////////
	///Add Content Item Dialog Definition End
	///////////////////////////////////////////

	$('#add-space-help-content').expandopanel({
		
	});
	

	var openContentItemDialog = function(saveFunction, contentItem){
		var d = $('#edit-content-item-dialog');

		// prepare edit dialog
		d.find("input[name=storeId]").val(contentItem ? contentItem.storeId : "");
		d.find("input[name=spaceId]").val(contentItem ? contentItem.spaceId : "");
		d.find("input[name=contentId]").val(contentItem ? contentItem.contentId : "");
		d.find("input[name=contentMimetype]").val(contentItem ? contentItem.metadata.mimetype : "");
		
		d.dialog({
			autoOpen: false,
			show: 'blind',
			hide: 'blind',
			height: 250,
			resizable: false,
			closeOnEscape:true,
			modal: true,
			width:500,
			buttons: {
				'Save': saveFunction,
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
							mimetype: true,
						},
					},
					messages: {
							
					}
				});


				
				$("input",this).bindEnterKey(saveFunction);
				
			}
		});
		
		d.dialog("open");
		
	};

	
	
	$('.add-content-item-button').live("click",
			function(evt){
				$("#add-content-item-dialog").dialog("open");
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
								getContentItem(storeId,spaceId,contentId,space);
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
				dc.busy( "Deleting content item...", {modal: true});
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
				dc.busy( "Deleting space...",{modal: true});
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
		setObjectId(detail,space.spaceId);
		
		var center = $(".center", detail);
		
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
		
		if(space.itemCount == null || space.itemCount < 0){
			//attach poller if itemCount is null or -1
			var pollItemCount = function(){
				dc.store.GetSpace(
						space.storeId,
						space.spaceId, 
						{
							success: function(s){
								if(s != undefined && s != null){
                                    loadProperties(center, extractSpaceProperties(s));
									if(s.itemCount == null || s.itemCount < 0){
										setTimeout(pollItemCount, 5000);
									}
								}
							}, 
							failure:function(info){
								alert("Get Space failed for: " + space.spaceId);
							},
						}
					);				
			};
			
			setTimeout(pollItemCount, 5000);
		}
		
		var mp = loadMetadataPane(detail, space.extendedMetadata);
		
		$(mp).bind("dc-add", function(evt, future){
				var value = future.value;
				addSpaceMetadata(space.spaceId, value.name, value.value, future);
			}).bind("dc-remove", function(evt, future){
				removeSpaceMetadata(space.spaceId, future.value.name,future);
			});
		
		var tag = loadTagPane(detail, space.metadata.tags);

		$(tag).bind("dc-add", function(evt, future){
			var value = future.value[0];
			addSpaceTag(space.spaceId, value, future);
		}).bind("dc-remove", function(evt, future){
			var value = future.value;
			removeSpaceTag(space.spaceId, value, future);
		});

		$("#detail-pane").replaceContents(detail, spaceDetailLayoutOptions);

		loadContentItems(space.contents);
		
	};

	
	var extractSpaceProperties = function(space){
		return [ 
					['Items', (space.itemCount == null || space.itemCount == undefined || space.itemCount < 0 ? space.metadata.count + ": performing exact count <img src='/duradmin/images/wait.gif'/>":space.itemCount)],
					['Created', space.metadata.created],
                    ['Size', space.metadata.size],                    
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
	
	var j2kViewerBaseURL = "";
	
	var loadContentItem = function(/*object*/contentItem,/*object*/ space){
		setHash(contentItem);
		var pane = $("#contentItemDetailPane").clone();
		setObjectName(pane, contentItem.contentId);
		setObjectId(pane,contentItem.spaceId+"/"+contentItem.contentId);
        
		$(".download-content-item-button", pane)
			.attr("href", dc.store.formatDownloadURL(contentItem));

		$(".delete-content-item-button",pane)
			.click(function(evt){
				deleteContentItem(evt,contentItem);
			});
		
		var mimetype = contentItem.metadata.mimetype;
		
		if(mimetype.indexOf("image") == 0){
			if(j2kViewerBaseURL != ""){
				loadPreview(pane,contentItem,space, j2kViewerBaseURL);
			}else{
				dc.service.GetJ2kBaseURL({
					success:function(url){
						j2kViewerBaseURL = url;
						loadPreview(pane,contentItem,space, j2kViewerBaseURL);
					},
					failure: function(text){
						alert("GetJ2kBaseURL failed: " + text);
					}
				});			
			}			
		}else if(mimetype.indexOf("video") == 0){
			loadVideo(pane, contentItem);
		}else if(mimetype.indexOf("audio") == 0){
			loadAudio(pane, contentItem);
		}else {
			var viewerURL= dc.store.formatDownloadURL(contentItem, false);
			$(".view-content-item-button", pane).attr("href", viewerURL).css("display", "inline-block");
		}

		$(".durastore-link", pane).attr("href", contentItem.durastoreURL);

		loadProperties(pane, extractContentItemProperties(contentItem));
		// load the details panel
		var mimetype = contentItem.metadata.mimetype;
		$(".mime-type .value", pane).text(mimetype);
		$(".mime-type-image-holder", pane).addClass(dc.getMimetypeImageClass(mimetype));

		var mp = loadMetadataPane(pane, contentItem.extendedMetadata);
		
		
		$(mp).bind("dc-add", function(evt, future){
				var value = future.value;
				addContentItemMetadata(contentItem.spaceId, contentItem.contentId, value.name, value.value, future);
			}).bind("dc-remove", function(evt, future){
				removeContentItemMetadata(contentItem.spaceId, contentItem.contentId, future.value.name,future);
			});
		
		var tag = loadTagPane(pane, contentItem.metadata.tags);

		$(tag).bind("dc-add", function(evt, future){
			var value = future.value[0];
			addContentItemTag(contentItem.spaceId, contentItem.contentId, value, future);
		}).bind("dc-remove", function(evt, future){
			var value = future.value;
			removeContentItemTag(contentItem.spaceId, contentItem.contentId, value, future);
		});

		$(".edit-content-item-button",pane).click(
				function(evt){
					openContentItemDialog(function(){
						var form = $("#edit-content-item-form");
						var data = form.serialize();
						if(form.valid()){
							var callback = {
								success: function(contentItem){
									dc.done();
									loadContentItem(contentItem,space);
								},
								failure: function(text){
									dc.done();
									alert("failed to update content item.");
								},
							};
							$('#edit-content-item-dialog').dialog("close");
							dc.busy("Updating mime type", {modal: true});
							dc.store.UpdateContentItemMimetype(data, callback)
						}
					}, contentItem);
				}
			);			
		
		$("#detail-pane").replaceContents(pane,contentItemDetailLayoutOptions);

	

	};

	var contentItemListStatusId = "#content-item-list-status";
	
	var showContentItemListStatus = function(text){
		if(text == null || text == undefined || text == ''){
			$(contentItemListStatusId).fadeOut("fast").html('');
		}else{
			$(contentItemListStatusId).html("Loading...").fadeIn("slow");
		}
	};
	
	var getFilterText = function(){
		return $("#content-item-filter").val();
	};

	var getSpace = function(spaceId, loadHandler){
		if(isObjectAlreadyDisplayedInDetail(spaceId)){
			return;
		}
		clearContents();
		clearPageHistory();
		$("#detail-pane").fadeOut("slow");
		dc.store.GetSpace(
			getCurrentProviderStoreId(),
			spaceId, 
			{
				begin: function(){
					dc.busy("Loading...");
					showContentItemListStatus("Loading...");
				},
				success: function(space){
					dc.done();
					if(space == undefined || space == null){
						dc.error("error: space == " + space);
					}else{
						setHash(space);
						loadHandler(space);
					}
					showContentItemListStatus();
				}, 
				failure:function(info){
					dc.done();
					alert("Get Space failed: " + info);
				},
			}
		);
	};
	
	var getContentItem = function(storeId, spaceId, contentId,space){
		if(isObjectAlreadyDisplayedInDetail(spaceId+"/"+contentId)){
			return;
		}

		dc.store.GetContentItem(storeId,spaceId,contentId,{
			begin: function(){
				dc.busy("Loading...");
			},
			
			failure: function(text, xhr){
				dc.done();
				if(xhr.status == 404){
					alert(contentId + " does not exist.");
				}else{
					alert("Unable to retrieve content [" + contentId + "]:" + text);
				}
			},

			success: function(data){
				dc.done();
				loadContentItem(data,space);
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
						showContentItemListStatus("Loading contents...");
					},
					success: function(space){
						dc.done();
						if(space == undefined || space == null){
							showContentItemListStatus("Error: space not found.");
						}else{
							handler(space);
							showContentItemListStatus();
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
	var clearPageHistory = null;
	(function(){
		var list = $("#content-item-list");
		var previousList = new Array();
		$("#content-item-list-view").find(".dc-item-list-filter").bind("change",function(){
			previousList = new Array();
			refreshButtonState();
		});
		
		var previousButton = $("#content-item-list-view .previous"); 
		var nextButton = $("#content-item-list-view .next")
		
		clearPageHistory = function(){
			previousList = new Array();
			previousButton.makeHidden();
			nextButton.makeHidden();
		};

		refreshButtonState = function(){
			previousButton.makeVisible(previousList.length > 0);
			var itemData = list.selectablelist("lastItemData");
			var marker = null;

			if(itemData != null){
				marker = itemData.contentId;
			}
			
			//if there are no items in the current view
			if(marker == null){
				//get previous page marker
				if(previousList.length > 0){
					marker = previousList[previousList.length-1];
				}
			}

			var prefix = getFilterText();
			
			dc.store.GetSpace(
				getCurrentProviderStoreId(),
				getCurrentSpaceId(), 
				{	
					begin: function(){
						nextButton.makeHidden();
					},
					success: function(space){
						nextButton.makeVisible(space.contents.length > 0);
						showContentItemListStatus();
					}, 
					failure:function(info){
						alert("next page failed: " + info);
						showContentItemListStatus();

					},
				},
				{prefix: prefix, marker: marker}						
			);
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
						});
			}
		});
		
		nextButton.click(function(){
			var that = this;
			var itemData =  list.selectablelist("lastItemData");
			var marker = null;
			if(itemData != null){
				marker = itemData.contentId;
			}
			
			if(marker != null){
				reloadContents(
					getCurrentSpaceId(), 
					marker,
					function(space){
						previousList.push(marker);
						loadContentItems(space.contents,true);	
					}
				);
			}else{
				//it means that there weren't any items in the list
				//in this case grap the previous marker if there was one.
				if(previousList.length > 0){
					//get the last marker (marks the previous page)
					marker = previousList[previousList.length-1];
				}				

				reloadContents(
					getCurrentSpaceId(), 
					marker,
					function(space){
						loadContentItems(space.contents,true);	
					}
				);
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
        var content = document.createElement("span");

		var deleteButton = $("<button class='delete-space-button featured icon-only'><i class='pre trash'></i></button>");
		deleteButton.click(function(evt){
			deleteContentItem(evt,
								contentItem);
		});
		$(actions).append(deleteButton);
        $(content).attr("class", "dc-item-content")
                  .html(contentItem.contentId);
		$(node).attr("id", contentItem.contentId)
			   .append(content)
			   .append(actions);
		$("#content-item-list").selectablelist('addItem',node, contentItem);	   
	
	};
	
	var toggleSpaceAccess = function(space, callback){
		var access = space.metadata.access;
		var newAccess = (access == "OPEN") ? "CLOSED":"OPEN";
		dc.busy( "Changing space access...", {modal: true}); 
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

	var addRemoveSpaceMetadata = function(spaceId, params,callback){
		dc.ajax(createSpaceMetadataCall(spaceId, formatBulkMetadataUpdateParams(params), "addRemove", callback));		
	};
	
	var formatBulkMetadataUpdateParams = function(params){
		var data = "";
		data += formatMetadataList(params.metadataToRemove, "remove");
		data += formatMetadataList(params.metadataToAdd, "add");
		data += formatParamList(params.tagsToRemove, "tag", "remove");
		data += formatParamList(params.tagsToAdd, "tag", "add");
		return data;
	};

	var formatMetadataList = function(list, fieldNameModifier){
		var i, list,item,data;
		data = "";
		for(i = 0; i < list.length; i++){
			item = list[i];
			data += "&metadata-name-"+fieldNameModifier+"-" + i + "=" + escape(item.name);
			data += "&metadata-value-"+fieldNameModifier+"-" + i + "=" + escape(item.value);
		}
		return data;
	};

	var formatParamList = function(list, fieldPrefix, fieldNameModifier){
		var i, list,item,data;
		data = "";
		for(i = 0; i < list.length; i++){
			item = list[i];
			data += "&" + fieldPrefix + "-" + fieldNameModifier + "-"+ i + "=" + escape(item);
		}
		return data;
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
	
	var addRemoveContentItemMetadata = function(spaceId, contentId, params,callback){
		dc.ajax(
			createContentItemMetadataCall(
				spaceId,
				contentId,
				formatBulkMetadataUpdateParams(params), 
				"addRemove", 
				callback));		
	};


	$("#content-item-list").selectablelist({selectable: true});
	$("#spaces-list").selectablelist({selectable: true});

	// /////////////////////////////////////////
	// /click on a space list item

	var handleSpaceListStateChangedEvent = function(evt, state){
		try{
			
			if(state.selectedItems.length == 0){
				//uncheck 'check all' box
				$("#check-all-spaces").attr("checked", false);
				var currentItem = state.currentItem;
				if(currentItem !=undefined && currentItem != null){
					var spaceId = $(currentItem.item).attr("id");
					if(spaceId != undefined){
						getSpace(spaceId, loadSpace);
					}else{
						dc.error("spaceId is undefined");
					}
				}else{
					//do nothing;
				}
			}else{
				showMultiSpaceDetail();
			}
		}catch(err){
			dc.error(err);
		}
	};
	
	$("#spaces-list").bind("currentItemChanged", function(evt,state){
		handleSpaceListStateChangedEvent(evt, state);
	});

	$("#spaces-list").bind("selectionChanged", function(evt,state){
		handleSpaceListStateChangedEvent(evt, state);
	});

	$("#spaces-list").bind("itemRemoved", function(evt,state){
		clearContents();
		showGenericDetailPane();
	});

	// /////////////////////////////////////////
	// /click on a content list item
	var handleContentListStateChangedEvent = function(evt, state){
		try{
			if(state.selectedItems.length == 0){
				//uncheck 'check all' box
				$("#check-all-content-items").attr("checked", false);
				var currentItem = state.currentItem;
				if(currentItem !=undefined && currentItem != null){
					var spaceId = getCurrentSpaceId();
					if(spaceId != undefined){
						var contentId = $(currentItem.item).attr("id");
						if(isObjectAlreadyDisplayedInDetail(spaceId+"/"+contentId)){
							return;
						}
						dc.store.GetSpace(
								getCurrentProviderStoreId(),
								spaceId, 
								{
									begin: function(){
										dc.busy("Loading...");
									},
									success: function(space){
										getContentItem(getCurrentProviderStoreId(),spaceId,contentId,space);
									}, 
									failure:function(info){
										dc.done();
										alert("Get Space failed: " + info);
									},
								}
							);

					}else{
						dc.error("spaceId is undefined");
					}
				}else{
					//do nothing
				}
			}else{
				showMultiContentItemDetail();
			}
		}catch(err){
			dc.error(err);
		}
	};
	$("#content-item-list").bind("currentItemChanged", function(evt,state){
		handleContentListStateChangedEvent(evt,state);
	});

	$("#content-item-list").bind("selectionChanged", function(evt,state){
		handleContentListStateChangedEvent(evt,state);
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
        $("#provider-logo").removeClass();
        $("#provider-logo").addClass(getStoreType(getCurrentProviderStoreId()) + '-logo');

		$("#spaces-list").selectablelist("clear");
		var firstMatchFound = false;
		for(s in spaces){
			var space = spaces[s];
			if(filter === undefined || space.spaceId.toLowerCase().indexOf(filter.toLowerCase()) > -1){
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
				dc.busy("Loading spaces...", {modal: true});
				$("#space-list-status").html("Loading...").fadeIn("slow");
			},
			success: function(spaces){
				dc.done();
				spacesArray = new Array();
				for(s in spaces){
					spacesArray[s] = {spaceId: spaces[s], storeId: providerId};
				}
				// clear content filters
				$("#content-item-filter").val('');
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
	
});