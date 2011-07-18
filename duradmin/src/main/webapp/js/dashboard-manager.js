/**
 * Dashboard Manager
 * 
 * @author Daniel Bernstein
 */



(function(){
	$.widget("ui.graphpanel", {  
		options: {
			title: "Placeholder Title",
			total: "total place holder",
			dataTable: null,
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
			$(this.element).append("<div class='header'><div>Total: <span class='total'></span></div><div class='button-panel'></div></div>");
			$(this.element).append("<div class='dc-graph'></div>");
			$(this.element).append("<div><h5 class='title'></h5></div>");
			this._title(this.options.title);
			this._total(this.options.total);

			if(this.options.dataTable){
				this._addButton("Data", function(){
					var d,tableData, table;
					d = $.fn.create("div");
					$(that.element).append(d);
					table = dc.createTable(that.options.dataTable.rows, that.options.dataTable.columnDefs);
					$(table).addClass("tablesorter");
					$(table).tablesorter({sortList: [[0,0],] });
					d.append(table);
					d.dialog({
						autoOpen: true,
						height: 300,
						width: 350,
						modal: false,});
				});
			}
		}, 
		
		_title: function(title){
			$(".title", this.element).html(title);
		},

		_total: function(total){
			$(".total", this.element).html(total);
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
		
		_entityColumnTitles: ["Storage Provider", "GBs", "Files"],
		_mimetypeColumnTitles: ["File Type", "GBs", "Files"],

		 _clearGraphs: function(){
				$(".dc-graph-panel",this.element).empty();
		 },
		 
		 _showNoData: function(){
				$(".dc-graph",this.element).html("Not applicable for selected time period.");
		 },
		 
		_formatGigabytes: function(value){
			 return dc.formatGB(value,2, false);
		 },
		_formatDataTable: function(firstColumnFieldName, firstColumnDisplayName,  metrics){
			 var dt = {};
			 var defaultCssClass = "label number";
			 
			 dt.columnDefs = 
				 [
				  {name: firstColumnDisplayName}
				 ,{name: "Gigabytes",cssClass:defaultCssClass, formatter: this._formatGigabytes}
				 ,{name: "Files",cssClass:defaultCssClass}
				 ];
			 dt.rows = [];
			 $.each(metrics, function(i,val){
				 dt.rows.push([val[firstColumnFieldName], val["totalSize"], val["totalItems"]])
			 });
			 
			 return dt;
		 },
		 
		_loadMimetypeGraphs:function(titlePrefix, metrics){
			var data, dataTable, mimetypeBytes, mimetypeFiles, mimetypePanel;
		 	mimetypePanel = $(".mimetype-panel", this.element);
		 	dataTable = this._formatDataTable("mimetype", "File Types", metrics.mimetypeMetrics);
			data = formatPieChartData(
					metrics.mimetypeMetrics, 
					"mimetype", 
		 			"totalSize");
			mimetypeBytes = $(".bytes-graph",mimetypePanel);
			mimetypeBytes.graphpanel({title: titlePrefix+ " by File Type (Size)", total:dc.formatGB(metrics.totalSize,2)});
			plotPieChart(
				$(".dc-graph", mimetypeBytes), 
				data, 
				function(x){ return dc.formatGB(x,2);}
			);
	
			//files
			data = formatPieChartData(
					metrics.mimetypeMetrics, 
				 	"mimetype", 
				 	"totalItems");
			mimetypeFiles = $(".files-graph", mimetypePanel);
			mimetypeFiles.graphpanel({title: titlePrefix+ " by File Type (Count)", total: metrics.totalItems +" Files", dataTable: dataTable});
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
				
				this._entityColumnTitles[0] = "Space";
				
				$.ui.basegraphpanel.prototype._init.call(this); 	

				$(document).bind("storageproviderchanged", function(evt){
					 that._storageProvider = evt.storageProvider;
					 that._reload();
				});	
	
				$(".entity-panel",this.element).find(".bytes-graph, .files-graph")
					.bind("plotclick",function (event, pos, item){
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
				 
				var that, 
					spm, 
					entityPanel,
					mimetypePanel,
					data, 
					bytes, 
					files, 
					dataTable;
				
				that = this;
				
				spm  =  this._getStorageProviderMetrics();
				if(!spm){
					this._showNoData();
					return;
				}

				entityPanel = $(".entity-panel", this.element);
				mimetypePanel = $(".mimetype-panel", this.element);

				//bytes
				var titlePrefix =  "Spaces in " + dc.STORAGE_PROVIDER_KEY_MAP[spm.storageProviderType];
				data = formatPieChartData(
						spm.spaceMetrics, 
						"spaceName", 
			 			"totalSize");

				bytes = $(".bytes-graph",entityPanel);
				bytes.graphpanel({title: titlePrefix + " (Size)", total:dc.formatGB(spm.totalSize,2)});
				plotPieChart(
					$(".dc-graph", bytes), 
					data, 
					function(x){ return dc.formatGB(x,2);}
				);

				//files
				data = formatPieChartData(
						spm.spaceMetrics, 
					 	"spaceName", 
					 	"totalItems");

			 	dataTable = this._formatDataTable("spaceName", "Spaces", spm.spaceMetrics);
				files = $(".files-graph", entityPanel);
				files.graphpanel({title: titlePrefix + " (Count)", total:spm.totalItems, dataTable: dataTable});
				plotPieChart($(".dc-graph", files), data, 
						 	function(x){ return x;}		 	
				);

				this._loadMimetypeGraphs("Spaces", spm);
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
						this._loadMimetypeGraphs(spm.spaceName, spm);
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
						that._fireStorageProviderChanged(item.series.label);
					});	
				
			},

			_fireStorageProviderChanged: function(storageProviderId){
				var event = $.Event("storageproviderchanged");
				event.storageProvider = storageProviderId;
				event.storageReport = this._storageReport;
				$(document).trigger(event);				
			},
			
			_reloadImpl: function(){
				var that, 
					spm, 
					entityPanel,
					mimetypePanel,
					data, 
					bytes, 
					files, 
					mimetypeBytes, 
					mimetypeFiles, 
					xTickFormatter,
					dataTable, 
					storageProviderLinkClass,
					titlePrefix;

				
				that = this;

				spm  =  this._getStorageMetrics();
				
				
				entityPanel = $(".entity-panel", this.element);
				mimetypePanel = $(".mimetype-panel", this.element);
	
				titlePrefix =  "Storage Providers";

				data = formatBarChartData(
					 	spm.storageProviderMetrics, 
					 	"storageProviderType", 
					 	"totalSize");
	
				bytes = $(".bytes-graph",entityPanel);
				bytes.graphpanel({title: titlePrefix + " (Size)", total:dc.formatGB(spm.totalSize,2)});

				storageProviderLinkClass = "storage-provider-link";
				//format x axis ticks
				xTickFormatter = function(value){
					var txt; 
					txt = dc.STORAGE_PROVIDER_KEY_MAP[value];
					if(!txt) txt = value;
					return "<span style='font-size:0.8em'><a id='"+value+"'class='"+storageProviderLinkClass+"' href='#'>"+txt+"</a></span>";
				};
				
				
				plotBarChart(
					$(".dc-graph", bytes), 
					data, 
					xTickFormatter,
					function(value){
						return dc.formatGB(value);
					},
					function(xValue, yValue){
						return dc.STORAGE_PROVIDER_KEY_MAP[xValue] + ": " + dc.formatGB(yValue,2);
					}
				);
	
				//files
				data = formatBarChartData(
					 	spm.storageProviderMetrics, 
					 	"storageProviderType", 
					 	"totalItems")
				
				files = $(".files-graph", entityPanel);
				dataTable = this._formatDataTable("storageProviderType", "Storage Providers", spm.storageProviderMetrics);
				files.graphpanel({title: titlePrefix + " (Count)", total: spm.totalItems +" Files", dataTable: dataTable});
	
				 plotBarChart($(".dc-graph", files), 
						 	data, 
						 	xTickFormatter,
						 	function(value){
								return toFixed(value, 0);
							},
							function(xValue, yValue){
								return dc.STORAGE_PROVIDER_KEY_MAP[xValue] + ": " + toFixed(yValue,0);
							}
						);
	
				 this._loadMimetypeGraphs(titlePrefix, spm);

				$("." +storageProviderLinkClass).click(
						function(evt){
							that._fireStorageProviderChanged($(this).attr("id"));
						}
					);
				},
			}
		)
	);	

	
})();


