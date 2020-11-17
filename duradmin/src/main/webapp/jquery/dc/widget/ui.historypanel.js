/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

/**
 * created by Daniel Bernstein
 */
(function() {
    $.widget("ui.historypanel",$.extend({}, $.ui.expandopanel.prototype, {
        _SUMMARIES_GRAPH_ELEMENT: "summaries-graph",
        _DETAIL_GRAPH_ELEMENT: "detail-graph",

        options: $.extend({}, $.ui.expandopanel.prototype.options, {
            title: "History", 
            spaceId: null,
            storeId: "0",
            open: false
        }),         
        
        _init : function() {
           var that = this;
            $.ui.expandopanel.prototype._init.call(this); //call super init first
            this.element.addClass("history-panel");
            var content = this.getContent();
            
            content.append("<h3 class='summaries-title'>Cumulative Byte and File Counts Over Time</h3>");
            content.append(that._createDiv(this._SUMMARIES_GRAPH_ELEMENT));
            content.append(that._createDiv("summaries-legend"));
            content.append(that._createDiv(this._DETAIL_GRAPH_ELEMENT));
            
            
            this._initSummariesGraph();
        },
        
        
        _createDiv: function(id){
            return $.fn.create("div").attr("id", id);
        },
        
        destroy : function() {
            
        },
        
        _initSummariesGraph: function(){
            var that = this;
            $.when(this._getSummaries())
                .done(function(result){
                    var summaries = result;

                    that.toggle();
                      var content = that.getContent();
                      if(summaries.length == 0){
                        var warning = $.fn.create("p");
                        warning.text("There is no history available for this space.");
                        content.append(warning);
                        $("#" + that._SUMMARIES_GRAPH_ELEMENT).hide();
                        $(".summaries-title").hide();

                      }else{

                        that._initTimeSeriesGraph(summaries);
                      }
                }).fail(function(err){
                    alert("failed to retrieve time series");
                });
        },

        _getSummaries: function(){
            return dc.store.GetStorageStatsTimeline(
                    this.options.storeId, 
                    this.options.spaceId);
        },
        _initTimeSeriesGraph: function(summaries){
            var that = this;
            var min = null;
            var max = 0;
            var sizeData = [];
            var countData = [];
            var summariesGraph = $("#"+this._SUMMARIES_GRAPH_ELEMENT);
            var countMin = null;
            
            $.each(summaries, function(i,summary){
                
                if (summary.timestamp > max){
                    max = summary.timestamp;
                }
                
                if(min == null || min > summary.timestamp){
                    min = summary.timestamp;
                }
                
                if(countMin == null || countMin > summary.objectCount){
                    countMin = summary.objectCount;
                }
                
                
                sizeData.push([summary.timestamp, summary.byteCount]);
                countData.push([summary.timestamp, summary.objectCount]);

            });
            
            var sizeSeries = {
                label: "Bytes",
                lines: { show: true, lineWidth:5},
                points: { show: true},                    
                data: sizeData,    
                yaxis: 1
            };
            
            var countSeries = {
                label: "Files",
                lines: { show: true, lineWidth:5},
                points: { show: true},                    
                data: countData,
                yaxis: 2
            };
            
            var plot = $.plot(summariesGraph, [sizeSeries,countSeries], {
                xaxes: [{
                    color: "#EEE",
                    mode: "time",
                    min: min,
                    max: max
                }],
                yaxes: [
                 {
                    color: "#EEE",
                    minTickSize: 1,
                    show: true, 
                    tickFormatter: function(tickValue, axis){
                        return dc.formatBytes(tickValue);
                    }
                 }, 
                 {
                     color: "#EEE",
                     tickDecimals: 0,
                     minTickSize: 1,
                     show: true, 
                     position: "right",
                     tickFormatter: function(tickValue, axis){
                         return tickValue + " files";
                     },
                     
                 }
                ],
                legend: {
                    show: true, 
                    container: $("#summaries-legend"),
                    noColumns: 7,
                },
                
                grid: { hoverable: true, clickable: true },
            });
            
            var previousPoint = null;
            summariesGraph.bind("plothover", function (event, pos, item) {
                $("#x").text(pos.x.toFixed(2));
                $("#y").text(pos.y.toFixed(2));
                
                if (item) {
                    if (previousPoint != item.dataIndex) {
                        previousPoint = item.dataIndex;
                        
                        $("#tooltip").remove();
                        var x = parseInt(item.datapoint[0]),
                            y = item.datapoint[1].toFixed(0),
                            reportId = item.series.data[item.dataIndex][2];
                        var value = item.seriesIndex == 0 ? dc.formatBytes(y) : y + " Items";
                        
                        dc.chart.showTooltip(item.pageX, item.pageY,
                                     new Date(x).toString("MMM d yyyy")  + " - " +  value);
                    }
                    
                }else {
                    $("#tooltip").remove();
                    previousPoint = null;            
                }
            });
            
            summariesGraph.unbind("plotclick");
            
            if(!that.options.spaceId){
              summariesGraph.bind("plotclick", function (event, pos, item) {
                  if (item) {
                      plot.unhighlight();
                      plot.highlight(item.series, item.datapoint);
                      var datum = item.series.data[item.dataIndex];
                      var date = new Date(datum[0]);
                      var message = "Loading...";
                      dc.busy(message, {modal: true});
  
                      that._loadDetail(that.options.storeId, date)
                          .done(function(){
                              dc.done();
                          });
                  }
              });
              
              if(summaries.length > 0){
                  var summary = summaries[summaries.length-1];
                  this._loadDetail(
                                   that.options.storeId, 
                                   new Date(summary.timestamp));
              }
            }

        },
        
        
        _loadDetail: function(storeId, date){
            var that = this;
            if(that.options.spaceId){
              return;
            }            
            return $.when(dc.store.GetStorageStatsSnapshot(
                    storeId, 
                    date)
             ).done(function(response){
                 var detail = $("#"+that._DETAIL_GRAPH_ELEMENT);
                 detail.empty();
                  var p = dc.chart.loadSpacesMetricsPanel(detail, response, date);
                  p.bind("plotclick", function (event, pos, item) {
                      //alert("clicked space not yet implemented!");
                  });
             }); 
             
        }
    }));
})();
