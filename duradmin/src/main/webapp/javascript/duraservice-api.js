/**
 * Duraserivce AJAX API
 * @author Daniel Bernstein
 */

var dc; 
$(document).ready(function(){
	
	$.require("jquery.dc.commons.js");
	
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
});



