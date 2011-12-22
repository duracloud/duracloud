/**
 * 
 * @author Daniel Bernstein
 */

/**
 * Durastore API 
 */

var dc; 
(function(){
	if(dc == undefined){
		dc ={};
	}
	
	dc.store = {};
	
	/**
	 * @param String spaceId  space id 
	 * @param Object callback The callback must implement success and failure methods.
	 * @option Function success(spaces) spaces is an array of spaces objects
	 * @option Function failure(info) 
	 * @param Object options
	 * @option String marker - the last content item id of the previous page
	 * @option String prefix - a filters the results to show only those matching the prefix
	 */

	 dc.store.GetSpace = function(storeProviderId,spaceId,callback,options){
		if(options == undefined){
			options = {};
		}
		
		var marker = '';
		if(options.marker){
			marker = options.marker;
		}

		var prefix = '';
		if(options.prefix){
			prefix = options.prefix;
		}

		
		dc.ajax({
			url: "/duradmin/spaces/space", 
			data: "storeId="+storeProviderId+"&spaceId="+encodeURIComponent(spaceId)+"&prefix="+encodeURIComponent(prefix)+"&marker="+encodeURIComponent(marker),
			cache: false,
			async: options.async != undefined ? options.async : true,
			context: document.body,
			success: function(data){
				callback.success(data.space);
			},
		}, callback);		
	};
	
	/**
	 * @param Object space
	 * @param Object callback The callback must implement success and failure methods. options begin method is supported.
	 */
	dc.store.DeleteSpace = function(space, callback){
		dc.ajax({
			url: "/duradmin/spaces/space", 
			data: "action=delete&storeId="+space.storeId+"&spaceId="+encodeURIComponent(space.spaceId),
			type: "POST",
			success: callback.success,
		    failure: callback.failure,
		}, callback);
	};

	/**
	 * @param String space  space
	 * @param bool publicFlag  indicates whether the space should be created with public access enabled. 
	 * @param Object callback The callback must implement success and failure methods. options begin method is supported.
	 */
	dc.store.AddSpace = function(space, publicFlag, callback){
		dc.ajax(
			{
				url: "/duradmin/spaces/space", 
				data: "storeId="+space.storeId+"&spaceId="+encodeURIComponent(space.spaceId)+"&publicFlag="+publicFlag,
				type: "POST",
				success: function(data){
					callback.success(data.space)
				},
			    failure:callback.failure,
			},callback);
	};

	/**
	 * Returns a list of spaces
	 * @param Number | String storeProviderId The id of the store provider
	 * @param Object callback
	 * @option Function success(spaces) a handler for an array of spaces
	 * @option Function failure(info) a handler that returns failure info 
	 */
	dc.store.GetSpaces = function(storeProviderId, callback){
		dc.ajax({ 
				url: "/duradmin/spaces", 
				data: "storeId="+storeProviderId+"&f=json",
				cache: false,
				success: function(data){
					callback.success(data.spaces)
				},
				failure:callback.failure,
		},callback);
		
	};
	
	
	/**
	 * returns contentItem details
	 */
	dc.store.GetContentItem = function(storeProviderId, spaceId, contentItemId, callback){
		dc.ajax({
				url: "/duradmin/spaces/content",
				data: "storeId="+storeProviderId+"&spaceId="+encodeURIComponent(spaceId)+"&contentId="+encodeURIComponent(contentItemId),
                cache: false,
				success: function(data){
					callback.success(data.contentItem);
			    },
			    failure: callback.failure,
		},callback);

	};
	
	/**
	 * @param Object contentItem
	 * @param Object callback The callback must implement success and failure methods. optional begin method is supported.
	 */
	dc.store.DeleteContentItem = function(contentItem, callback){
		dc.ajax({
			url: "/duradmin/spaces/content", 
			data: "action=delete&storeId="+contentItem.storeId+"&spaceId="+encodeURIComponent(contentItem.spaceId)+"&contentId="+encodeURIComponent(contentItem.contentId),
			type: "POST",
			success: callback.success,
		    failure: callback.failure,
		},callback);
	};

	dc.store._appendNVPair = function(name, value){
	    return "&"+name+"="+encodeURIComponent(value);
	}

	/**
     * @param Object contentItem
     * @param Object callback The callback must implement success and failure methods. optional begin method is supported.
     */
    dc.store.copyContentItem = function(storeId, spaceId, contentId, destStoreId, destSpaceId, destContentId, deleteOriginal, callback){
        var anvp = dc.store._appendNVPair;
        return dc.ajax({
                        url: "/duradmin/spaces/content", 
                        data: "".concat(
                              anvp("action", "put"),
                              anvp("method", "copy"),
                              anvp("deleteOriginal", deleteOriginal),
                              anvp("storeId", storeId),
                              anvp("spaceId", spaceId),
                              anvp("contentId", contentId),
                              anvp("destStoreId", destStoreId),
                              anvp("destSpaceId", destSpaceId),
                              anvp("destContentId", destContentId)
                            ),
                        type: "POST",
                        success: function(result){
                            callback.success(result.contentItem);
                        }
                    },callback);
    };

	
	/**
	 * @param Object serialized form data
	 * @param Object callback The callback must implement success and failure methods. optional begin method is supported.
	 */
	dc.store.UpdateContentItemMimetype = function(data, callback){
		dc.ajax({
			url: "/duradmin/spaces/content", 
			data: data + "&action=put&method=changeMimetype",
			type: "POST",
			success: function(data,xhr){
				if(data.contentItem != undefined){
					callback.success(data.contentItem);
				}else{
					this.failure(data,xhr);
				}
			},
		    failure: callback.failure,
		},callback);
	};

	/**
	 * Adds a content item.  
	 * @param Object/String a reference to a form dom node or the form's id
	 * @param Object future an object implementing a success and failure method.
	 */
	dc.store.AddContentItem = function(form, future){
		future.begin();
		$(form).ajaxSubmit({
			iframe: true,
			dataType: 'json',
			success: function(data){
				dc.checkSession(data);
				future.success(data);
		    },
		    error: function(xhr, status, errorThrown){
		    	future.failure(status,xhr);
		    },
		});
	};
	
	/**
	 * checks if the content item already exists
	 * @param spaceId
	 * @param contentId
	 * @param storeId 
	 * 
	 */
	dc.store.CheckIfContentItemExists = function(contentItem, callback){
		dc.store.GetContentItem(contentItem.storeId,contentItem.spaceId,contentItem.contentId,{
			begin: function(){
			},
			
			failure: function(text){
				callback.success(false);
			},

			success: function(contentItem){
				callback.success(contentItem != undefined);
			},
		});
	};
	
	/**
	 * 
	 */
	dc.store.formatJ2kViewerURL = function(/*string*/j2kViewerBaseURL, /*object*/contentItem, /*boolean*/ openSpace){
		var contentUrl = contentItem.durastoreURL;
        if(!openSpace){
            contentUrl = contentUrl.replace("http://", "https://");}
        return j2kViewerBaseURL + "/viewer.html?rft_id=" + encodeURIComponent(contentUrl);
	};	

	
	/**
	 * 
	 */
	dc.store.formatThumbnail = function(/*object*/contentItem, /*int*/ size, /*optional - string*/j2kViewerBaseURL, /*boolean*/ openSpace ){
        var contentUrl = contentItem.durastoreURL;
        if(!openSpace)
            contentUrl = contentUrl.replace("http://", "https://");
        return j2kViewerBaseURL+
    			"/resolver?url_ver=Z39.88-2004&rft_id="+encodeURIComponent(contentUrl)+"&" +
                "svc_id=info:lanl-repo/svc/getRegion&svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000&" +
                "svc.format=image/png&svc.level="+size+"&svc.rotate=0&svc.region=0,0,500,500";
    };


	var GENERIC_THUMBNAIL_PREFIXES = ["image", "video", "text", "pdf"];
	
	/**
	 * 
	 */
    dc.store.formatGenericThumbnail = function(/*object*/contentItem){
        var mimetype = contentItem.properties.mimetype;
    	var gtf,i;
    	for(i in GENERIC_THUMBNAIL_PREFIXES){
    		gtf = GENERIC_THUMBNAIL_PREFIXES[i];
    		if(mimetype.indexOf(gtf) == 0){
    			return "/duradmin/images/generic-thumb-" + gtf + ".png";
    		}
    	}
    	return "/duradmin/images/generic-thumb-other.png";
    };
    
    /**
     * 
     */
	dc.store.formatDownloadURL = function(/*object*/contentItem, /*boolean*/ asAttachment){
		if(asAttachment == undefined) asAttachment = true;
		return "/duradmin/download/contentItem?spaceId=" +  encodeURIComponent(contentItem.spaceId) +
			   "&contentId=" + encodeURIComponent(contentItem.contentId) + "&storeID=" + contentItem.storeId +
			   "&attachment=" + asAttachment;
	};	

	/**
	 * @param storeId
	 * @param spaceId
	 */
    dc.store.GetUnassignedSpaceAcls = function(storeId,spaceId, callback){
        dc.ajax({
            url: "/duradmin/spaces/acls/unassignedAcls?storeId="+storeId+"&spaceId="+spaceId, 
            async: false,
            cache: false,
            success: function(data){
                callback.success(data.acls);
            },
        }, callback);
    };
    
    /**
     * @param storeId
     * @param spaceId
     */
    dc.store.GetSpaceAcls = function(storeId,spaceId, callback){
        dc.ajax({
            url: "/duradmin/spaces/acls?storeId="+storeId+"&spaceId="+spaceId, 
            async: false,
            cache: false,
            success: function(data){
                callback.success(data.acls);
            },
        }, callback);
    };
    
    /**
     * @formData is serialized form data containing the following required fields:
     *             storeId 
     *             spaceId
     *           and one or more of the following non required fields:
     *             read (users/groups to give read access to)
     *             write (users/groups to give write access to)
     * @param add - optional parameter. If value is set to true the acls in the form
     *               will be added to the existing space acls rather than replacing them.              
     *             
     */
    dc.store.UpdateSpaceAcls = function(/*serialized form data*/formData, /*boolean*/ add, callback){
        dc.ajax({
            url: "/duradmin/spaces/acls?action="+(add ? "add":""), 
            async: true,
            data: formData,
            type: "post",
            success: function(data){
                callback.success(data.acls);
            },
        });
    };
})();


