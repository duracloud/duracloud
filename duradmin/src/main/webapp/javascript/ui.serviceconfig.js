/**
 * Service Widgets
 */

;(function(){
	$.widget('ui.serviceconfig', { 
		_service: null,
		_controlContainer: null,
		_init: function(){
			this.element.html("");
			this._controlContainer =  $.fn.create("fieldset");
			this.element.append(
				$.fn.create("span").addClass("dc-message")
			).append(
				$.fn.create("form").append(
					$.fn.create("div").addClass("form-fields h400").append(
							this._controlContainer
					)
				)
			);
		},

		
		_destroy: function(){
			$(".dc-message", this.element).html("");
			
		},
		options: {},
		_createControl: function(/*userConfig object*/uc){
			var inputType = uc.inputType;
			if(inputType == "TEXT"){
				return $.fn.create("input").attr("type", "text").attr("name", uc.name).attr("id",uc.name).val(uc.value != undefined && uc.value != null ? uc.value : '');
			}else if(inputType == "SINGLESELECT"){
				var select =  $.fn.create("select").attr("name", uc.name).attr("id",uc.name);
				for(i in uc.options){
					var o = uc.options[i];
					var option = $.fn.create("option").attr("value", o.value).html(o.displayName);
					if(o.selected){
						option.attr("selected", "true");
					}
					select.append(option);
				}
				
				return select;

			}else if(inputType == "MULTISELECT"){
				var select =  $.fn.create("ul");
				for(i in uc.options){
					var o = uc.options[i];
					var li = $.fn.create("li");
					var id = o.id + "-" + i;
					var option = $.fn.create("input")
									.attr("name", uc.name)
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
		
		load: function(service, deployment){
			var that = this;
			
			if(service == undefined){
				return this._service;
			}
			
			this._service = service;

			//console.debug("loading service: " + service.id);
			var userConfigs = service.userConfigs;

			if(deployment != undefined){
				this._deployment = deployment;
				//console.debug("loading deployment: " + deployment.id);
				userConfigs = deployment.userConfigs;
			}
			
			this._controlContainer.html("");
			this._controlContainer.append(
				$.fn.create("input").attr("type", "hidden")
									.attr("id", "serviceid-" + service.id)
									.attr("name", "serviceId")
									.val(service.id)
			);

			var list = $.fn.create("ul");
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
				for(i in dOptions){
					var o = dOptions[i];
					locationSelect.append($.fn.create("option")
												.attr("value", o.hostname + "-" + 
																	o.locationType[0]).html(
																			o.displayName + " - " + o.hostname + " (" + o.locationType+")"));
				}
				
				list.append(
					this._createListItem("location", "Location").append(locationSelect)
				);
			}

			
			
			
			if(userConfigs != undefined && userConfigs != null && userConfigs.length > 0){
				var item,control,uc;
				for(i in userConfigs){
					uc = userConfigs[i];
					control = this._createControl(uc);
					//attach listener if an exclusion definition
					if(uc.exclusion == "EXCLUSION_DEFINITION"){
						control.change(function(){
							that._exclusionGroupChanged(userConfigs);
						});
					}
					
					item = this._createListItem(uc.name, uc.displayName);
					item.append(control)
					list.append(item);
				}	

				that._exclusionGroupChanged(userConfigs);

			}
			
			
		},
		
		_exclusionGroupChanged: function(userConfigs){
			//first extract exclusions and defs
			var exDefs = new Array();
			var exclusions = new Array();
			var uc,ex;
			
			for( i in userConfigs){
				uc = userConfigs[i];
				ex = uc.exclusion;
				if(ex == "EXCLUSION_DEFINITION"){
					dc.debug("adding exclusion definition:" + uc.name + "; "+ex);
					exDefs.push(uc);
				}else if(ex != undefined){
					dc.debug("adding exclusion:" + uc.name + "; " + ex);
					exclusions.push(uc);
				}
			}

			var selectedExclusions;
			
			for(i in exDefs){
				selectedExclusions = new Array();
				uc = exDefs[i];
				var control = $("[name="+uc.name+"]");
				if(uc.inputType == "SINGLESELECT"){
					selectedExclusions.push(control.val());
				}else if(uc.inputType == "MULTISELECT"){
					$("[name="+uc.name+"]:checked").each(function(ii, selected){ 
						selectedExclusions[ii] = $(selected).attr("name");
					});
				}else{
					//ignore
				}
				
				var exlist;
				for(j in exclusions){
					uc = exclusions[j];
					exlist = uc.exclusion.split("|");
					var visible = false;
					for(x in exlist){
						dc.debug("ex["+x+"]:" + exlist[x]);
						for(y in selectedExclusions){
							if(exlist[x] == selectedExclusions[y]){
								visible = true;
							}
						}
					}
					$("#li-"+uc.name,this.element).css("display", visible ? "block":"none");
				}
			}
		}
		
	});	
})();