(function(){
	var ONE_DAY  =  1000*60*60*24;

	$.widget("ui.dateslider", {
		options:{
			minDate: /*Date*/ null,
			maxDate: /*Date*/ null,
			lowBound: /*Date*/ null, 
			highBound: /*Date*/ null,
			change: /*Function*/ null,

		},
		_value: {lowBound: /*date*/ null, highBound: /*date*/null},
		_init: function(){
			var that = this;
			var o = this.options;
			var maxValue = this._daysBetween(o.minDate, o.maxDate);
			var lowDayRange = this._daysBetween(o.minDate, o.lowBound);

			this._updateSelectedRangeText(o.lowBound, o.highBound);
			this._updateSelectedRangeValues(o.lowBound, o.highBound);
			
			$(".date-slider", this.element).slider({
				range: true,
				min: 0,
				max: maxValue,
				values: [lowDayRange, maxValue],
				slide: function( event, ui ) {
					var low = that._addDaysAsDate(ui.values[0]), 
						hi =  that._addDaysAsDate(ui.values[1]);
					that._updateSelectedRangeText(low, hi);
				},
				change: function(event, ui){
					var low = that._addDaysAsDate(ui.values[0]), 
						hi =  that._addDaysAsDate(ui.values[1]);
					that._updateSelectedRangeValues(low,hi);
					dc.log("value changed: " + that.value());
					if(o.change){
						o.change(that.element, that.value());
					}
				}
			});
		},
		
		_updateSelectedRangeText: function(lowBound, highBound){
			this._setStartText(lowBound);
			this._setEndText(highBound);
		},

		_updateSelectedRangeValues: function(lowBound, highBound){
			this._value =  {
				lowBound: lowBound,
				highBound: highBound,
				toString: function(){
					return "(low=" + this.lowBound.toLocaleDateString() + "; high: " + 
								this.highBound.toLocaleDateString() + ")";
				},
			};
			return this._value;
		},
		
		value: function(){
			return this._value;
		},

		_daysBetween: function(firstDate, lastDate){
			var msBetween = Math.abs(lastDate.getTime()-firstDate.getTime());
			return Math.round(msBetween/ONE_DAY);
		},

		_addDaysAsDate: function(daysToAdd){
			return new Date(this.options.minDate.getTime()+(ONE_DAY*daysToAdd));
		},

		_setDateValue: function(selector, date){
			$(selector, this.element).html(date.toLocaleDateString());
		},
		
		_setStartText: function(date){
			this._setDateValue(".date-range-start", date);
		},

		_setEndText: function(date){
			this._setDateValue(".date-range-end", date);
		},
	});
})();


