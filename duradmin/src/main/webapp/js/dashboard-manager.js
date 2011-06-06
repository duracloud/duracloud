/**
 * Dashboard Manager
 * 
 * @author Daniel Bernstein
 */

(function(){
	$.widget("ui.graphpanel", {  
		options: {
			title: "Placeholder Title",
			data: null,
    	    
		},
		
		_init: function(){ 
			$(this.element).append($.fn.create("h3").html(this.options.title));
			
			var graph = $.fn.create("div").addClass("dc-graph");
			$(this.element).append(graph);

			 $.plot(graph, this.options.data,
				 {
			         series: {
			             pie: { 
			                 show: true
			             }
			         },
			         grid: {
			             hoverable: true,
			             clickable: true
			         }
				 }
			 );
			 
		}, 
	});
})();

(function(){
	$.widget("ui.breadcrumb", {  
		options: {
			rootText: "root",
			rootClick: function(){ alert("default click!")},
			titleClass: "dc-breadcrumb-title",
		},

		_crumb: new Array(),
		_init: function(){ 
			$(this.element).append($.fn.create("div").append(
				$.fn.create("ul").addClass("horizontal-list")));
			$(this.element)
				.append(
					$.fn.create("div")
						.addClass(this.options.titleClass)
						.html(this.options.rootText));
			this._crumb.push({text:this.options.rootText, click: this.options.rootClick});
		}, 
		_title: function (title){
			var titleElement = this.element.find("."+this.options.titleClass);
			if(!title){
				return titleElement.html();
			}else{
				titleElement.html(title);
			}
		},
		
		add: function(text, click){
			var that = this;
			//get last element in crumb
			var lastElement = this._crumb[this._crumb.length-1];

			//add the new element to internal list
			var newElement = {text:text, click: click};
			this._crumb.push(newElement);
			
			//add last element to the clickable list items
			var item = $.fn.create("li");
			item.html(lastElement.text);
			//wrap clickable
			item.click(function(){
				$.each(that._crumb,function(i,value){
					//remove all elements after matching value
					if(item.text() == value.text){
						that._crumb.splice(i+1,that._crumb.length);
						//set the text of the clicked value 
						//in the title field.
						that._title(value.text);
						//remove list elements after 
						item.nextAll().remove();
						item.remove();
						return false;
					}
				});
				
				if(lastElement.click){
					lastElement.click();
				}
			});
			//and append to clickable list
			$("ul",this.element).append(
					item);
			
			//set the current item to the new element text
			this._title(newElement.text);
		},
	});
})();

var centerLayout,mainContentLayout;

