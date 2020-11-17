/**
 * Properties Panel: used for displaying lists of static properties
 * created by Daniel Bernstein
 */
$.widget("ui.propertiesviewer",
	$.extend({}, $.ui.expandopanel.prototype, 
		{  //extended definition 
	        
			_init: function(){ 
				$.ui.expandopanel.prototype._init.call(this); //call super init first
				var that = this;
				
				//initialize table
				var form = $("form", this.element);

				if(form.children().length == 0){
					form = $(document.createElement("form"));
					var table = $(document.createElement("table"));
					table.attr("role", "presentation");
					form.append(table);
					this.getContent().prepend(form);
					
					if(!this.options.readOnly){
    					var addControlsRow = this._createControlsRow();

    					var disableControls = function(){
    						$("input, button", that.element).attr("disabled", "disabled");
    						$(".dc-expando-status", addControlsRow).addClass("dc-busy");
    					};
    						
    					var enableControls = function(){
    						$("input,button", that.element).removeAttr("disabled");
    						$(".dc-expando-status", addControlsRow).removeClass("dc-busy");
    
    					};
    
    					var fSuccess = function(){
    						enableControls();
    						that._addSuccess(that)
    					};
    					
    					var triggerAdd = function(){
    						if(that._isValid() && form.valid()){
        					    disableControls();
        						that.element
        						    .trigger(
        						        "dc-add", 
        						        { 
            							  value: that._getValue(), 
            							  success: fSuccess,
            							  failure: function(text){
            								enableControls();
            								that._addFailure(text);
            							  },
        						        }
        						    );
                            }
    					};
    										  
    					//attach listeners
    					$("input[type=button]", addControlsRow).click(function(evt){
    						triggerAdd();
    					});
    					
    					$("input[type=text]", addControlsRow).bindEnterKey(triggerAdd);
    
    					table.append(addControlsRow);
					}
					
					this.getContent().prepend("<div id='empty-viewer-message'></div>");   

				}

				form.validate({
					rules : {
						name : {
							isusascii: true,
						},
						value : {
							isusascii: true,
						}
					},
					messages : {

					}
				});

            },
			
			_setEmptyMessage: function(message){
			  if(this.options.readOnly){
			      $("#empty-viewer-message", this.element).html(message);  
			  }
			},
			
			
			destroy: function(){ 
				//tabular destroy here
				$.ui.expandopanel.prototype.destroy.call(this); // call the original function 
			}, 

			options: $.extend({}, $.ui.expandopanel.prototype.options, {
                emptyViewerMessage: "No properties currently set.",
			    data: [
				           {name: "name 1", value: "value1"},
				           {name: "name 2", value: "value2"}
							],
			}),

			load: function(data){
				this.options.data = data;
				for(i in data){
					this._add(data[i]);
				};
				
				this._setEmptyMessage(data.length > 0 ? '' : this.options.emptyViewerMessage);
			},

			
			_addSuccess: function(context){
				var v = context._getValue();
				if($.isArray(v)){
					for(i in v){
						context._add(v[i]);
					}
				}else{
					context._add(v);
					
				}
				context._clearForm();
				$("input[type=text]", context.element).first().focus();
			},
			
			_addFailure: function(){
				alert("add operation failed!");	
			},

			_removeSuccess: function(context, data){
				$(".name",context.element).each(function(index,value){
					var valueElement = $(value).siblings(".value").first();
					var val = valueElement.text();
					if($(value).html() == data.name && val == data.value){
						context._animateRemove(
									$(value).parent(), 
									function(){$(value).parent().remove()});
					}
					
				});	
			},
			
			_animateRemove: function(element, removeComplete){
				$(element).hide("slow", function(){
					removeComplete();
				});
				
			},
			
			_removeFailure: function(){
				alert("remove failed!");	
			},
			
			_createControlsRow: function(){
				var controls = $(document.createElement("tr"));
				controls.append(
					$(document.createElement("td"))
						.addClass("name")
						.html("<div><label for='name'>Name </label><input id='name' name='name' type='text' placeholder='[name]' class='name-txt' size='20'/></div>")
				);

				controls.append(
						$(document.createElement("td"))
							.addClass("value")
							.html("<div><label for='value'>Value </label><input id='value' name='value' type='text' placeholder='[value]' class='value-txt' size='20'/><input type='button' value='Add'/><div class='dc-expando-status'></div></div>")
					);
				
				return controls;
				
			},

			_getValue: function(){
			    var that = this;
				return { 
						name: that._getNameFieldValue(),
						value: $(".value-txt",this.element).val(),
				};
			},
			
			_getNameFieldValue: function(){
			    return $(".name-txt",this.element).first().val();
			},
			
	         _isValid: function(){
	             var v = this._getNameFieldValue();
	             return v != null && v.trim() != '';
            },


			_getDataContainer: function(){
				return $("table", this.element);
			},

			
			_createDataChild: function(data){
				var child = $(document.createElement("tr"));
				//add the name element
				child.append($(document.createElement("td"))
				                .addClass("name")
				                .html(data.name));
				//add the value value
				var valueCell = $(document.createElement("td"));
				child.append(valueCell.addClass("value").html(data.value));
				
				if(!this.options.readOnly){
    				//append remove button
    				button = $(document.createElement("span"))
    				            .addClass("dc-mouse-panel float-r")
    				            .makeHidden()
    				            .append("<input type='button' value='x'/>");
    				valueCell.append(button);
                }

				return child;
			},
			
			_appendChild: function (child){
			    var children = this._getDataContainer().children();
			    if(children.size() > 0){
	                children.last().prepend(child);
			    }else{
			        this._getDataContainer().append(child);
			    }
				return child;
			},
			
			_add: function(data){
				var that = this;
				var child = this._createDataChild(data);
				child.addClass("dc-mouse-panel-activator");
				this._appendChild(child);
				//add click listener 
				$("input", child).click(function(evt){
					var props = "";
					for(p in data){
						props += p + ": " + data[p] + ", ";
					}

					var value = data;
					
					child.addClass("dc-removing");
					that.element.trigger("dc-remove", { value: value, 
						  success: function(){
							child.removeClass("dc-removing");
							that._removeSuccess(that,data);
						  },
						  failure: function(){
							  child.removeClass("dc-removing");
							  that._removeFailure();
						  }
					});
				});
			},
			
			
			_clearForm: function(){
				$("input[type='text']", this.element).val('');
			},
			
			destroy: function(){ 
			}, 
			
		}
	)
); 

