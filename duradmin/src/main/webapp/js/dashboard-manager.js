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

			plotPieChart(graph,this.options.data);
			$(this.element).append($.fn.create("div").append("<span>"+this.options.total + " " + this.options.units));

			 
		}, 
	});
})();


(function(){
	$.widget("ui.breadcrumb", {  
		options: {
			rootText: "root",
			titleClass: "dc-breadcrumb-title",
		},

		_crumb: new Array(),
		_init: function(){ 
			var that = this;
			$(this.element).append($.fn.create("div").append(
				$.fn.create("ul").addClass("horizontal-list")));
			$(this.element)
				.append(
					$.fn.create("div")
						.addClass(this.options.titleClass)
						.html(this.options.rootText));
			
			this._crumb.push(
							{element:this.options.rootText, 
							  click: function(){ 
								$(that.element).trigger({type:"rootclicked"});
							  }
							});
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
						
						if(value.click){
							value.click();
						}
						return false;
					}
				});
				
			});
			
			//and append to clickable list
			$("ul",this.element).append(
					item);
			
			//set the current item to the new element text
			this._title(newElement.element);
		},
	});
	
	$.widget("ui.storageprovider", {

		_storageReport: null,
		_storageProvider: null,

		_init: function(){
			var that = this;
			$(document).bind("storageproviderchanged", function(evt){
				 that._storageProvider = evt.storageProvider;
				 that._reload();
			});	

			$(document).bind("storagereportchanged", function(evt){
				 that._storageReport = evt.storageReport;
				 that._reload();
			});	

		},
		
		 _getStorageProviderMetrics: function(storageProvider,storageReport){
			 var spm = null;
			 $.each(storageReport.storageMetrics.storageProviderMetrics, function(i,sm){
				 if(sm.storageProviderType == storageProvider){
					 spm =  sm;
					 return false;
				 }
			 });
			 
			 return spm;
		 },

		_reload: function(){
		
			var spm  =  this._getStorageProviderMetrics(this._storageProvider, this._storageReport);
			$("#bytes,#mimetype-bytes",this.element).empty();
			if(!spm){
				$("#bytes,#mimetype-bytes",this.element).html("No data for this storage provider available for selected time period.");
				return;
			}else{
				plotPieChart($("#bytes", this.element), formatPieChartData(
							spm.spaceMetrics, 
						 	"spaceName", 
						 	"totalSize"));
	
				plotPieChart($("#mimetype-bytes",this.element),formatPieChartData(
									spm.mimetypeMetrics, 
								 	"mimetype", 
								 	"totalSize"));
			}
		},
	});	
})();


var centerLayout,mainContentLayout;

