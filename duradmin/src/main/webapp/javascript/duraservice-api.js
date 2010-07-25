/**
 * Duraserivce AJAX API
 * @author Daniel Bernstein
 */
var dc; 

(function(){
	if(dc == undefined){
		dc ={};
	}
	
	dc.service = {};
	
	var APP_CONTEXT = "/duradmin";
	var SERVICES_URL_BASE = APP_CONTEXT+"/services?f=json&method=";
	var SERVICE_URL_BASE = APP_CONTEXT+"/services/service?";

	/**
	 * Returns a list of available services.
	 */
	 dc.service.GetAvailableServices = function(callback){
		 dc.ajax({
			 url: SERVICES_URL_BASE + "available",
		 }, callback);
	 };

	/**
	 * Returns a list of deployed services.
	 */
	 dc.service.GetDeployedServices = function(callback){
		 dc.ajax({
			 url: SERVICES_URL_BASE + "deployed",
		 }, callback);
	 };
	 
	/**
	 * 
	 */
	 dc.service.GetServiceDeploymentConfig = function(service, deployment, callback){
		 dc.ajax({
			 url: SERVICE_URL_BASE + _formatServiceParams("getproperties", service,deployment),
		 }, callback);
	 };

	 var _formatServiceParams = function(method, service, deployment){
		return "method="+ method + "&serviceId="+service.id + "&deploymentId="+deployment.id 
	 }

	 /**
	  * 
	  */
	dc.service.Undeploy = function(service, deployment, callback){
		dc.ajax({
			url: SERVICE_URL_BASE + _formatServiceParams("undeploy", service,deployment),
		}, callback);
	};

})();

/**
 * Service Widgets
 */
(function(){
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
				return $.fn.create("input").attr("type", "text").attr("name", uc.name).val(uc.value != undefined && uc.value != null ? uc.value : '');
			}else if(inputType == "SINGLESELECT"){
				var select =  $.fn.create("select").attr("name", uc.name);
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
									.attr("id", id)
									.attr("type","checkbox")
									.attr("name", uc.name)
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
			var li = $.fn.create("li").addClass("row clearfix");
			
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
				for(i in userConfigs){
					var uc = userConfigs[i];
					
					list.append(
							this._createListItem(uc.id, uc.displayName)
								.append(this._createControl(uc))
					);
				}	
			}
		
		},
		reconfigure: function(service,deployment){
			this.element.html("loading service: " + service.displayName + ", deployment: " + deployment.id);
		},

	});
})();
