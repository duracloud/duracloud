/**
 * Dashboard Manager
 * 
 * @author Daniel Bernstein
 */




/**
 * The bread crumb widget
 */
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

})();


/**
 * The graph panels
 */
(function(){
    /**
     * The base graph panel widget
     */
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
		     return dc.chart.formatDataTable(firstColumnFieldName, firstColumnDisplayName,  metrics);
		 },
		 
		_loadMimetypeGraphs:function(titlePrefix, metrics){
			var data, dataTable, mimetypeBytes, mimetypeFiles, mimetypePanel;
		 	mimetypePanel = $(".mimetype-panel", this.element);
		 	dataTable = this._formatDataTable("mimetype", "File Types", metrics.mimetypeMetrics);
			data = dc.chart.formatPieChartData(
					metrics.mimetypeMetrics, 
					"mimetype", 
		 			"totalSize");
			mimetypeBytes = $(".bytes-graph",mimetypePanel);
			mimetypeBytes.graphpanel({title: titlePrefix+ " by File Type (Size)",  total:dc.formatGB(metrics.totalSize,2)});
			dc.chart.plotPieChart(
				$(".dc-graph", mimetypeBytes), 
				data, 
				function(x){ return dc.formatGB(x,2);}
			);
	
			//files
			data = dc.chart.formatPieChartData(
					metrics.mimetypeMetrics, 
				 	"mimetype", 
				 	"totalItems");
			mimetypeFiles = $(".files-graph", mimetypePanel);
			mimetypeFiles.graphpanel(
				{
					title: titlePrefix+ " by File Type (Count)", 
					dataDialogTitle: titlePrefix, 
					total: metrics.totalItems +" Files", 
					dataTable: dataTable});
			
			dc.chart.plotPieChart($(".dc-graph", mimetypeFiles), data, 
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
	
	/**
	 * The storage provider widget
	 */
	$.widget("ui.storageprovider", 
		$.extend({}, $.ui.basegraphpanel.prototype, {
			_storageProvider: null,
			_init: function(){
				var that = this;
				
				this._entityColumnTitles[0] = "Space";
				
				$.ui.basegraphpanel.prototype._init.call(this); 	

				$(document).bind("storageproviderchanged", function(evt){
					try{
					 that._storageProvider = evt.storageProvider;
					 that._reload();
					}catch(err){
						dc.log("error:" + err);
					}
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
				 var list = this._getStorageMetrics().storageProviderMetrics;
				 $.each(list, function(i,sm){
					 if(sm.storageProviderType == that._storageProvider){
						 spm =  sm;
						 return false;
					 }
				 });

				 if(spm == null && list.length > 0){
					spm = list[0];
					that._storageProvider = spm.storageProviderType;
				 }
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
				data = dc.chart.formatPieChartData(
						spm.spaceMetrics, 
						"spaceName", 
			 			"totalSize");

				bytes = $(".bytes-graph",entityPanel);
				bytes.graphpanel({title: titlePrefix + " (Size)", total:dc.formatGB(spm.totalSize,2)});
				dc.chart.plotPieChart(
					$(".dc-graph", bytes), 
					data, 
					function(x){ return dc.formatGB(x,2);}
				);

				//files
				data = dc.chart.formatPieChartData(
						spm.spaceMetrics, 
					 	"spaceName", 
					 	"totalItems");

			 	dataTable = this._formatDataTable("spaceName", "Spaces", spm.spaceMetrics);
				files = $(".files-graph", entityPanel);
				files.graphpanel({title: titlePrefix + " (Count)", 					
									dataDialogTitle: titlePrefix, 
									total:spm.totalItems, 
									dataTable: dataTable});
				dc.chart.plotPieChart($(".dc-graph", files), data, 
						 	function(x){ return x;}		 	
				);

				this._loadMimetypeGraphs("Spaces", spm);
			},
		})
	);


	/**
	 * The space graph widget
	 */
	$.widget("ui.spacegraphpanel", 
		$.extend({}, $.ui.storageprovider.prototype, 
			{
				_spaceName: null,

				_init: function(){
					$.ui.storageprovider.prototype._init.call(this); 					
					var that = this;
					
					$(document).bind("spacechanged", function(evt){
						try{
						 that._spaceName = evt.spaceName;
						 that._reload();
						}catch(err){
							dc.log("error: " + err);
						}
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

	/**
	 * The storage summary panel widget
	 */
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
			
			clickFirstStorageProvider: function(){
				this._fireStorageProviderChanged(
						this._storageReport.storageMetrics.storageProviderMetrics[0].storageProviderType);
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

				data = dc.chart.formatBarChartData(
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
				
				
				dc.chart.plotBarChart(
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
				data = dc.chart.formatBarChartData(
					 	spm.storageProviderMetrics, 
					 	"storageProviderType", 
					 	"totalItems")
				
				files = $(".files-graph", entityPanel);
				dataTable = this._formatDataTable("storageProviderType", "Storage Providers", spm.storageProviderMetrics);
				files.graphpanel(
						{
							title: titlePrefix + " (Count)", 
							dataDialogTitle: titlePrefix, 
							total: spm.totalItems +" Files", dataTable: dataTable
						});
				
				 dc.chart.plotBarChart($(".dc-graph", files), 
						 	data, 
						 	xTickFormatter,
						 	function(value){
								return dc.chart.toFixed(value, 0);
							},
							function(xValue, yValue){
								return dc.STORAGE_PROVIDER_KEY_MAP[xValue] + ": " +  dc.chart.toFixed(yValue,0);
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

/**
 *  A date slider component that wraps a jquery ui slider
 *  and updates start, end, and current value 
 *  labels on slide and emits change events with 
 *  a date range instead of integer values. 
 */
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

            $(".date-slider", this.element).addClass("dc-slider");

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
					return "(low=" + this.lowBound.toUTCString() + "; high: " + 
								this.highBound.toUTCString() + ")";
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
			var md = this.options.minDate;
			var beginningOfDayMs = Date.UTC(md.getUTCFullYear(),md.getUTCMonth(), md.getUTCDate(), 0,0,0,0);
			return new Date(beginningOfDayMs+(ONE_DAY*daysToAdd));
		},

		_setDateValue: function(selector, date){
			$(selector, this.element).html(date.getUTCMonth()+1 +"/" + date.getUTCDate() +"/"+ date.getUTCFullYear());
		},
		
		_setStartText: function(date){
			this._setDateValue(".date-range-start", date);
		},

		_setEndText: function(date){
			this._setDateValue(".date-range-end", date);
		},
	});
})();


$(function() {
	
	 var getScrollerApi = function(){
		 $(".scrollable").scrollable();
		 return $(".scrollable").data("scrollable");
	 };
	
	 var seekTo = function(index){
		 getScrollerApi().seekTo(index, 500);
	 };
	 
	var formatChartDate = function(/*string*/reportId){
		return new Date(convertStorageReportIdToMs(reportId))
					.toString('MMM-dd-yyyy');
	};
	
	var formatSelectedDate = function(/*string*/reportId){
		return new Date(convertStorageReportIdToMs(reportId))
					.toString('MMM-dd-yyyy');
	};

	/**
	 * extract date info from reportId and convert to milliseconds
	 * converts a string like this	'report/storage-report-2011-06-21T15_27_33.xml'
	 * to ms.
	 */
	var convertStorageReportIdToMs = function(storageReportId){
		var pattern = /report\/storage-report-(.*)[.]xml/i;
		var newVal = pattern.exec(storageReportId)[1].replace(/_/g, ':');

	     // Chrome and Firefox parse the dates properly, but safari chokes:
	     // http://stackoverflow.com/questions/4310953/invalid-date-in-safari
	     // Safari doesn't recognize:
	     //"So, it seems that YYYY-MM-DD is included 
	     // in the standard, but for some reason, Safari doesn't support it."
	     // Using DateJs to solve this problem. Another reason to use dojo btw.
	 	return Date.parse(newVal).getTime();
	};

	
	var getLastDayOfMonth = function(date){
		var year = date.getUTCFullYear(), month = date.getUTCMonth(); 
		if(month == 11){
			year += 1;
		}
		month = (month + 1) % 12;
		return new Date(Date.UTC(year, month, 0, 23, 59, 59));
	};
	
	var getFirstDayOfMonth = function(date){
		var year = date.getUTCFullYear(), 
			month = date.getUTCMonth(), 
			date = date.getUTCDate();
		return new Date(Date.UTC(year, month, 1));
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
		});

		bc.bind("rootclicked", function(event){
					seekTo(0);
				});
		
		return bc;
		
	};

	var getCurrentReportId = function(storageReportIds){
		return storageReportIds[getSliderIndex()];
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
	
	var initSlider = function(reportMap,storageReportIds){
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
				getStorageReport(reportMap,storageReportIds[ui.value], function(storageReport){
					fireStorageReportChangedEvent(storageReport);
				});
			}
		});
		
		
		$(document).bind("storagereportchanged", function(evt){
			var storageReport = evt.storageReport;
			updateSelectedDate(storageReport.reportId);
		});
		
		return slider;
	};
	
	var fireStorageReportChangedEvent = function(storageReport){
		var event = jQuery.Event("storagereportchanged");
		event.storageReport = storageReport;
		$(document).trigger(event);		
	};

	var getStorageReport = function(reportMap, storageReportId, callback){
		var report = reportMap[storageReportId];
		if(report){
			if(callback){
				callback(report);
			}
		}else{
			dc.busy("Preparing charts...", {modal:true});
			dc.store.GetStorageReport(storageReportId)
			        .success(function(result){
	                    var storageReport = result.storageReport;
	                    reportMap[storageReportId] = storageReport; 
	                    callback(storageReport);
			        }).fail(function(){
			            alert("failed to get storage report " + storageReportId);
			        }).always(function(){
			            dc.done();
			        });
		}
	};
		
    var getStorageReportIds = function(callback){
        return dc.store.GetStorageReportIds(callback);
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
	
	var initializeStoragePanel = function(storagePanel){
	     var reportMap = {};

	    dc.log("initializing storage panel");
		
		initGraphSwitcher();
		initBreadcrumb();
		initStorageProviderView();
		initStorageProvidersView();
		initSpaceView();

		$("#tabs-storage .back-link").click(function(){
			getBreadcrumb().breadcrumb("up");
		});

		dc.busy("Loading reports...", {modal: true});

		getStorageReportIds(
		{
			success: function(storageReportIds){
				var currentId;
				
				dc.done();
				storageReportIds.reverse();
				
				//initialize date controls
				initSlider(reportMap,storageReportIds);

				$("#report-start-range").html(formatChartDate(storageReportIds[0]));
				$("#report-end-range").html(formatChartDate(storageReportIds[storageReportIds.length-1]));
				currentId = getCurrentReportId(storageReportIds);

				updateSelectedDate(currentId)
				//load the most recent report
				getStorageReport(reportMap, currentId, function(storageReport){
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
	
	var getStorageSummary = function(){
		return $("#storage-summary");
	};
	
	var isSummaryShowing = function(){
		return getScrollerApi().getIndex() == 0;
	};
	
	var initStorageProvidersView = function(){
		var sp = getStorageSummary();
		sp.summarypanel();
		$(document).bind("storageproviderchanged", function(evt){
			seekTo(1);
		})

		//if there's only one storage provider, display spaces
		//view for that storage provider.
		$(document).bind("storagereportchanged", function(evt){
			var spm = evt.storageReport.storageMetrics.storageProviderMetrics;
			if(spm.length == 1){
				if(isSummaryShowing()){
					sp.summarypanel("clickFirstStorageProvider");
				}
			};
		});		
	};


	
	var addReportOverlayClickListener = function(node){
        $(".report-link",node).each(function(i,item){
            var link = $(item);
            dc.reportOverlayOnClick(link, link.attr("href"));
        });
	};
	
	var postProcessProperties = function(props){
        for(i in props){
            if (i == REPORT_KEY || i == ERROR_REPORT_KEY){
                props[i] = "<a class='report-link' href='"+props[i]+"'>"+props[i]+"</a>";
            }
        }
	};
	
	/**
	 * converts a map into a two-dimensional array
	 */
	var toArray = function(map){
		var a = [];
		$.each(map, function(name, value){
			a.push([name, value]);
		});
		
		return a;
	};
	
	var convertDateStringToUTC = function(dateString){
		var d = Date.parse(dateString);
		
		return new Date(Date.UTC(
						d.getFullYear(), 
						d.getMonth(), 
						d.getDate(), 
						d.getHours(), 
						d.getMinutes(), 
						d.getSeconds()));
	};
	
	
	/**
	 * main routine for initializing page
     */
	var initPage = function(){
	    var centerLayout,mainContentLayout;

	    $('#page-content').layout();
	    
	    centerLayout = $('#page-content').layout({
	    center__paneSelector:   "#main-content-panel"
	    });
	    
	    mainContentLayout = $('#main-content-panel').layout({
	        // minWidth: 300 // ALL panes
	            north__size:            90  
	        ,   north__paneSelector:     ".north"
	        ,   north__resizable:   false
	        ,   north__slidable:    false
	        ,   north__spacing_open:            0           
	        ,   north__togglerLength_open:      0           
	        ,   north__togglerLength_closed:    0           
	        ,   center__paneSelector:   ".center"
	        });
	    
	    //initialize the tabs
	    $("#main-content-tabs").tabs();
	    $("#main-content-tabs").tabs("select", 1);

	    //lazily initialize tabs.
	    var tabInitState = {
	        "tabs-storage": {
	            load: initializeStoragePanel,
	        }
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

	    $("#main-content-tabs").tabs("select", 0);
	};
	
	//it all starts here (at the end) :).
	initPage();
});