function plotPieChart(element, data){
	$.plot(element, data,
			 {
		         series: {
		            pie: {
		                show: true,
		                radius: 1,
		                label: {
		                    show: true,
		                    radius: 2/3,
		                    formatter: function(label, series){
		                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'+label+'<br/>'+formatGB(series.data[0][1],2)+'</div>';
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
	
    	
}

function formatGB(value, scale){
	var roundedVal;
	
	roundedVal = value/(1000*1000*1000);
	
	if(scale){
		var s = scale*10;
		roundedVal = Math.round(roundedVal*s)/s;
	}
	
	return roundedVal+ "GB";	
}

function plotBarChart(element, data){
	var ticks = [0,""];
	var i = 0;
	for(i=0; i < data.length; i++){
		ticks.push([i+1,data[i].label]);
	};
	
	var xax = {
		ticks:ticks
	};
	
	$.plot(element, data,
			 {
		         series: {
					bars: {show:true, align:"center"},
			     },
		         
		         grid: {
		             hoverable: true,
		             clickable: true
		         },
		         legend: { show:false},
		         xaxis: xax,
		         yaxis: {
		        	tickFormatter: function (value) {
		        	   return formatGB(value);
		         	},
		         }
			 }
		 );	
	
    var previousPoint = null;

    $(element).unbind("plothover");
    $(element).bind("plothover", function (event, pos, item){
        $("#x").text(pos.x.toFixed(2));
        $("#y").text(pos.y.toFixed(2));
 
        if (item) {
            if (previousPoint != item.dataIndex) {
                previousPoint = item.dataIndex;
                
                $("#tooltip").remove();
                var x = item.datapoint[0].toFixed(2),
                    y = item.datapoint[1].toFixed(2);
                
                showTooltip(item.pageX, item.pageY,
                            item.series.label + ": " +formatGB(y,2));
            }
        }
        else {
            $("#tooltip").remove();
            previousPoint = null;            
        }
	});
}

function showTooltip(x, y, contents) {
    $('<div id="tooltip">' + contents + '</div>').css( {
        position: 'absolute',
        'z-index': 100,
        display: 'none',
        top: y + 5,
        left: x + 5,
        border: '1px solid #fdd',
        padding: '2px',
        'background-color': '#fee',
        opacity: 0.80,
        
    }).appendTo("body").fadeIn(200);
}

/**
 * converts an array of arbitray objects into something
 * flot pie chart can read.
 */
function formatPieChartData(inputArray, label, value){
	 var data = new Array();
	 var i;
	 for(i in inputArray){
		 data.push({
			 label: inputArray[i][label],
			 data: inputArray[i][value],
		 });
	 };
	 
	 return data;
}


function formatBarChartData(inputArray, label, value){
	 var data = new Array();
	 var i;
	 for(i = 0; i < inputArray.length;i++){
		 data.push({
			 bars:{show:true},
			 label: inputArray[i][label],
			 data: 	[[(i+1),0],[(i+1),inputArray[i][value]]],
		 });
	 };
	 
	 return data;
}

$(function() {
	centerLayout = $('#page-content').layout({
	center__paneSelector:	"#main-content-panel"
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
	


	 
	 var loadSpaceReport = function (spaceMetrics){
		 var sp = $("#space");
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

	 
	 var handlePlotClick = function(label,click){
		handleStorageProviderPlotClick(label, click);
	 };

	 var handleStorageProviderPlotClick = function(label,click){
		 var element = $.fn.create("div").css("display", "inline-block");
		 element.text(label);
		 addToBreadcrumb(element, click);		 
	 };

	 var seekTo = function(index){
		 var api;
		 $(".scrollable").scrollable();
		 api = $(".scrollable").data("scrollable");
		 api.seekTo(index, 500);
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
		 click();
	};
		
	var initBreadcrumb = function(){
		var bc = getBreadcrumb();
		bc.empty();

		bc.breadcrumb({
			rootText:"Overview", 
		});
		return bc;
		
	};

	var getCurrentReportId = function(storageReportIds){
		return storageReportIds[getSliderIndex()];
	};
	
	var getCurrentStorageReport = function(storageReportIds){
		return reportMap[getCurrentReportId(storageReportIds)];
	};

	var getSlider = function(){
		return $( "#report-date-slider" );
	};
	
	var getSliderIndex = function(){
		return getSlider().slider("value");
	};

	var initSlider = function(storageReportIds){
		var slider = getSlider();
		slider.slider({
			value:0,
			min: 0,
			max: storageReportIds.length-1,
			step: 1,
			slide:function(event,ui){
				$("#report-selected-date").html(formatSelectedDate(storageReportIds[ui.value]));
			},
			change: function( event, ui ) {
				getStorageReport(storageReportIds[ui.value], function(storageReport){
					fireStorageReportChangedEvent(storageReport);
				});
			}
		});
		return slider;
	};
	
	var fireStorageReportChangedEvent = function(storageReport){
		var event = jQuery.Event("storagereportchanged");
		event.storageReport = storageReport;
		$(document).trigger(event);		
	};

	var getStorageReport = function(storageReportId, callback){
		var report = reportMap[storageReportId];
		if(report){
			if(callback){
				callback(report);
			}
		}else{
			dc.busy("Loading...");
			
			dc.ajax({
				url: "/duradmin/storagereport/get?reportId=" + storageReportId,
				type:"GET",
				success: function(result){
					dc.done();
					var storageReport = result.storageReport;
					reportMap[storageReportId] = storageReport;	
					callback(storageReport);
				},
				
			    failure: function(textStatus){
					dc.done();
					alert("failed to get storage report");
				},
			});	
		}
	};
		

	var getStorageReportIds = function(callback){
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
		$("#tabs-storage .graph-switch").buttonset();
		initBreadcrumb();

		dc.busy("Loading reports...");

		getStorageReportIds(
		{
			success: function(storageReportIds){
				dc.done();
				
				//initialize date controls
				initSlider(storageReportIds);

				getBreadcrumb().bind("rootclicked", function(event){
					seekTo(0);
				});
				
				var selectedReportId = storageReportIds[0];
				$("#report-selected-date").html(formatSelectedDate(selectedReportId));
				$("#report-start-range").html(formatChartDate(storageReportIds[0]));
				$("#report-end-range").html(formatChartDate(storageReportIds[storageReportIds.length-1]));
				
				initStorageProvidersView(storageReportIds);
				initStorageProviderView();
				
				//load the most recent report
				getStorageReport(selectedReportId, function(storageReport){
					fireStorageReportChangedEvent(storageReport);
				});
			},
			
			failure: function(){
				dc.done();
			}
		});
		
		
	};


	
	var initStorageProviderView = function (){
		$("#storage-provider").storageprovider();
	};
	
	var initStorageProvidersView = function(){
		$(document).bind("storagereportchanged", function(evt){
			 var sm = evt.storageReport.storageMetrics;
			 var storageProviders = $("#storage-providers");
			 //addToBreadcrumb(storageReport.contentId);
			 /*
			 $("#bytes", storageProviders).graphpanel({
				 title: "Bytes",
				 data:  formatPieChartData(
						 	sm.storageProviderMetrics, 
						 	"storageProviderType", 
						 	"totalSize"),
				 total: sm.totalSize,
				 units: "Bytes",
			 });
			 */
			 
			 plotBarChart($("#bytes", storageProviders), formatBarChartData(
														 	sm.storageProviderMetrics, 
														 	"storageProviderType", 
														 	"totalSize"));
			 
			 plotPieChart($("#mimetype-bytes",storageProviders),formatPieChartData(
																 	sm.mimetypeMetrics, 
																 	"mimetype", 
																 	"totalSize"));
		});
		
		$("#bytes, #mimetype-bytes", $("#storage-providers")).bind("plotclick",function (event, pos, item){
			 handlePlotClick(item.series.label, function(){
				 seekTo(1);
				 fireStorageProviderChangedEvent(item.series.label);
			 });
		 });	
	}
	
	var fireStorageProviderChangedEvent = function(storageProvider){
		var event = jQuery.Event("storageproviderchanged");
		event.storageProvider = storageProvider;
		$(document).trigger(event);		
	};


	var reportMap = {};

	initializeView();

});