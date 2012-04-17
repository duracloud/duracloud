/**
 * Duraserivce AJAX API
 * @author Daniel Bernstein
 */

var dc; 
$(function(){
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
		 dc.service.GetDeployedServices = function(callback,/*optional boolean */ async){
			 dc.ajax({
				 async: (async == undefined || async) ? true : false,
				 url: SERVICES_URL_BASE + "deployed",
			 }, callback);
		 };
		 
		/**
		 * 
		 */
		 dc.service.GetServiceDeploymentConfig = function(service, deployment, callback, /*optional boolean*/ async){
			 dc.ajax({
				 async: (async == undefined || async) ? true : false,
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

		var MEDIA_STREAMER_URL = APP_CONTEXT+"/services/mediastreamer";

		dc.service.UpdateSpaceStreaming = function(storeId, spaceId, /*bool*/enable){
		    var jqxhr = dc.ajax2({
		        url: MEDIA_STREAMER_URL +"?storeId="+storeId+"&spaceId="
		                        + encodeURIComponent(spaceId) 
		                        + "&enable=" + enable,
		        type: "post",
		        cache: false,
		    }).fail(function(){
                dc.displayErrorDialog(jqxhr);  
		    });
		    return jqxhr;
		};
		
		dc.service.GetStreamingStatus = function (storeId, spaceId){
		    var jqxhr =  dc.ajax2({
                url: MEDIA_STREAMER_URL + "?storeId="+storeId+"&spaceId="
                                + encodeURIComponent(spaceId), 
                type: "get",
                cache: false,
            }).fail(function(){
	            dc.displayErrorDialog(jqxhr);  
	        });
		    return jqxhr;
		};

	})();
});



