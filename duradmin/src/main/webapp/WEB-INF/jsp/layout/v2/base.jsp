<!DOCTYPE html>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page session="false"%>
<%@include file="/WEB-INF/jsp/include.jsp" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<!-- 
	created by Daniel Bernstein and CH
 -->
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta name="language" content="en" />
    <title><spring:message code="application.title" /> :: <tiles:insertAttribute name="title"/></title>

	<link rel="stylesheet"  href="${pageContext.request.contextPath}/style/base.css" type="text/css" />	
	<link rel="stylesheet"  href="${pageContext.request.contextPath}/style/flex.css" type="text/css" />
	<link rel="stylesheet"  href="${pageContext.request.contextPath}/style/dialogs.css" type="text/css" />
	<link rel="stylesheet"  href="${pageContext.request.contextPath}/style/buttons.css" type="text/css" />

	<!-- jquery core, ui and css -->
	<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.js"></script>
	<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.1/jquery-ui.js"></script>
	<!-- 3rd party jquery plugins start-->
	<script type="text/javascript" src="http://layout.jquery-dev.net/download/jquery.layout.min-1.2.0.js"></script>
	<script type="text/javascript"
		src="${pageContext.request.contextPath}/jquery/plugins/jquery.ba-throttle-debounce/jquery.ba-throttle-debounce.min.js"></script>

	<script type="text/javascript" src="http://ajax.microsoft.com/ajax/jquery.validate/1.7/jquery.validate.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/jquery/plugins/jquery.form/jquery.form-2.4.3.js"></script>

	<!-- 3rd party jquery plugins end-->
	<script type="text/javascript" src="${pageContext.request.contextPath}/javascript/jquery.dc.common.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/javascript/ui.glasspane.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/javascript/ui.onoffswitch.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/javascript/ui.selectablelist.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/javascript/ui.listdetailviewer.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/javascript/ui.expandopanel.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/javascript/durastore-api.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/javascript/duraservice-api.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/javascript/main.js"></script>
	<script type="text/javascript">
		/*REMOVE THIS $.require method when we upgrade jquery to 1.4.3+*/
		/**
		* require is used for on demand loading of JavaScript
		*
		* require r1 // 2008.02.05 // jQuery 1.2.2
		*
		* // basic usage (just like .accordion)
		* $.require("comp1.js");
		*
	
		* @param  jsFiles string array or string holding the js file names to load
		* @param  params object holding parameter like browserType, callback, cache
		* @return The jQuery object
		* @author Manish Shanker
		*/
	
		(function($){
		$.require = function(jsFiles, params) {
	
		var params = params || {};
		var bType = params.browserType===false?false:true;
	
		if (!bType){
		return $;
		}
	
		var cBack = params.callBack || function(){};
		var eCache = params.cache===false?false:true;
	
		if (!$.require.loadedLib) $.require.loadedLib = {};
	
		if ( !$.scriptPath ) {
		var path = $('script').attr('src');
		$.scriptPath = path.replace(/\w+\.js$/, '');
		}
		if (typeof jsFiles === "string") {
		jsFiles = new Array(jsFiles);
		}
		for (var n=0; n< jsFiles.length; n++) {
		if (!$.require.loadedLib[jsFiles[n]]) {
		$.ajax({
		type: "GET",
		url: $.scriptPath + jsFiles[n],
		success: cBack,
		dataType: "script",
		cache: eCache,
		async: false
		});
		$.require.loadedLib[jsFiles[n]] = true;
		}
		}
		//console.dir($.require.loadedLib);
	
		return $;
		};
		})(jQuery);
	
	$.require('jquery.validate.min.js');
	$.require('jquery.form-2.4.3.js');
	$.require('jquery.validate.min.js');
	$.require('jquery.ba-throttle-debounce.min.js');
	$.require('jquery.layout.min-1.2.0.js');
	$.require('jquery.dc.common.js');
	$.require('ui.onoffswitch.js');
	$.require('ui.selectablelist.js');
	$.require('ui.listdetailviewer.js');
	$.require('ui.expandopanel.js');
	$.require('durastore-api.js');
	$.require('duraservice-api.js');
	$.require('main.js');
	$(document).ready(function() {

		///////////////////////////////////////////////////////////////////////
		////load partner logo
		///////////////////////////////////////////////////////////////////////
		
		dc.store.CheckIfContentItemExists(
				{spaceId: "x-duracloud-admin", contentId: "logo", storeId:"0"}, 
				{ 
					success: function(exists){
						if(exists){
							$("#dc-partner-logo").attr('src',"/duradmin/download/contentItem?spaceId=x-duracloud-admin&contentId=logo&storeID=0").load(function() {
							});
						}
					},
					failure: function(text){
						//console.error("failed to check if content item exists:" +  text);
					}
				});
		

		///////////////////////////////////////////////////////////////////////
		////controls rollovers on tags and metadata
		///////////////////////////////////////////////////////////////////////
		
		$(".dc-mouse-panel-activator td, li.dc-mouse-panel-activator, .dc-mouse-panel").live("mouseover",function(evt){
			var ancestor = $(evt.target).nearestOfClass(".dc-mouse-panel-activator");
			$(".dc-mouse-panel",ancestor).css("visibility","visible");
		}).live("mouseout",function(evt){
			var ancestor = $(evt.target).nearestOfClass(".dc-mouse-panel-activator");
			$(".dc-mouse-panel",ancestor).css("visibility","hidden");
		});
		
		$(".dc-mouse-panel").css("visibility", "hidden");
		
		///////////////////////////////////////////////////////////////////////
		////Layout Page Frame
		///////////////////////////////////////////////////////////////////////
		var pageHeaderLayout 

		 $("body").layout({
				north__size:	    87
			,   north__paneSelector:"#page-header"
			,   resizable:   false
			,   slidable:    false
			,   spacing_open:			0			
			,	togglerLength_open:		0			
			,	togglerLength_closed:	-1
			,	useStateCookie:		true
			,   center__paneSelector: "#page-content"
			,	center__onresize:	"centerLayout.resizeAll"
		});
	});	
	</script>
	<!-- page level header extensions reserved for pages that wish to inject page specific scripts into the header -->
	<tiles:insertAttribute name="header-extensions" ignore="true"/>
			
</head>
<body>
	<tiles:insertAttribute name="body"/>
</body>
</html>
