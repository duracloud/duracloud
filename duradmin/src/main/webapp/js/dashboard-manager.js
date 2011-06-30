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
		/*
		 * converts an array of objects into an array of arrays. 
		 * the fieldNames array specifies the name and order of the fields
		 * to appear in the destination array 
		 */
		_toArray: function(/*array*/ source, /*array*/ fieldNames){
			var newArray;
			newArray = [];
			$.each(source, function(i,item){
				var row = [];
				$.each(fieldNames, function(j, cell){
					row[j] = item[cell];
				});
				newArray[i] = row;
			});
			
			return newArray;
		},
		_init: function(){ 
			var that = this;
			$(this.element).addClass("dc-graph-panel");
			$(this.element).append("<div class='header'><div class='title'></div><div class='button-panel'></div></div>");
			$(this.element).append("<div class='dc-graph'></div>");
			$(this.element).append("<div></div>");
			this._title(this.options.title);
			this._addButton("Data", function(){
				var d,tableData, table;
				d = $.fn.create("div");
				$(that.element).append(d);
				tableData = that._toArray(that.options.data, ["label", "data"]);
				
				table = dc.createTable(tableData, ["label", "value number"], ["Name", "Value"]);
				$(table).addClass("tablesorter");
				$(table).tablesorter({sortList: [[0,0],] });
				d.append(table);
				d.dialog({
					autoOpen: true,
					height: 300,
					width: 350,
					modal: false,});
			});
		}, 
		
		_title: function(title){
			$(".title", this.element).html(title);
		},

		_addButton: function(text, handler){
			$(".button-panel", this.element).append($.fn.create("button").html(text).click(handler));
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

		up: function(){
			$("li:last-child",this.element).trigger("click");
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
	
	$.widget("ui.basegraphpanel", {
		_storageReport: null,
		_init: function(){
			var that = this;
	
			$(document).bind("storagereportchanged", function(evt){
				 that._storageReport = evt.storageReport;
				 that._reload();
			});	
		},

		 _clearGraphs: function(){
				$(".dc-graph-panel",this.element).empty();
		 },
		 
		 _showNoData: function(){
				$(".dc-graph",this.element).html("Not applicable for selected time period.");
		 },
		 
		_loadMimetypeGraphs:function(metrics){
			var data, mimetypeBytes, mimetypeFiles, mimetypePanel;
		 	mimetypePanel = $(".mimetype-panel", this.element);
		 	
			data = formatPieChartData(
					metrics.mimetypeMetrics, 
					"mimetype", 
		 			"totalSize");
			mimetypeBytes = $(".bytes-graph",mimetypePanel);
			mimetypeBytes.graphpanel({title: "Bytes: " + formatGB(metrics.totalSize,2), data: data});
			plotPieChart(
				$(".dc-graph", mimetypeBytes), 
				data, 
				function(x){ return formatGB(x,2);}
			);
	
			//files
			data = formatPieChartData(
					metrics.mimetypeMetrics, 
				 	"mimetype", 
				 	"totalItems");
			mimetypeFiles = $(".files-graph", mimetypePanel);
			mimetypeFiles.graphpanel({title:"Files: " + metrics.totalItems, data:data});
			plotPieChart($(".dc-graph", mimetypeFiles), data, 
					 	function(x){ return x;}		 	
			);
		},
		
		_reload: function(){
			var entityPanel, mimetypePanel, entityDisplayed;
			
			this._clearGraphs();
			
			//hack to prevent an error when trying to render a graph
			//in a hidden div PART 1
			entityPanel = $(".entity-panel", this.element);
			mimetypePanel = $(".mimetype-panel", this.element);
			entityDisplayed = mimetypePanel.is(":hidden");
			entityPanel.show();
			mimetypePanel.show();
			
			this._reloadImpl();

			//hack to prevent an error when trying to render a graph
			//in a hidden div PART 2
			if(entityDisplayed)
				mimetypePanel.hide();
			else
				entityPanel.hide();

		},
		
		_getStorageMetrics: function(){
			return this._storageReport.storageMetrics;
		},
		_reloadImpl: function(){
			alert("this function should be overridden");
		},

	});
	
	$.widget("ui.storageprovider", 
		$.extend({}, $.ui.basegraphpanel.prototype, {
			_storageProvider: null,
			_init: function(){
				var that = this;
				$.ui.basegraphpanel.prototype._init.call(this); 	

				$(document).bind("storageproviderchanged", function(evt){
					 that._storageProvider = evt.storageProvider;
					 that._reload();
				});	
	
				$(".entity-panel .bytes-graph", this.element).bind("plotclick",function (event, pos, item){
					var event = $.Event("spacechanged");
					event.spaceName = item.series.label;
					event.storageReport = that._storageReport;
					event.storageProvider = that._storageReport;
					$(document).trigger(event);
				});
			},
			
			_getStorageProviderMetrics: function(){
				 var spm = null;
				 var that = this;
				 $.each(this._getStorageMetrics().storageProviderMetrics, function(i,sm){
					 if(sm.storageProviderType == that._storageProvider){
						 spm =  sm;
						 return false;
					 }
				 });
				 
				 return spm;
			},

			_reloadImpl: function(){
				 
				var spm, 
					entityPanel,
					mimetypePanel,
					data, 
					bytes, 
					files, 
					mimetypeBytes, 
					mimetypeFiles;
				
				spm  =  this._getStorageProviderMetrics();
				if(!spm){
					this._showNoData();
					return;
				}

				entityPanel = $(".entity-panel", this.element);
				mimetypePanel = $(".mimetype-panel", this.element);

				//entity
				//bytes
				data = formatPieChartData(
						spm.spaceMetrics, 
						"spaceName", 
			 			"totalSize");

				bytes = $(".bytes-graph",entityPanel);
				bytes.graphpanel({title: "Bytes: " + formatGB(spm.totalSize,2), data: data});
				plotPieChart(
					$(".dc-graph", bytes), 
					data, 
					function(x){ return formatGB(x,2);}
				);

				//files
				data = formatPieChartData(
						spm.spaceMetrics, 
					 	"spaceName", 
					 	"totalItems");
				
				files = $(".files-graph", entityPanel);
				files.graphpanel({title:"Files: " + spm.totalItems, data:data});
				plotPieChart($(".dc-graph", files), data, 
						 	function(x){ return x;}		 	
				);

				this._loadMimetypeGraphs(spm);
			},
		})
	);



	$.widget("ui.spacegraphpanel", 
		$.extend({}, $.ui.storageprovider.prototype, 
			{
				_spaceName: null,

				_init: function(){
					$.ui.storageprovider.prototype._init.call(this); 					
					var that = this;
					
					$(document).bind("spacechanged", function(evt){
						 that._spaceName = evt.spaceName;
						 that._reload();
					});	
				},
				_getSpaceMetrics: function(){
					var that = this;
					var spm = this._getStorageProviderMetrics();
					var s = null;
					if(!spm){
						return null;
					}
				
					 $.each(spm.spaceMetrics, function(i,sm){
						if(sm.spaceName == that._spaceName){
							s = sm;
							return false;
						}
					 });
					return s;
				},
	
				_reloadImpl: function(){
					var spm;
					var spm  =  this._getSpaceMetrics();
					
					if(!spm){
						this._showNoData();
					}else{
						//mimetype
						//bytes
						this._loadMimetypeGraphs(spm);
					}
				},
			}
		)
	);	

	
	$.widget("ui.summarypanel", 
		$.extend({}, $.ui.basegraphpanel.prototype, {
			_init: function(){
				var that = this;
				$.ui.basegraphpanel.prototype._init.call(this); 	
				$(document).bind("storagereportchanged", function(evt){
					that._storageReport = evt.storageReport;
					that._reload();
				});		
				
				$(".entity-panel .bytes-graph, .entity-panel .files-graph", this.element)
					.bind("plotclick",function (event, pos, item){
						var event = $.Event("storageproviderchanged");
						event.storageProvider = item.series.label;
						event.storageReport = that._storageReport;
						$(document).trigger(event);
					});	
				
			},
			
			_reloadImpl: function(){
				var spm, 
				entityPanel,
				mimetypePanel,
				data, 
				bytes, 
				files, 
				mimetypeBytes, 
				mimetypeFiles;

			spm  =  this._getStorageMetrics();
			entityPanel = $(".entity-panel", this.element);
			mimetypePanel = $(".mimetype-panel", this.element);

			//entity
			//bytes

			data = formatBarChartData(
				 	spm.storageProviderMetrics, 
				 	"storageProviderType", 
				 	"totalSize");

			bytes = $(".bytes-graph",entityPanel);
			bytes.graphpanel({title: "Bytes: " + formatGB(spm.totalSize,2), data: data});
			plotBarChart(
				$(".dc-graph", bytes), 
				data, 
			 	function(value){
					return formatGB(value);
				},
				function(value){
					return formatGB(value,2);
				}
			);

			//files
			data = formatBarChartData(
				 	spm.storageProviderMetrics, 
				 	"storageProviderType", 
				 	"totalItems")
			
			files = $(".files-graph", entityPanel);
			files.graphpanel({title:"Files: " + spm.totalItems, data:data});

			 plotBarChart($(".dc-graph", files), 
					 	data, 
					 	function(value){
							return(value);
						},
						function(value){
							return(value);
						}
					);

			 this._loadMimetypeGraphs(spm);
				
			},
		})
	);	

	
})();