var centerLayout,mainContentLayout;

function plotPieChart(element, data, labelValueFormatter){
	var that, labelFormatter;
	that = this;
	
	labelFormatter = function(label, series){
        return label+'<br/>'+labelValueFormatter(series.data[0][1])+'</div>';
    };
    
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
    							return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'+
    								labelFormatter(label, series)
    								+"</div>";
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
    
    var previousSlice = null;
	$(element).unbind("plothover");
	$(element).bind("plothover", function (event, pos, item){
        $("#x").text(pos.pageX);
        $("#y").text(pos.pageY);
 
        if (item) {
            if (previousSlice != item) {
                previousSlice = item;
                $("#tooltip").remove();
                showTooltip(pos.pageX, pos.pageY,
                           labelFormatter(item.series.label, item.series));
            }
        }
        else {
            $("#tooltip").remove();
            previousSlice = null;            
        }
	});;
	
    	
}

function toFixed(value, decimalplaces){
	return new Number(value+"").toFixed(parseInt(decimalplaces));
}

function plotBarChart(element, data, xTickFormatter, yTickFormatter, labelFormatter){
	var ticks = [0,""];
	var i = 0;
	for(i=0; i < data.length; i++){
		ticks.push([i+1,xTickFormatter(data[i].label)]);
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
		         xaxis: {
		     		ticks:ticks,
		    		tickFormatter: xTickFormatter,
		         },
		         yaxis: {
		        	tickFormatter: yTickFormatter,
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
                           labelFormatter(item.series.label, y));
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
	 
	 return simpleSort(data, "label");
}

function simpleSort(array, field){
	 array.sort(function(a,b){
		 if(!field){
			a = a[field];
			b = b[field];
		 }

		 if(a == b) return 0;
		 else if( a > b) return 1;
		 else return -1;
	 });

	return array;
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
	 
	 return simpleSort(data, "label");
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
	 * converts a string like this	'report/storage-report-2011-06-21T15_27_33.xml'
	 * to ms.
	 */
	var convertStorageReportIdToMs = function(storageReportId){
		var pattern = /report\/storage-report-(.*)[.]xml/i;
		var newVal = pattern.exec(storageReportId)[1].replace(/_/g, ':');
		return new Date(newVal).getTime();
	};

	/**
	 * extract date info from reportId and convert to milliseconds
	 * converts a string like this	'report/service-summaries-2011-07.xml'
	 * to ms.
	 */
	var convertServicesReportIdToDate = function(serviceReportId){
		var pattern = /report\/service-summaries-([\d]*)-([\d]*)[.]xml/i;
		var year = pattern.exec(serviceReportId)[1];
		var month = pattern.exec(serviceReportId)[2];
		var date = new Date();
		date.setFullYear(year, month-1, 1);
		return date;
	};
	
	var getLastDayOfMonth = function(date){
		var year = date.getFullYear(), month = date.getMonth(); 
		if(month == 11){
			year += 1;
		}
		month = (month + 1) % 12;
		return new Date(year, month, 0);
	};
	
	var getFirstDayOfMonth = function(date){
		var year = date.getFullYear(), 
			month = date.getMonth(), 
			day = date.getDate();
		
		if(date == 1){
			return date;
		}
		
		if(day == 0){
			month -= 1;
		}

		if(month < 0){
		  month = 11; 	
		}
		
		if(month == 11){
			year -= 1;
		}
		return new Date(year, month, 1);
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
			var spname = dc.STORAGE_PROVIDER_KEY_MAP[evt.storageProvider];
			if(!spname){
				spname = evt.storageProvider;
			}
			
			addToBreadcrumb(spname, function(){
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

	var updateSelectedDate = function(storageReportId){
		$("#report-selected-date").html(formatSelectedDate(storageReportId));
		$("#report-link").attr("href", "/duradmin/storagereport/get?reportId="+storageReportId+"&format=xml");
	};
	
	var initSlider = function(storageReportIds){
		var slider = getSlider();

		
		slider.slider({
			value:storageReportIds.length-1,
			min: 0,
			max: storageReportIds.length-1,
			step: 1,
			slide:function(event,ui){
				updateSelectedDate(storageReportIds[ui.value]);
			},
			change: function( event, ui ) {
				getStorageReport(storageReportIds[ui.value], function(storageReport){
					fireStorageReportChangedEvent(storageReport);
				});
			}
		});
		
		$(document).bind("storagereportchanged", function(evt){
			var storageReport = evt.storageReport;
			updateSelectedDate(storageReport.contentId);
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
	
	var initializeStoragePanel = function(){
		dc.log("initializing storage panel");
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
				var currentId;
				
				dc.done();
				storageReportIds.reverse();
				
				//initialize date controls
				initSlider(storageReportIds);

				getBreadcrumb().bind("rootclicked", function(event){
					seekTo(0);
				});
				
				$("#report-start-range").html(formatChartDate(storageReportIds[0]));
				$("#report-end-range").html(formatChartDate(storageReportIds[storageReportIds.length-1]));
				currentId = getCurrentReportId(storageReportIds);

				updateSelectedDate(currentId)
				//load the most recent report
				getStorageReport(currentId, function(storageReport){
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
	
	
	var serviceReportMap = {};
	var serviceReportList = null;
	var serviceSlider = null;
	var initializeServicesPanel = function(/*element*/ servicesPanel){
		dc.log("initializing services panel");
		//init toggle of deployed and completed
		$("input[name='phase']", servicesPanel).click(function(evt, ui){
			$("#completed-services-panel, #deployed-services-panel", servicesPanel).toggle();
		});

		var servicesList = getServiceListUI(servicesPanel);
		
		//initialize service list
		servicesList.selectablelist(
			{
				clickable: false,
				selectionChanged:function(evt, ui){
					dc.log("selection changed: selection count = " + ui.selectedItems.length);
					buildCompletedServicesList(servicesPanel);
				}
			}
		);

		$("#success-checkbox, #failure-checkbox", servicesPanel).click(function(){
			buildCompletedServicesList(servicesPanel);
		});
		
		//use "live" function so that headers added in the future will be clickable.
		$( ".service .service-header ", servicesPanel).live("click", function(evt){
			$(evt.target).closest(".service").children(".service-body").slideToggle("fast");
			
		});

		//load service report ids
		getServiceReportIds({
			success:function(data){
				dc.done();
				serviceReportList = data;
				$.each(serviceReportList, function(i, serviceReportId){
					serviceReportMap[serviceReportId] = null;
				});
				
				serviceSlider = initializeServiceSlider(servicesPanel, serviceReportList);
				
				buildCompletedServicesList(servicesPanel);
			},
		});
	};
	
	var getServiceListUI = function(servicesPanel){
		return $( "#service-list" , servicesPanel);
	};

	var getSelectedStatuses = function(servicesPanel){
		return {
			success: $("#success-checkbox", servicesPanel).is(":checked"),
			failure: $("#failure-checkbox", servicesPanel).is(":checked"),
		};
	};

	var filterServices = function(serviceSummaries, criteria){
		if(!criteria){
			return serviceSummaries;
		}
		
		var filteredSummaries = serviceSummaries.slice(0);
		var offset = 0;

		$.each(serviceSummaries, function(i, ss){
			var remove = false;
			var success = isSuccessful(ss);
			if(!criteria.status.success){
				if(success){
					remove = true;
				}
			}
			
			if(!remove && !criteria.status.failure){
				if(!success){
					remove = true;
				}
			}
			
			if(!remove && criteria.services && criteria.services.length > 0){
				var inlist = false;
				$.each(criteria.services, function(j, service){
					if(service.id == ss.id){
						inlist = true;
						return false;
					}
				});
				
				if(!inlist){
					remove = true;
				}
			}
			
			if(remove){
				filteredSummaries.splice(i-offset,1);
				offset++;
			}
			
		});
		
		return filteredSummaries;
	};
	
	var buildCompletedServicesList = function(servicesPanel){
		var sliderValue = serviceSlider.dateslider("value");
		dc.debug("slider value:" + sliderValue);
		var serviceList = getServiceListUI(servicesPanel);
		var services  = buildServicesList(serviceReportList,serviceReportMap, sliderValue);
		var uniqueServices  = listUniqueServices(services);
		var selectedServices = serviceList.selectablelist("getSelectedData");
		var filteredServices = filterServices(services , { 
			services: selectedServices,
			status: getSelectedStatuses(servicesPanel)
		});
		
		serviceList.selectablelist("clear");
		$.each(uniqueServices, function(i,service){
			var item = $.fn.create("div"), select = false;
			item.attr("id", service.id);
			item.html(service.name);
			$(selectedServices).each(function(i, selectedService){
				if(selectedService.id == service.id){
					select = true;
					return false;
				}
			});
			serviceList.selectablelist("addItem", item, service, select);
			//todo select any items that were previously selected.
		});

		loadServiceViewer(filteredServices, servicesPanel);
	};

	var loadServiceViewer = function(/*array of servicesummaries*/serviceSummaries, servicesPanel){
		var serviceViewer = $("#completed-services-panel #service-viewer", servicesPanel);
		serviceViewer.children().not(":first").remove();
		var template = serviceViewer.children().first();
		$.each(serviceSummaries, function(i, ss){
			var node = template.clone();
			node.addClass(isSuccessful(ss) ? "successful-service":"failed-service");
			serviceViewer.append(node);
			$(".service-name", node).html(ss.name);
			var completed = getCompletedDate(ss);
			$(".service-completed-date", node).html(completed.toLocaleString());
			$(".service-configuration", node).append(dc.createTable(toArray(ss.configs)));
			$(".service-properties", node).append(dc.createTable(toArray(ss.properties)));
			node.show();
		});
	};
	
	var toArray = function(map){
		var a = [];
		$.each(map, function(name, value){
			a.push([name, value]);
		});
		
		return a;
	};
	
	
	var getCompletedDate = function(serviceSummary){
		return serviceSummary.properties.completed;
	}
	
	var isSuccessful = function (serviceSummary){
		return serviceSummary.id % 6 != 0;
	}
	
	var buildServicesList = function(serviceReportList, servicesReportMap, sliderValue){
		var low = sliderValue.lowBound, high = sliderValue.highBound;
		var servicesArray = [];
		$.each(serviceReportList, function(i,serviceReportId){
			var serviceSummaries, 
				reportLowDate = getFirstDayOfMonth(convertServicesReportIdToDate(serviceReportId)),
				reportHighDate = getLastDayOfMonth(convertServicesReportIdToDate(serviceReportId));

			if(reportLowDate > high){
				return true;
			}
			serviceSummaries = servicesReportMap[serviceReportId];
			if(!serviceSummaries){
				getServiceSummaries(serviceReportId, function(summaries){
					serviceSummaries = summaries;
				});
			}

			$.each(serviceSummaries, function(j,ss){
				var completed = getCompletedDate(ss);
				if(completed >= low && completed <= high )
					servicesArray.push(ss);
			});

			if(reportLowDate < low){
				return false;
			}
		});
		
		return servicesArray;
	};

	var listUniqueServices = function(services){
		var map = {}, i, newlist = [];
		$.each(services, function(i, service){
			map[service.id] = service;
		});
		
		for( i in map){
			newlist.push(map[i]);
		}
		
		return newlist;
	};
	
	var getServiceSummaries = function(serviceReportId, callback){
		
		if(serviceReportMap[serviceReportId]){
			callback(serviceReportMap[serviceReportId]);
			return;
		}
		
		var reportDate = convertServicesReportIdToDate(serviceReportId);
		var firstDay = getFirstDayOfMonth(reportDate);
		var lastDay = getLastDayOfMonth(reportDate);
		var serviceSummaries = [];
		var i = 0;
		var limit = Math.floor(Math.random()*10) + 1;
		var now = new Date().getTime();
		var randomMs = null;
		for(i = 0; i < limit; i++){
			randomMs = Math.round(Math.random()*(lastDay.getTime()-firstDay.getTime()));
			serviceSummaries.push({
				id: i,
				name: "My Service " + i,
				deploymentId: new Date().getTime() % 100000,
				version: "1.1.0",
				configs: {
					"config1": "value1",
					"config2": "value2",
					"config3": "value3",
					
				},
				properties: {
					"completed": new Date(firstDay.getTime()+ randomMs),
					"property2": "value2",
					"property3": "value3",
				},
			});
		}
		
		serviceReportMap[serviceReportId] = serviceSummaries;
		callback(serviceSummaries)
		
	};
	
	var initializeServiceSlider = function(servicesPanel, serviceReportList){
		var firstIndex = serviceReportList.length-1;
		var lastIndex = serviceReportList[0]
		var firstDate = getFirstDayOfMonth(convertServicesReportIdToDate(serviceReportList[firstIndex]));
		var lastMonth = convertServicesReportIdToDate(lastIndex);
		var lowerDate = getFirstDayOfMonth(lastMonth);
		var upperDate = getLastDayOfMonth(lastMonth);
		return $("#service-date-slider", servicesPanel).dateslider({
			minDate: firstDate,
			maxDate: upperDate,
			lowBound: lowerDate, 
			highBound: upperDate,
			change: function(event, ui){
				buildCompletedServicesList(servicesPanel);
			}
		});
	};
	
	var getServiceReportIds = function(callback){
		dc.busy("retrieving service report list");
		callback.success(['report/service-summaries-2011-05.xml', 'report/service-summaries-2011-06.xml', 'report/service-summaries-2011-07.xml'].reverse());
		/*
		dc.ajax({
			url: "/duradmin/completedservicesreport/list",
			type:"GET",
			success: function(result){
				callback.success(result.serviceReportIds);
			},
		    failure: function(textStatus){
				alert("failed to get service report ids");
			},
		});
		*/	
	};
	

	
	var reportMap = {};
	//initialize the tabs
	$("#main-content-tabs").tabs();

	//lazily initialize tabs.
	
	var tabInitState = {
		"tabs-storage": {
			load: initializeStoragePanel,
		},
		"tabs-services": {
			load: initializeServicesPanel,
		},
	};


	$('#main-content-tabs').bind('tabsselect', function(event, ui) {
		var tabId = $(ui.panel).attr("id");
		var initState = tabInitState[tabId];
		if(initState){
			if(!initState.initialized){
				initState.load(ui.panel);
				initState.initialized = true;
			}
		}
	});

	$("#main-content-tabs").tabs("select", 2);
	

});