/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

/**
 * 
 * created by Daniel Bernstein
 */

var dc;

$(function(){
	(function(){

		
		///////////////////////////////////////////////////////////////////////
		////duracloud js utils
		///////////////////////////////////////////////////////////////////////
		if(dc == undefined){
			dc = {};
		}
		
		
		dc.log = function(message){
			if(window.console){
				console.log(message);
			}
		};

		dc.debug = function(message){
			if(window.console){
				if(window.opera){
					console.log(message);
				}else{
					console.debug(message);
				}
			}
		};

		dc.error = function(message){
			if(window.console){
				if(window.opera){
					console.log(message);
				}else{
					console.error(message);
				}
			}
		};
		
		dc.checkSession = function(data){
			if(data != undefined && data != null){
				if(data.toString().indexOf("loginForm") > -1){
					alert("Your session has timed out.");
					window.location.reload();
				}
			}
		};
		
		dc.displayErrorDialog = function(xhr, textStatus, errorThrown){
			var errorText = xhr.responseText;
			try{
				var response = $.parseJSON(errorText);
				errorText = response['exception.message'];
			}catch(error){
				
			}

			var errorDialog = $.fn.create("div");
			$(document).append(errorDialog);
			errorDialog.append("<h1>An unexpected error occurred:</h1><p>"+errorText+"</p>");
			
			errorDialog.dialog({
				autoOpen: true,
				show: 'fade',
				hide: 'fade',
				resizable: false,
				height: 300,
				width:500,
				closeOnEscape:true,
				modal: false,
				buttons: {
					"Close": function(){
						$(this).dialog("close");
					},
				},
			});
		};

		dc.ajax = function(innerCallback, outerCallback){
			var callback = $.extend(
					true, 
					{}, 
					innerCallback, 
					{
						success: function(data, status, xhr){
							dc.checkSession(data);
							if(innerCallback.success != undefined){
								innerCallback.success(data);
							}else{
								if(outerCallback != undefined 
										&& outerCallback.success != undefined){
									outerCallback.success(data);
								}
							}
						},
						error: function(xhr, textStatus, errorThrown){
							dc.error(xhr.responseText);
							if(innerCallback.failure != undefined){
								innerCallback.failure(textStatus, xhr, errorThrown);
							}else if(outerCallback != undefined 
										&& outerCallback.failure != undefined){
									outerCallback.failure(textStatus, xhr, errorThrown);
							}else{
								//default error handler
								dc.done();
								dc.displayErrorDialog(xhr, textStatus, errorThrown);
							}
						},
						 
						begin: (innerCallback.begin != undefined ? innerCallback.begin : 
								(outerCallback != undefined && outerCallback.begin != undefined ? outerCallback.begin : undefined)),
					}
			);

			if(callback.begin != undefined){
				callback.begin();
			}
			
			$.ajax(callback);			
		}
		/**
		 * Create a cookie with the given name and value and other optional parameters.
		 *
		 * @example dc.cookie('the_cookie', 'the_value');
		 * @desc Set the value of a cookie.
		 * @example dc.cookie('the_cookie', 'the_value', { expires: 7, path: '/', domain: 'jquery.com', secure: true });
		 * @desc Create a cookie with all available options.
		 * @example dc.cookie('the_cookie', 'the_value');
		 * @desc Create a session cookie.
		 * @example dc.cookie('the_cookie', null);
		 * @desc Delete a cookie by passing null as value. Keep in mind that you have to use the same path and domain
		 *       used when the cookie was set.
		 *
		 * @param String name The name of the cookie.
		 * @param String value The value of the cookie.
		 * @param Object options An object literal containing key/value pairs to provide optional cookie attributes.
		 * @option Number|Date expires Either an integer specifying the expiration date from now on in days or a Date object.
		 *                             If a negative value is specified (e.g. a date in the past), the cookie will be deleted.
		 *                             If set to null or omitted, the cookie will be a session cookie and will not be retained
		 *                             when the the browser exits.
		 * @option String path The value of the path atribute of the cookie (default: path of page that created the cookie).
		 * @option String domain The value of the domain attribute of the cookie (default: domain of page that created the cookie).
		 * @option Boolean secure If true, the secure attribute of the cookie will be set and the cookie transmission will
		 *                        require a secure protocol (like HTTPS).
		 * @author Klaus Hartl/klaus.hartl@stilbuero.de - modified by Daniel Bernstein
		 */
		dc.cookie = function(name, value, options) {
		    if (typeof value != 'undefined') { // name and value given, set cookie
		        options = options || {};
		        if (value === null) {
		            value = '';
		            options.expires = -1;
		        }
		        var expires = '';
		        if (options.expires && (typeof options.expires == 'number' || options.expires.toUTCString)) {
		            var date;
		            if (typeof options.expires == 'number') {
		                date = new Date();
		                date.setTime(date.getTime() + (options.expires * 24 * 60 * 60 * 1000));
		            } else {
		                date = options.expires;
		            }
		            expires = '; expires=' + date.toUTCString(); // use expires attribute, max-age is not supported by IE
		        }
		        // CAUTION: Needed to parenthesize options.path and options.domain
		        // in the following expressions, otherwise they evaluate to undefined
		        // in the packed version for some reason...
		        var path = options.path ? '; path=' + (options.path) : '';
		        var domain = options.domain ? '; domain=' + (options.domain) : '';
		        var secure = options.secure ? '; secure' : '';
		        document.cookie = [name, '=', encodeURIComponent(value), expires, path, domain, secure].join('');
		    } else { // only name given, get cookie
		        var cookieValue = null;
		        if (document.cookie && document.cookie != '') {
		            var cookies = document.cookie.split(';');
		            for (var i = 0; i < cookies.length; i++) {
		                var cookie = jQuery.trim(cookies[i]);
		                // Does this cookie string begin with the name we want?
		                if (cookie.substring(0, name.length + 1) == (name + '=')) {
		                    cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
		                    break;
		                }
		            }
		        }
		        return cookieValue;
		    }
		};
		
		dc.createTable = function(data, /*optional: array*/ columnClasses){
		var table = document.createElement("table");
		for(i = 0; i < data.length; i++){
			var row = document.createElement("tr");
			$(table).append(row);
			for(j = 0; j < data[i].length; j++){
				var cell = document.createElement("td");
				$(row).append(cell);
				$(cell).html(data[i][j]);
				if(columnClasses !=null){
					var columnClass;
					if(j >= columnClasses.length){
						columnClass = columnClasses[ j % columnClasses.length];
					}else{
						columnClass = columnClasses[j];
					}
		
					$(cell).addClass(columnClass)
				}
			}
		}
		return table;
		};
		
		dc.getServiceTypeImageClass = function(serviceName){
			return "service-replicate";
		};
		dc.getMimetypeImageClass = function(mimetype){
		var mtc = "";
		if(mimetype.indexOf("audio") > -1){
			mtc = "audio";
		}else if(mimetype.indexOf("image") > -1){
			mtc ="image";
		}else if(mimetype.indexOf("video") > -1){
			mtc = "video";
		}else if(mimetype.indexOf("xml") > -1){
			mtc = "xml";
		}else if(mimetype.indexOf("zip") > -1){
			mtc = "compression";
		}else{
			mtc = "generic";
		}
		return "mime-type-" + mtc;
		
		};
		

		
		dc.busy = function(message, options){
			var d = 
			   $("#busy-dialog");
			var modal = false;
			var buttons = undefined;
			
			if(options != undefined){
				if(options.modal != undefined){
					modal = options.modal;
				}
				
				if(options.buttons != undefined){
					buttons = options.buttons;
				}
			}
			
			var dOptions = {
					autoOpen: false,
					show: 'fade',
					hide: 'fade',
					resizable: false,
					height: 100,
					closeOnEscape:false,
					modal: modal != undefined ? modal : false,
					width:300,
					close: function() {
						
					},
					
					open: function(e){
					},
			};
			
			if(buttons != undefined){
				dOptions.buttons = buttons;
			}
			
			d.dialog(dOptions);
			
		   $("#busy-dialog-title").html(message);
		   d.dialog("open");
		};
		
		dc.done = function(message){
			$("#busy-dialog").dialog("close");

			if(message != undefined){
				var d = $("#message-dialog");
				d.dialog({
					autoOpen: false,
					show: 'fade',
					hide: 'fade',
					resizable: false,
					height: 100,
					closeOnEscape:true,
					modal: false,
					width:300,
					buttons: { 
						"Close" : function(){ 
							$(this).dialog('close');
						 },
					},
					close: function() {},
					open: function(e){},
				});
				
			   $("#message-dialog-title").html(message);
			   d.dialog("open");
			}
		};

		
		dc.confirm = function(message,evt){
			if(!confirm(message)){
				if(evt != undefined){
					evt.stopPropagation();
					evt.preventDefault();
				}
				return false;
			};
			
			return true;
		};
		

		/**
		 * checks the progress of a remote task and notifies caller of results.
		 */
		var DEFAULT_POLL_INTERVAL = 5000;
		
		dc.checkProgress = function(url, key, callback){
			
			if(callback.count == undefined){
				callback.count = 0;
			}

			var updater = function(progressCallback, data){
				if(data.error){
					_error(data.message);
					progressCallback.failure(message);
					return;
				}
				
				progressCallback.update(data);
				if(data.task != undefined){
					var state = data.task.properties.state;
					
					if(state == 'running' || state == 'initialized'){
						setTimeout(function(){ dc.checkProgress(url,key, progressCallback); }, DEFAULT_POLL_INTERVAL);
					}else if(state == 'success'){
						if(progressCallback.success != undefined){
							progressCallback.success(data);
						}
					}else if(state == 'cancelled'){
						if(progressCallback.cancel != undefined){
							progressCallback.cancel();
						}
					}else{
						if(progressCallback.failure != undefined){
							progressCallback.failure();
						}
					}
				}
			};
			
			dc.ajax({ url: url, 
				cache: false,
				type: "GET",
				success: function(data){
					//ajax response gets set here
					updater(callback, data);
			    },
			    failure: function(textStatus){
			    	alert("updater failed: " + textStatus);
			    },
			});		
		};

	})();
});