var centerLayout,mainContentLayout;

function plotPieChart(element, data, labelFormatter){
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
		                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'+label+'<br/>'+labelFormatter(series.data[0][1])+'</div>';
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

function plotBarChart(element, data, tickFormatter, labelFormatter){
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
					bars: {show:true, align:"center", barWidth:0.75},
			     },
		         
		         grid: {
		             hoverable: true,
		             clickable: true
		         },
		         legend: { show:false},
		         xaxis: xax,
		         yaxis: {
		        	tickFormatter: function (value) {
		        	   return tickFormatter(value);
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
                            item.series.label + ": " +labelFormatter(y));
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
	


	/* 
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
	 
	 */
	 
	 /*
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

	*/
	/*
	 
	 var handlePlotClick = function(label,click){
		handleStorageProviderPlotClick(label, click);
	 };

	 
	 var handleStorageProviderPlotClick = function(label,click){
		 addToBreadcrumb(label, click);		 
	 };
	 */
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

	var addToBreadcrumb = function (label, click){
		 var element = $.fn.create("div").css("display", "inline-block");
		 element.text(label);

		getBreadcrumb().breadcrumb("add", element, click);
		if(click){
			click();
		}
	};
		
	var initBreadcrumb = function(){
		var bc = getBreadcrumb();
		bc.empty();

		bc.breadcrumb({
			rootText:"Summary", 
		});

		$(document).bind("spacechanged", function(evt){
			addToBreadcrumb(evt.spaceName);
		})

		$(document).bind("storageproviderchanged", function(evt){
			addToBreadcrumb(evt.storageProvider, function(){
				seekTo(1);
			});
		})

		
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
			value:storageReportIds.length-1,
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

	var initGraphSwitcher = function(){
		var graphSwitch,toggleRadio;
		graphSwitch = $("#tabs-storage .graph-switch");
		toggleRadio = function(evt){
			var t, selected, showEntity, panels;
			t = $(evt.target);
			showEntity = false;
			$("input[type=radio]", graphSwitch).removeAttr("checked");
			if(t.hasClass("entity-radio")){
				selected = $(".entity-radio", graphSwitch);
				showEntity = true;
			}else{
				selected = $(".mimetype-radio", graphSwitch);
			};
			selected.attr("checked", "checked");
			panels = [
				          $(".entity-panel", "#storage-summary, #storage-provider"), 
				          $(".mimetype-panel", "#storage-summary, #storage-provider"),
			          ];
			if(!showEntity) panels.reverse();
			panels[0].show("slow");
			panels[1].hide("slow");
		};
		
		$(".entity-radio, .mimetype-radio", graphSwitch).click(function(evt){
			toggleRadio(evt);
		});
	};
	
	var initializeView = function(){
		
		$("#main-content-tabs").tabs();
		$("#main-content-tabs").tabs("select", 1);

		initGraphSwitcher();
		initBreadcrumb();
		initStorageProvidersView();
		initStorageProviderView();
		initSpaceView();

		$("#tabs-storage .back-link").click(function(){
			getBreadcrumb().breadcrumb("up");
		});

		
		dc.busy("Loading reports...");

		getStorageReportIds(
		{
			success: function(storageReportIds){
				storageReportIds.reverse();
				dc.done();
				
				//initialize date controls
				initSlider(storageReportIds);

				getBreadcrumb().bind("rootclicked", function(event){
					seekTo(0);
				});
				
				var selectedReportId = getCurrentReportId(storageReportIds);
				$("#report-selected-date").html(formatSelectedDate(selectedReportId));
				$("#report-start-range").html(formatChartDate(storageReportIds[0]));
				$("#report-end-range").html(formatChartDate(storageReportIds[storageReportIds.length-1]));
				
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
		var sp;
		sp = $("#storage-provider");
		sp.storageprovider();
	};

	var initSpaceView = function (){
		$("#space").spacegraphpanel();
		$(document).bind("spacechanged", function(){
			seekTo(2);
		})
	};
	
	var initStorageProvidersView = function(){
		$("#storage-summary").summarypanel();
		$(document).bind("storageproviderchanged", function(evt){
			seekTo(1);
		})
	};
	


	var reportMap = {};

	initializeView();

});