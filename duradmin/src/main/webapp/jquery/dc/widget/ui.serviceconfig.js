/**
 * Service Widgets
 */

;(function(){
	$.widget('ui.serviceconfig', { 
		/*
		 * service info member variable
		 */
		_service: null,
		
		/*
		 * 
		 */
		_controlContainer: null,
		
		/**
		 * built in widget initialization method
		 */
		_init: function(){
			this.element.html("");
			this._controlContainer =  $.fn.create("fieldset");
			this.element.append(
				$.fn.create("span").addClass("dc-message")
			).append(
				$.fn.create("form").append(
					$.fn.create("div").addClass("form-fields").append(
							this._controlContainer
					)
				)
			);
		},

		
		_destroy: function(){
			$(".dc-message", this.element).html("");
			
		},
		options: {},
		
		/*
		 * returns a form control appropriate to the user config object.
		 * 
		 */
		_createControl: function(/* userConfig object */uc, /*string*/namespacePrefix){
			var inputType = uc.inputType;
			var i;
			
			
			var controlId = namespacePrefix + "." + uc.name;
			
			if(inputType == "TEXT"){
				return $.fn.create("input")
						   .attr("type", "text")
						   .attr("name", controlId)
						   .attr("id",controlId)
						   .val(uc.value != undefined && uc.value != null ? uc.value : '');
			}else if(inputType == "SINGLESELECT"){
				var select =  $.fn.create("select")
								  .attr("name", controlId)
								  .attr("id", controlId);
				
				for(i = 0; i < uc.options.length; i++){
					var o = uc.options[i];
					var option = $.fn.create("option")
									 .attr("value", o.value)
									 .html(o.displayName);
					if(o.selected){
						option.attr("selected", "true");
					}
					select.append(option);
				}
				
				return select;

			}else if(inputType == "MULTISELECT"){
				var select =  $.fn.create("ul");
				for(i = 0; i < uc.options.length; i++){
					var o = uc.options[i];
					var li = $.fn.create("li");
					var id = o.id + "-" + i;
					var option = $.fn.create("input")
									.attr("name", controlId+"-"+checkbox+"-"+i)
									.attr("type","checkbox")
									.attr("value", o.value);
					if(o.selected){
						option.attr("checked", "true");
					}
					
					li.append(option).append("label")
										.attr("for", id)
										.html(o.displayName);
					select.append(li);
				}
				
				return select;
				
			}else{
				throw Error("input type [" + inputType + "] not recognized");
			}
		},
		
		_createListItem: function(fieldId, displayName){
			var li = $.fn.create("li").attr("id", "li-"+fieldId).addClass("row clearfix");
			
			li.append(
					$.fn.create("label").attr("for", fieldId).html(displayName)
			);
			
			return li;
		},

		data: function(){
			var result =  { service: this._service};
			if(this._deployment != undefined){
				result.deployment = this._deployment;
			}
			return result;
		},
		
		/**
		 * Returns serialized string of currently visible form values
		 */
		serializedFormValues: function(){
			return $(this.element).find("form").serialize();
		},
		
		_addUserConfig: function(userConfig, controlList, namespacePrefix){
			var control = this._createControl(userConfig,namespacePrefix);
			var item = this._createListItem(userConfig.name, userConfig.displayName);
			item.append(control);
			controlList.append(item);
		},
		
		_addMode: function(mode, controlList,namespacePrefix){
			var prefix = namespacePrefix +"."+mode.name;
			if(mode.userConfigs != undefined){
				for(j = 0; j < mode.userConfigs.length; j++){
					this._addUserConfig(mode.userConfigs[j], controlList,prefix);
				}
			}
			
			if(mode.userConfigModeSets != undefined){
				for(j = 0; j < mode.userConfigModeSets.length; j++){
					this._addModeSet(mode.userConfigModeSets[j], controlList,prefix);
				}
			}
		},

		_addModeSet: function(modeSet, controlList, /*optional - top level namespaces have no prefix*/ namespacePrefix){
			var modes = modeSet.modes;
			var prefix = (namespacePrefix == null || namespacePrefix == undefined)? 
							modeSet.name: namespacePrefix +"."+modeSet.name;
			
			//if only a single mode, just add to the control list
			if(modes.length == 1){
				this._addMode(modes[0], controlList, prefix);
				return;
			}

			var i,j, mode,modeSetId,modeSetSelect;
			modeSetId = controlList.attr("id") + "-modeset-" + modeSet.id + "-"+ modeSet.name;
			modeSetSelect = $.fn.create("select").attr("id", modeSetId).attr("name", prefix);

			//for multiple modes
			//first create a mode selection box
			for(i = 0; i < modes.length; i++){
				mode = modeSet.modes[i];
				modeSetSelect.append($.fn.create("option")
										 .attr("value",mode.name)
										 .attr("selected", mode.selected ? "selected" : "")
										 .html(mode.displayName));
			}

			//create a list item that will contain the mode panels (ie html uls)
			var modeSetItem = this._createListItem(modeSetId, modeSet.displayName);
			var modeSetItemId = modeSetItem.attr("id");
			modeSetItem.addClass("dc-exclusion-group");
			modeSetItem.append(modeSetSelect);
			controlList.append(modeSetItem);
			
			//build and add mode panels
			for(i = 0; i < modes.length; i++){
				mode = modes[i];
				// for each mode, create a list and add all the children to it.
				var modeDiv, modeList, modeListId;
				modeListId = modeSetId+"-"+mode.name;					
				modeList = $.fn.create("ul")
							   .attr("id", modeListId);
							   
				modeDiv = $.fn.create("div").attr("id", "div-"+modeListId);
				modeDiv.append(modeList);
				modeSetItem.append(modeDiv);
				this._addMode(mode,modeList,prefix);
			}
			
			// attach a change listener to the modeSet select control
			// hide all but the selected list.
			
			var toggleVisibleMode = function(){
				$("#"+ modeSetItemId +" > div").hide();
				var modeName = modeSetSelect.val();
				$("#"+ modeSetItemId +" > div#div-" + modeSetId + "-" + modeName).show();
			};
			
			modeSetSelect.change(function(evt){
				toggleVisibleMode();
			});

			toggleVisibleMode();
		},
		
		/**
		 * loads the widget
		 */
		load: function(service, /*optional*/deployment){
			var that = this;
			
			var i;
			
			if(service == undefined){
				return this._service;
			}
			
			this._service = service;

			dc.debug("loading service: " + service.id);
			var modeSets = service.userConfigModeSets;
			if(deployment != undefined){
				this._deployment = deployment;
				dc.debug("loading deployment: " + deployment.id);
				modeSets = deployment.userConfigModeSets;
			}
			
			this._controlContainer.html("");
			this._controlContainer.append(
				$.fn.create("input").attr("type", "hidden")
									.attr("id", "serviceid-" + service.id)
									.attr("name", "serviceId")
									.val(service.id)
			);

			var list = $.fn.create("ul").addClass("dc-user-config");
			this._controlContainer.append(list);

			if(deployment != undefined){
				this._controlContainer.append(
						$.fn.create("input").attr("type", "hidden")
						.attr("id", "deploymentid-" + deployment.id)
						.attr("name", "deploymentId")
						.val(deployment.id)
				);
			}else{
				var dOptions = service.deploymentOptions;
				var locationSelect = $.fn.create("select").attr("name","deploymentOption");
				for(i = 0; i < dOptions.length;i++){
					var o = dOptions[i];
					locationSelect.append($.fn.create("option")
												.attr("value", o.hostname + "-" + 
																	o.location[0]).html(
																			o.displayName + " - " + o.hostname + " (" + o.location+")"));
				}

                if(dOptions.length < 2)
				    locationSelect.attr("disabled", true);

				list.append(
					this._createListItem("location", "Location").append(locationSelect)
				);
			}
			
			for(i = 0; i < modeSets.length;i++){
				this._addModeSet(modeSets[i],list);
			}
		},
	});	
})();