$(function() {
	centerLayout = $('#page-content').layout({
	// minWidth: 300 // ALL panes
		north__size: 			50	
	,	north__paneSelector:     ".center-north"
	,   north__resizable:   false
	,   north__slidable:    false
	,   north__spacing_open:			0			
	,	north__togglerLength_open:		0			
	,	north__togglerLength_closed:	0			

	,   west__size:				400
	,	west__minSize:			400
	,   west__resizable:   true
	,   west__slidable:    true

	,   west__paneSelector:     "#dynamic-panel"
	,	center__paneSelector:	"#main-content-panel"
	});
	
	mainContentLayout = $('#main-content-panel').layout({
		// minWidth: 300 // ALL panes
			north__size: 			100	
		,	north__paneSelector:     ".north"
		,   north__resizable:   false
		,   north__slidable:    false
		,   north__spacing_open:			0			
		,	north__togglerLength_open:		0			
		,	north__togglerLength_closed:	0			
		,	center__paneSelector:	".center"
		});
	
	var users = $("#current-users").expandopanel({title: "Current Users"});
	$(users).expandopanel("getContent").append("<h3>Coming soon.</h3>");

	var announcements = $("#announcements").expandopanel({title: "Announcements"});
	/*
	   $.getFeed({
	       url: '/duradmin/feed?url=http://twitter.com/statuses/user_timeline/38821410.rss',
	       success: function(feed) {
			 var content = $(announcements).expandopanel("getContent");
			 content.append("<h3>"+ feed.title + "</h3>");
	       }
	   });
	*/
	 var content = $(announcements).expandopanel("getContent");
	 content.append("<h3>Coming soon.</h3>");

	 /**
	  * converts an array of arbitray objects into something
	  * flot pie chart can read.
	  */
	 var formatPieChartData = function(inputArray, label, value){
		 var data = new Array();
		 var i;
		 for(i in inputArray){
			 data.push({
				 label: inputArray[i][label],
				 data: inputArray[i][value],
			 });
		 };
		 
		 return data;
	 };
	 
	 var loadSpaceReport = function (spaceMetrics){
		 var sp = replaceReportPaneWithCopy("#space");
		 var mtm = spaceMetrics.mimetypeMetrics;

		 
		 $("#mimetype-bytes",sp).graphpanel({
			 title: "Mimetype Bytes",
			 data:  formatPieChartData(
					 	mtm, 
					 	"mimetype", 
					 	"totalSize"),

		 });

		 $("#mimetype-files",sp).graphpanel({
			 title: "Mimetype Files",
			 data:  formatPieChartData(
					 	mtm, 
					 	"mimetype", 
					 	"totalItems"),
		 });

	 };
	 
	 var loadStorageProviderReport = function (storageProviderMetrics){
		 var sp = replaceReportPaneWithCopy("#storage-provider");
		 
		 var sm = storageProviderMetrics.spaceMetrics;
		 var mtm = storageProviderMetrics.mimetypeMetrics;
		 $("#bytes", sp).graphpanel({
			 title: "Bytes",
			 data:  formatPieChartData(
					 	sm, 
					 	"spaceName", 
					 	"totalSize"),
		 });

		 $("#files", sp).graphpanel({
			 title: "Files",
			 data:  formatPieChartData(
					 	sm, 
					 	"spaceName", 
					 	"totalItems"),
		 });
		 
		 
		 $("#bytes .dc-graph, #files .dc-graph", sp).bind("plotclick",function (event, pos, item){
			 handlePlotClick(item.series.label, function(){
				 var spm = getSpaceMetrics(item.series.label,sm);
				 loadSpaceReport(spm);
			 });
			 
		 });
		 
		 $("#mimetype-bytes",sp).graphpanel({
			 title: "Mimetype Bytes",
			 data:  formatPieChartData(
					 	mtm, 
					 	"mimetype", 
					 	"totalSize"),

		 });

		 $("#mimetype-files",sp).graphpanel({
			 title: "Mimetype Files",
			 data:  formatPieChartData(
					 	mtm, 
					 	"mimetype", 
					 	"totalItems"),
		 });

	 };
	 
	 var replaceReportPaneWithCopy = function(nodeId){
		 var newNode =  $(nodeId).clone()
			.removeClass("dc-hidden")
			.removeAttr("id");
		$("#report-pane").empty();
		$("#report-pane").append(newNode);
		return newNode;
	 };

	 var getSpaceMetrics = function(spaceName,spaceMetrics){
		 var spm = null;
		 $.each(spaceMetrics, function(i,sm){
			 if(sm.spaceName == spaceName){
				 spm =  sm;
				 return false;
			 }
		 });
		 
		 return spm;
	 };

	 var getStorageProviderMetrics = function(label,storageReport){
		 var spm = null;
		 $.each(storageReport.storageMetrics.storageProviderMetrics, function(i,sm){
			 if(sm.storageProviderType == label){
				 spm =  sm;
				 return false;
			 }
		 });
		 
		 return spm;
	 };
	 
	 var handlePlotClick = function(label,click){
		 bc.breadcrumb("add", label, click);		 
		 click();
	 };

	 var loadStorageReport = function(storageReport){
		 initBreadcrumb();
		 
		 var storageProviders = replaceReportPaneWithCopy("#storage-providers");
		 
		 var sm = storageReport.storageMetrics;
		 
		 $("#bytes", storageProviders).graphpanel({
			 title: "Bytes",
			 data:  formatPieChartData(
					 	sm.storageProviderMetrics, 
					 	"storageProviderType", 
					 	"totalSize"),
			 
		 });
		 
		 $("#files",storageProviders).graphpanel({
			 title: "Files",
			 data:  formatPieChartData(
					 	sm.storageProviderMetrics, 
					 	"storageProviderType", 
					 	"totalItems"),
		 });

		 $("#bytes .dc-graph, #files .dc-graph", storageProviders).bind("plotclick",function (event, pos, item){
			 handlePlotClick(item.series.label, function(){
				 var spm = getStorageProviderMetrics(item.series.label,storageReport);
				 loadStorageProviderReport(spm);
			 });
			 
		 });
		 
		 $("#mimetype-bytes",storageProviders).graphpanel({
			 title: "Mimetype Bytes",
			 data:  formatPieChartData(
					 	sm.mimetypeMetrics, 
					 	"mimetype", 
					 	"totalSize"),

		 });

		 $("#mimetype-files",storageProviders).graphpanel({
			 title: "Mimetype Files",
			 data:  formatPieChartData(
					 	sm.mimetypeMetrics, 
					 	"mimetype", 
					 	"totalItems"),
		 });
	 };
	 
	 
	 var getStorageReportIds = function(){
		 /*
		  * This is a mock up for now.
		  */
			ids = new Array();
			var d  = new Date().getTime();
			for(i = 0; i < 50; i++ ){
				ids.push(d - (60*1000*60*24*30*(50-i)));
			}
			
			return ids;
		};
	
	var storageReportIds = getStorageReportIds();
			 
	$("#report-date").html(new Date(storageReportIds[0]).toUTCString());

	var slider = $( "#slider" );
	slider.slider({
		value:storageReportIds.length-1,
		min: 0,
		max: storageReportIds.length-1,
		step: 1,
		slide:function(event,ui){
			$("#report-date").html(new Date(storageReportIds[ui.value]).toUTCString());
		},
		change: function( event, ui ) {
			getStorageReport(storageReportIds[ui.value]);
		}
	});
	
	var initBreadcrumb = function(){
		var bc = $("#report-breadcrumb");
		bc.empty();
		return bc.breadcrumb(
			{
				rootText:"Overview", 
				rootClick: function(){ 
					var value = slider.slider("value");
					getStorageReport(storageReportIds[value]);
				}
			}
		);
	};
	
	var bc = initBreadcrumb();
	
	 
	var reportMap = {};

	
	var generateMimetypeMetrics = function(){
		return [
                {
					 mimetype: "text/plain", 
					 totalItems: Math.random()*5000,
					 totalSize: Math.random()*100000
				 }, 
				 {
					 mimetype: "images/gif", 
					 totalItems: Math.random()*5000,
					 totalSize: Math.random()*100000
				 },
				 {
					 mimetype: "images/jpg", 
					 totalItems: Math.random()*5000,
					 totalSize: Math.random()*100000
				 },

			];
	};
	
	var generateMockSpaceMetrics = function(spaceName){
		return{
       	 spaceName:spaceName,
			 totalItems: Math.random()*5000,
			 totalSize: Math.random()*100000,
            mimetypeMetrics:generateMimetypeMetrics(),
        }	;	
	};
	
	var generateStorageProviderMetrics = function(id, type){
		return {
			
			 storageProviderId: id, 
			 storageProviderType: type,
			 totalItems: Math.random()*5000,
			 totalSize: Math.random()*100000,
			 spaceMetrics: 
				 [
	                 generateMockSpaceMetrics("Space-1"),
	                 generateMockSpaceMetrics("Space-2"),
	                 generateMockSpaceMetrics("Space-3"),
	             ],
			mimetypeMetrics: generateMimetypeMetrics(),
		 };
	};
	/*
	 * This is just test data
	 */
	var generateMockStorageReportData = function(timeInMs) {
		
	 	return {
			completionTime: timeInMs,
			storageMetrics: {
				totalSize: Math.random()*100000,
				totalItems: Math.random()*500,
				storageProviderMetrics: 
					[
					 generateStorageProviderMetrics(0,"Amazon S3"),
					 generateStorageProviderMetrics(1,"Rackspace"),
					 generateStorageProviderMetrics(2,"Azure"),
					],
				mimetypeMetrics: generateMimetypeMetrics(),
			},
				 
		 }; 
	};
	
	var getStorageReport = function(storageReportId){
		loadStorageReport(generateMockStorageReportData(storageReportId));
	};

	getStorageReport(storageReportIds[storageReportIds.length-1]);
	 
});