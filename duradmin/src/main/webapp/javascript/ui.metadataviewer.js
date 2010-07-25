/**
 * Metadata Panel: used for displaying lists of static properties
 * created by Daniel Bernstein
 */
$.widget("ui.metadataviewer", 
	$.extend({}, $.ui.expandopanel.prototype, 
		{  //extended definition 
			_init: function(){ 
				$.ui.expandopanel.prototype._init.call(this); //call super init first
				var that = this;
				
				//initialize table
				var table =  $("table", this.element);
				if(table.size() == 0){
					table = $(document.createElement("table"));
					this.getContent().prepend(table);	
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
						disableControls();
						that.element.trigger("add", { 
											  value: that._getValue(), 
											  success: fSuccess,
											  failure: function(text){
												enableControls();
												that._addFailure(text);
											  },});
					};
										  
					//attach listeners
					$("input[type=button]", addControlsRow).click(function(evt){
						triggerAdd();
					});
					
					$("input[type=text]", addControlsRow).keyup(function(e) {
							//enter key listener
							if(e.keyCode == 13) {
								triggerAdd();
							}
						});

					table.append(addControlsRow);
				}
				
				this._initializeDataContainer();
			}, 
			
			
			destroy: function(){ 
				//tabular destroy here
				$.ui.expandopanel.prototype.destroy.call(this); // call the original function 
			}, 

			options: $.extend({}, $.ui.expandopanel.prototype.options, {
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
			},
			
			_initializeDataContainer: function(){
				
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
					if($(value).html() == data.name){
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
						.html("<div><label>Name</label></div><div><input type='text' class='name-txt' size='15'/></div>")
				);

				controls.append(
						$(document.createElement("td"))
							.addClass("value")
							.html("<div><label>Value</label></div><div><input type='text' class='value-txt' size='20'/><input type='button' value='+'/><div class='dc-expando-status'></div></div>")
					);
				
				return controls;
				
			},

			_getValue: function(){
				var fields = { 
						name: $(".name-txt",this.element).first().val(),
						value: $(".value-txt",this.element).val(),
				};
				
				return fields;
			},

			_getDataContainer: function(){
				return $("table", this.element);
			},

			
			_createDataChild: function(data){
				var child = $(document.createElement("tr"));
				//add the name element
				child.append($(document.createElement("td")).addClass("name").html(data.name));
				//add the value value
				var valueCell = $(document.createElement("td"));
				child.append(valueCell.addClass("value").html(data.value));
				//append remove button
				button = $(document.createElement("span")).addClass("dc-mouse-panel float-r").makeHidden().append("<input type='button' value='x'/>");
				valueCell.append(button);
				return child;
			},
			
			_appendChild: function (child){
				this._getDataContainer().children().last().prepend(child);
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

					var value = that._getValue();
					
					child.addClass("dc-removing");
					that.element.trigger("remove", { value: value, 
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

