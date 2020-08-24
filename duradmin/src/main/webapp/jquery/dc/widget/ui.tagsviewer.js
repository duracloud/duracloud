/**
 * Tags panel is substantially the same in display and behavior as propertiesviewer
 * created by Daniel Bernstein
 */
$.widget("ui.tagsviewer", 
		$.extend({}, $.ui.propertiesviewer.prototype,
			{  //extended definition 
		        
	            options: $.extend({}, $.ui.propertiesviewer.prototype.options, {
	                emptyViewerMessage: "No tags currently set.",
	            }),
	            
				_init: function(){ 
					$.ui.propertiesviewer.prototype._init.call(this); //call super init first
					this._initializeDataContainer();
				}, 
				
				_initializeDataContainer: function(){
					$("table",this.element).prepend("<tr><td><ul class='horizontal-list'></ul></td></tr>");
				},
				
				destroy: function(){ 
					$.ui.propertiesviewer.prototype.destroy.call(this); // call the original function
				}, 

				_createControlsRow: function(){
					var controls = $(document.createElement("tr"));

					controls.append(
							$(document.createElement("td"))
								.addClass("value")
								.html("<label for='tag'>Enter Tag </label> <input id='tag' name='value' type='text' class='name-txt' placeholder='tag here'	 size='35'/><input type='button' value='Add'/><div class='dc-expando-status'></div>")
						);
					
					return controls;
					
				},

				_getValue: function(){
					var tag =  $(".name-txt",this.element).first().val();
					var fields = new Array();
					fields.push(tag);
					return fields;
				},

				_getDataContainer: function(){
					return $("ul", this.element);
				},

				
				_createDataChild: function(data){
					var child = $.fn.create("li");
					//add the name element
					child.html(data);
					//append remove button
					if(!this.options.readOnly){
    					button = $.fn.create("span")
    					             .addClass("dc-mouse-panel float-r")
    					             .makeHidden()
    					             .append("<input type='button' value='x'/>");
    					child.append(button);
                    }

					return child;
				},
				
				
				_appendChild: function (child){
					this._getDataContainer().append(child);
					return child;
				},
				
				_removeSuccess: function(context, data){
					$("li",context.element).each(function(index,value){
						var text = $(value).text();
						if(text == data){
							context._animateRemove($(value), function(){$(value).remove()});
						}
						
					});	
				},
			}
		)
	);
