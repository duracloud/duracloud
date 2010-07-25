/**
 * 
 * created by Daniel Bernstein
 */

var dc;

(function(){
	var _log = function(message){
		if(window.console){
			console.log(message);
		}
	};

	var _debug = function(message){
		if(window.console){
			console.debug(message);
		}
	};

	var _error = function(message){
		if(window.console){
			console.error(message);
		}
	};

	////////////////////////////////////////////////////////////////////
	//jquery extensions
	///////////////////////////////////////////////////////////////////
	$.fn.makeVisible = function() {
		return $(this).css("visibility", "visible");
	};
	
	$.fn.makeHidden = function() {
		return $(this).css("visibility", "hidden");
	};
	
	$.fn.nearestOfClass = function(className){
		var nearest = (this.hasClass(className)) ? this : this.closest("." + className);
		return $(nearest);
	};
	
	$.fn.toggleOpenClosed = function(){
		$(this).nearestOfClass("button")
				.toggleClass("dialog-open")
				.toggleClass("dialog-closed");
	};
	
	$.fn.create = function(tag){
		return $(document.createElement(tag));
		
	};
	
	$.fn.scrollTo = function(element){
		var top = $(element).position().top;
		this.animate({scrollTop: top});

	};
	
	////////////////////////////
	//this method loads the children of the new contents
	//into the target after emptying the contents
	//with a fade in / fade out effect
	$.fn.replaceContents = function(/*the pane whose contents will be swapped in*/ newContents,  
									/*the layout for the target*/ layoutOptions){
		var target = this;
		$(target).fadeOut("fast", function(){
			$(target).empty().prepend($(newContents).children());
			$(target).fadeIn("fast");
			if(layoutOptions != null && layoutOptions != undefined){
				$(target).layout(layoutOptions);
			}
		});
		return $(target);
	};	
	/**
	 * openDialogClass optional - a css class to style the target while dialog is open
	 * */
	$.fn.openDialogOverTarget =  function(evt) {
		this.dialog('open');
		
		/*
		var offset = $(evt.target).offset();
		var coords = ['left','center'];
		this.dialog('option', 'position', coords)
		.dialog('open');
		*/
		
		/*
		$(evt.target).toggleOpenClosed();
		
		//listen when the dialog closes
		$(this).bind( "dialogclose", function(event, ui) {
			$(evt.target).toggleOpenClosed();
		});
		*/
	};
	
	
		

	$.fn.closeOnLostFocus = function() { // on the open event
	    // find the dialog element
	    var dialogEl = this;        
	    $(document).click(function (e) { // when anywhere in the doc is clicked
	        var clickedOutside = true; // start searching assuming we clicked outside
	        $(e.target).parents().andSelf().each(function () { // search parents and self
	            // if the original dialog selector is the click's target or a parent of the target
	            // we have not clicked outside the box
	            if ($(this).first().attr("id") == $(dialogEl).attr("id")) {
	                clickedOutside = false; // found
	                return false; // stop searching
	            }
	        });
	        if (clickedOutside) {
	            $(dialogEl).dialog("close"); // close the dialog
	            // unbind this listener, we're done with it
	            $(document).unbind('click',arguments.callee); 
	        }
	    });
	};
	
	
	///////////////////////////////////////////////////////////////////////
	////duracloud js utils
	///////////////////////////////////////////////////////////////////////
	if(dc == undefined){
		dc = {};
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
	

	
	dc.busy = function(message){
		var d = 
		   $("#busy-dialog");
		
		d.dialog({
				autoOpen: false,
				show: 'fade',
				hide: 'fade',
				resizable: false,
				height: 100,
				closeOnEscape:false,
				modal: false,
				width:300,
				close: function() {
					
				},
				
				open: function(e){
				},
			   });
		
		$(".ui-dialog-titlebar").hide();
		
		//$("#page-content").glasspane("show", message);
	   $("#busy-dialog-title").html(message);
	   d.dialog("open");
	};
	
	dc.done = function(){
		//$("#page-content").glasspane("hide");
		$("#busy-dialog").dialog("close");
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
		
		$.ajax({ url: url, 
			cache: false,
			method: "GET",
			context: document.body, 
			success: function(data){
				//ajax response gets set here
				updater(callback, data);
		    },
		    error: function(xhr, textStatus, errorThrown){
		    	alert("updater failed: " + textStatus);
		    },
		});		
	};
	
	
	dc.ajax = function(params,callback){
		var ajaxParameters = {
			url: params.url,
			success: function(data){
				callback.success(data);
			},
		    error: function(xhr, textStatus, errorThrown){
	    		_error("error: {url: " + params.url + ", xhr.status: " + status + "," + textStatus + ", error: " + errorThrown);
	    		callback.failure(textStatus);
		    },
		};
		
		if(params.type != undefined){
			ajaxParameters.type = params.type;
		}
		
		if(params.data != undefined){
			ajaxParameters.data = params.data;
		}
		
		if(callback.begin != undefined){
			callback.begin();
		}
			
		$.ajax(ajaxParameters);
	}


})();


/* jQuery ui.toaster.js - 0.2
*
* (c) Maxime Haineault <haineault@gmail.com>
* http://haineault.com 
* 
* MIT License (http://www.opensource.org/licenses/mit-license.php)
*
* Inspired by experimental ui.toaster.js by Miksago (miksago.wordpress.com)
* Thanks a lot.
*
* */

	$.widget('ui.toaster', {
		_init: function(){
			var self	= this;
			var wrapper = '#ui-toaster-'+ self.options.position;
	
			if (!$(wrapper).get(0)) {
				$('<div />').attr('id', 'ui-toaster-'+ self.options.position).appendTo('body');
			}
	
			self.toaster = $('<div style="display:none;" class="ui-toaster" />')
				.append($('<span class="ui-toaster-border-tr" /><span class="ui-toaster-border-tl" /><span class="ui-toaster-border-tc" />'))
				.append($('<span class="ui-toaster-body" />').html($('<div />').append($(self.element).html())))
				.append($('<span class="ui-toaster-border-br" /><span class="ui-toaster-border-bl" /><span class="ui-toaster-border-bc" />'))
				.width(self.options.width)
	            .hover(function(){ self.pause.apply(self)}, function(){ self.resume.apply(self)})
				[(self.options.position.match(/bl|br/)) ? 'prependTo': 'appendTo'](wrapper)
				;
	
			// Closable
			if (self.options.closable) {
				self.toaster.addClass('ui-toaster-closable');
				if ($(self.toaster).find('.ui-toaster-close').length > 0) {
					$('.ui-toaster-close', $(self.toaster)).click(function(){ self.hide.apply(self); });
				}
				else {
					$(self.toaster).click(function(){ self.hide.apply(self); });
				}
			}
	
			// Sticky
			if (self.options.sticky) {
				$(self.toaster).addClass('ui-toaster-sticky');
			}
			else {
				self.resume();
			}
			
			// Delay
			if (!!self.options.delay) {
			   setTimeout(function(){
					self.open.apply(self);
				}, self.options.delay * 1000);
			}
			else {
				self.open.apply(self);
			}
	   },
	
		open: function() {
			this.options.show.apply(this.toaster);
	   },
	
		hide: function(){
			if (this.options.onHide) this.options.onHide.apply(this.toaster);
			this.close(this.options.hide);
		},
	
		close: function(effect) {
			var self   = this;
			var effect = effect || self.options.close;
			if (self.options.onClose) {
				effect.apply(self.toaster);
			}
			effect.apply(self.toaster, [self.options.speed, function(){
				if (self.options.onClosed) self.options.onClosed.apply(self.toaster);
				$(self.toaster).remove();
	           }]);
	   },
	
		resume: function() {
			var self = this;
			self.timer = setTimeout(function(){
				self.close.apply(self);
			}, self.options.timeout * 1000 + self.options.delay * 1000);
		},
	
		pause: function() { clearTimeout(this.timer); },
		
		options: {
			delay:    0,      // delay before showing (seconds)
			timeout:  3,      // time before hiding (seconds)
			width:    200,    // toast width in pixel
			position: 'tl',   // tl, tr, bl, br
			speed:    'slow', // animations speed
			closable: true,   // allow user to close it
			sticky:   false,  // show until user close it
			onClose:  false,  // callback before closing
			onClosed: false,  // callback after closing
			onOpen:   false,  // callback before opening
			onOpened: false,  // callback after opening
			onHide:   false,  // callback when closed by user
			show:	  $.fn.fadeIn, // showing effect
			hide:	  $.fn.fadeOut,   // closing effect (by user)
			close:    $.fn.fadeOut    // hiding effect (timeout)
		}
	});


