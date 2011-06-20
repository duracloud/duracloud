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
    	    total: null,
    	    units: null,
		},
		
		_init: function(){ 
			$(this.element).addClass("dc-small-graph-panel").append($.fn.create("h3").html(this.options.title));
			
			var graph = $.fn.create("div").addClass("dc-graph");
			$(this.element).append($.fn.create("div").append(graph));


			 $.plot(graph, this.options.data,
				 {
			         series: {
			            pie: {
			                show: true,
			                radius: 1,
			                label: {
			                    show: true,
			                    radius: 2/3,
			                    formatter: function(label, series){
			                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'+label+'<br/>'+series.data[0][1]+'</div>';
			                    },
			                    threshold: 0.1,
			                    background: {
			                        opacity: 0.5,
			                        color: '#000'
			                    }
			                }
			            }
			         },
			         
			         grid: {
			             hoverable: true,
			             clickable: true
			         },
			         legend: { show:false},
				 }
			 );
			 
			 $(this.element).append($.fn.create("div").append("<span>"+this.options.total + " " + this.options.units));

			 
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
			this._crumb.push({element:this.options.rootText, click: this.options.rootClick});
		}, 
		_title: function (/*text or jquery obj*/title){
			var titleElement = this.element.find("."+this.options.titleClass);
			titleElement.empty();
			if(!title){
				return titleElement.html();
			}else{
				titleElement.append(title);
			}
		},
		
		add: function(element, click){
			var that = this;
			//get last element in crumb
			var lastElement = this._crumb[this._crumb.length-1];

			//add the new element to internal list
			var newElement = {element:element, click: click};
			this._crumb.push(newElement);
			
			//add last element to the clickable list items
			var item = $.fn.create("li");
			
			item.append(lastElement.element);
			//wrap clickable
			item.click(function(){
				$.each(that._crumb,function(i,value){
					//remove all elements after matching value
					if(lastElement == value){
						that._crumb.splice(i+1,that._crumb.length);
						
						//put the element of the clicked value 
						//in the title field.
						that._title(value.element);
						
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
			this._title(newElement.element);
		},
	});
})();

var centerLayout,mainContentLayout;

$(function() {
	centerLayout = $('#page-content').layout({
	// minWidth: 300 // ALL panes
	/*
		north__size: 			50	
	,	north__paneSelector:     ".center-north"
	,   north__resizable:   false
	,   north__slidable:    false
	,   north__spacing_open:			0			
	,	north__togglerLength_open:		0			
	,	north__togglerLength_closed:	0			
	,
	*/
	   west__size:				400
	,	west__minSize:			400
	,   west__resizable:   true
	,   west__slidable:    true

	,   west__paneSelector:     "#dynamic-panel"
	,	center__paneSelector:	"#main-content-panel"
	});
	
	mainContentLayout = $('#main-content-panel').layout({
		// minWidth: 300 // ALL panes
			north__size: 			90	
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
			 total: spaceMetrics.totalSize,
			 units: "Bytes",

		 });

		 $("#mimetype-files",sp).graphpanel({
			 title: "Mimetype Files",
			 data:  formatPieChartData(
					 	mtm, 
					 	"mimetype", 
					 	"totalItems"),
			 total: spaceMetrics.totalItems,
			 units: "Files",

		 });

	 };
	 
	 var loadStorageProviderReport = function (storageProviderMetrics){
		 var spm  = storageProviderMetrics;
		 var sp = replaceReportPaneWithCopy("#storage-provider");
		 
		 var sm = storageProviderMetrics.spaceMetrics;
		 var mtm = storageProviderMetrics.mimetypeMetrics;
		 $("#bytes", sp).graphpanel({
			 title: "Bytes",
			 data:  formatPieChartData(
					 	sm, 
					 	"spaceName", 
					 	"totalSize"),
			 total: spm.totalSize,
			 units: "Bytes",
		 });

		 $("#files", sp).graphpanel({
			 title: "Files",
			 data:  formatPieChartData(
					 	sm, 
					 	"spaceName", 
					 	"totalItems"),
			 total: spm.totalItems,
			 units: "Files",

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

			 total: spm.totalSize,
			 units: "Bytes",
					 	
		 });

		 $("#mimetype-files",sp).graphpanel({
			 title: "Mimetype Files",
			 data:  formatPieChartData(
					 	mtm, 
					 	"mimetype", 
					 	"totalItems"),
			 total: spm.totalItems,
			 units: "Files",
					 	
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
		 handleStorageProviderPlotClick(label, click);
	 };

	 var handleStorageProviderPlotClick = function(label,click){
		 var element = $.fn.create("div").css("display", "inline-block");
		 element.text(label);
		 addToBreadcrumb(element, click);		 
		 click();
	 };

	 var loadStorageReport = function(storageReport){
		 var storageProviders = replaceReportPaneWithCopy("#storage-providers");
		 var sm = storageReport.storageMetrics;
		 initBreadcrumb(storageReport.contentId);
		 
		 $("#bytes", storageProviders).graphpanel({
			 title: "Bytes",
			 data:  formatPieChartData(
					 	sm.storageProviderMetrics, 
					 	"storageProviderType", 
					 	"totalSize"),
			 total: sm.totalSize,
			 units: "Bytes",
		 });
		 
		 $("#files",storageProviders).graphpanel({
			 title: "Files",
			 data:  formatPieChartData(
					 	sm.storageProviderMetrics, 
					 	"storageProviderType", 
					 	"totalItems"),
			 total: sm.totalItems,
			 units: "Files",
		 });

		 $("#bytes .dc-graph, #files .dc-graph", storageProviders).bind("plotclick",function (event, pos, item){
			 handleStorageProviderPlotClick(item.series.label, function(){
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
			 total: sm.totalSize,
			 units: "Bytes",
		 });

		 $("#mimetype-files",storageProviders).graphpanel({
			 title: "Mimetype Files",
			 data:  formatPieChartData(
					 	sm.mimetypeMetrics, 
					 	"mimetype", 
					 	"totalItems"),
			 total: sm.totalItems,
			 units: "Files",
		 });
	 };
	 
	
	var formatChartDate = function(/*string*/reportId){
		return new Date(convertStorageReportIdToMs(reportId))
					.toLocaleDateString();
	};
	
	var formatSelectedDate = function(/*string*/reportId){
		return new Date(convertStorageReportIdToMs(reportId))
					.toLocaleDateString();
	};

	/**
	 * extract date info from reportId and convert to milliseconds
	 */
	var convertStorageReportIdToMs = function(storageReportId){
		var pattern = /storage-report-(.*)[.]xml/i;
		var newVal = pattern.exec(storageReportId)[1];
		return new Date(newVal).getTime();
	};


	var getBreadcrumb = function(){
		return $("#report-breadcrumb");
	};

	var addToBreadcrumb = function (element, click){
		 getBreadcrumb().breadcrumb("add", element, click);
	};
		
	var initBreadcrumb = function(storageReportId){
		var bc = getBreadcrumb();
		bc.empty();
		return bc.breadcrumb(
			{
				rootText:"Overview", 
				rootClick: function(){ 
					getStorageReport(storageReportId);
				}
			}
		);
	};


	var initSlider = function(storageReportIds){
		var slider = $( "#report-date-slider" );
		slider.slider({
			value:0,
			min: 0,
			max: storageReportIds.length-1,
			step: 1,
			slide:function(event,ui){
				$("#report-selected-date").html(formatSelectedDate(storageReportIds[ui.value]));
			},
			change: function( event, ui ) {
				getStorageReport(storageReportIds[ui.value]);
			}
		});
		
		return slider;
	};
	

	var getStorageReport = function(storageReportId){
		var report = reportMap[storageReportId];
		if(report){
			loadStorageReport(report);
		}else{
			dc.ajax({
				url: "/duradmin/storagereport/get?reportId=" + storageReportId,
				type:"GET",
				success: function(result){
					var storageReport = result.storageReport;
					reportMap[storageReportId] = storageReport;	
					loadStorageReport(storageReport);
				},
				
			    failure: function(textStatus){
					alert("failed to get storage report");
				},
			});	
			
			//report = generateMockStorageReportData(storageReportId);
			//reportMap[storageReportId] = report;
		}
	};
		

	var getStorageReportIds = function(callback){
		//return getMockStorageIds();	
		var ids = null;
		
		dc.ajax({
			url: "/duradmin/storagereport/list",
			type:"GET",
			success: function(result){
				callback.success(result.storageReportList);
			},
			
		    failure: function(textStatus){
				alert("failed to get ");
			},
		});	
	};

	
	var initializeView = function(){
		$("#main-content-tabs").tabs();
		$("#main-content-tabs").tabs("select", 1);
		getStorageReportIds(
		{
			success: function(storageReportIds){

				//load the most recent report
				getStorageReport(storageReportIds[storageReportIds.length-1]);
				
				//initialize date controls
				var selectedReportId = storageReportIds[0];
				$("#report-selected-date").html(formatSelectedDate(selectedReportId));
				$("#report-start-range").html(formatChartDate(storageReportIds[0]));
				$("#report-end-range").html(formatChartDate(storageReportIds[storageReportIds.length-1]));
				initSlider(storageReportIds);
				initBreadcrumb(selectedReportId);
			}
		});
	};

	var reportMap = {};

	initializeView();
	/*
	var getMockStorageReportIds = function(){
			ids = new Array();
			var d  = new Date().getTime();
			var i;
			for(i = 1; i < 10; i++ ){
				ids.push("storage-report-2011-0"+i+"-03T11-30-41.xml");
			}
			return ids;
	};
	
	var generateMimetypeMetrics = function(){
		return [
                {
					 mimetype: "text/plain", 
					 totalItems: Math.round(Math.random()*5000),
					 totalSize: Math.round(Math.random()*100000),
				 }, 
				 {
					 mimetype: "images/gif", 
					 totalItems: Math.round(Math.random()*5000),
					 totalSize: Math.round(Math.random()*100000),
				 },
				 {
					 mimetype: "images/jpg", 
					 totalItems: Math.round(Math.random()*5000),
					 totalSize: Math.round(Math.random()*100000),
				 },

			];
	};
	
	var generateMockSpaceMetrics = function(spaceName){
		return{
       	 spaceName:spaceName,
			 totalItems: Math.round(Math.random()*5000),
			 totalSize: Math.round(Math.random()*100000),
			 mimetypeMetrics:generateMimetypeMetrics(),
        }	;	
	};
	
	var generateStorageProviderMetrics = function(id, type){
		return {
			
			 storageProviderId: id, 
			 storageProviderType: type,
			 totalItems: Math.round(Math.random()*5000),
			 totalSize: Math.round(Math.random()*100000),
			 spaceMetrics: 
				 [
	                 generateMockSpaceMetrics("Space-1"),
	                 generateMockSpaceMetrics("Space-2"),
	                 generateMockSpaceMetrics("Space-3"),
	             ],
			mimetypeMetrics: generateMimetypeMetrics(),
		 };
	};
	
	var generateMockStorageReportData = function(timeInMs) {
		
	 	return {
			completionTime: timeInMs,
			storageMetrics: {
				 totalItems: Math.round(Math.random()*5000),
				 totalSize: Math.round(Math.random()*100000),
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
	*/
});