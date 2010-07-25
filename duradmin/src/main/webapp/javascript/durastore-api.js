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
		
		var marker = null;
		if(options.marker != undefined){
			marker = options.marker;
		}

		var prefix = null;
			if(options.prefix != undefined){
				prefix = options.prefix;
			}

		if(callback.begin != undefined){
			callback.begin();
		}
		
		$.ajax({ url: "/duradmin/spaces/space", 
			data: "storeId="+storeProviderId+"&spaceId="+escape(spaceId)+"&prefix="+escape(prefix==null?'':prefix),
			cache: false,
			context: document.body, 
			success: function(data){
				callback.success(data.space);
			},
		    error: function(xhr, textStatus, errorThrown){
	    		//console.error("get space failed: " + textStatus + ", error: " + errorThrown);
	    		//alert("get space failed: " + textStatus);
	    		callback.failure(textStatus);
		    },
		});		
		
	};
	
	/**
	 * @param Object space
	 * @param Object callback The callback must implement success and failure methods. options begin method is supported.
	 */
	dc.store.DeleteSpace = function(space, callback){

		if(callback.begin != undefined){
			callback.begin();
		}

		$.ajax({
			url: "/duradmin/spaces/space", 
			data: "action=delete&storeId="+space.storeId+"&spaceId="+escape(space.spaceId),
			type: "POST",
			success: callback.success,
		    error: function(xhr, textStatus, errorThrown){
	    		//console.error("delete space failed: " + textStatus + ", error: " + errorThrown);
	    		callback.failure(textStatus);
			},
		});
	};

	/**
	 * @param String space  space
	 * @param String access  access 
	 * @param Object callback The callback must implement success and failure methods. options begin method is supported.
	 */
	dc.store.AddSpace = function(space, callback){

		if(callback.begin != undefined){
			callback.begin();
		}

		$.ajax({
			url: "/duradmin/spaces/space", 
			data: "storeId="+space.storeId+"&spaceId="+escape(space.spaceId)+"&access="+space.access,
			type: "POST",
			success: function(data){callback.success(data.space)},
		    error: function(xhr, textStatus, errorThrown){
	    		//console.error("add space failed: " + textStatus + ", error: " + errorThrown);
	    		callback.failure(textStatus);
			},
		});
	};

	/**
	 * Returns a list of spaces
	 * @param Number | String storeProviderId The id of the store provider
	 * @param Object callback
	 * @option Function success(spaces) a handler for an array of spaces
	 * @option Function failure(info) a handler that returns failure info 
	 */
	dc.store.GetSpaces = function(storeProviderId, callback){
		if(callback.begin != undefined){
			callback.begin();
		}

		$.ajax({ url: "/duradmin/spaces", 
				data: "storeId="+storeProviderId+"&f=json",
				cache: false,
				context: document.body, 
				success: function(data){
					callback.success(data.spaces);
			    },
			    error: function(xhr, textStatus, errorThrown){
			    	//console.error("get spaces failed: " + textStatus + "; error: " + errorThrown + "; xhr=" + xhr);
			    	callback.failure(textStatus);
			    	
			    },
		});
		
	};
	
	
	/**
	 * returns contentItem details
	 */
	dc.store.GetContentItem = function(storeProviderId, spaceId, contentItemId, callback){
		if(callback.begin != undefined){
			callback.begin();
		}
		
		$.ajax({ url: "/duradmin/spaces/content",
				data: "storeId="+storeProviderId+"&spaceId="+escape(spaceId)+"&contentId="+escape(contentItemId),
				cache: false,
				type: "GET",
				context: document.body, 
				success: function(data){
					callback.success(data.contentItem);
			    },
			    error: function(xhr, textStatus, errorThrown){
			    	//console.error("get contentItem failed: " + textStatus);
			    	callback.failure(textStatus);
			    },
		});

	};
	
	/**
	 * @param Object contentItem
	 * @param Object callback The callback must implement success and failure methods. options begin method is supported.
	 */
	dc.store.DeleteContentItem = function(contentItem, callback){
		if(callback.begin != undefined){
			callback.begin();
		}
		$.ajax({
			url: "/duradmin/spaces/content", 
			data: "action=delete&storeId="+contentItem.storeId+"&spaceId="+escape(contentItem.spaceId)+"&contentId="+escape(contentItem.contentId),
			type: "POST",
			success: callback.success,
		    error: function(xhr, textStatus, errorThrown){
	    		//console.error("delete content item failed: " + textStatus + ", error: " + errorThrown +", contentItem: " + contentItem);
	    		callback.failure(textStatus);
			},
		});
	};


	/**
	 * @param Object serialized form data
	 * @param Object callback The callback must implement success and failure methods. options begin method is supported.
	 */
	dc.store.UpdateContentItemMimetype = function(data, callback){
		if(callback.begin != undefined){
			callback.begin();
		}
		$.ajax({
			url: "/duradmin/spaces/content", 
			data: data + "&action=put&method=changeMimetype",
			type: "POST",
			success: function(data){
				callback.success(data.contentItem);
			},
		    error: function(xhr, textStatus, errorThrown){
	    		//console.error("failed to update content item mimetype: " + textStatus + 
				//", error: " + errorThrown + "; xhr.status = " + xhr.status);
	    		callback.failure(textStatus);
			},
		});
	};

	
	dc.store.AddContentItem = function(form, future){
		future.begin();

		$(form).ajaxSubmit({
			iframe: true,
			dataType: 'json',
			success: function(data){
				future.success(data);
		    },
		    error: function(xhr, textStatus, errorThrown){
		    	future.failure(textStatus);
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
				//dc.busy("Checking for duplicates...");
			},
			
			failure: function(text){
				//dc.done();
				callback.success(false);
			},

			success: function(contentItem){
				callback.success(contentItem != undefined);
			},
		});
	};
		
})();


