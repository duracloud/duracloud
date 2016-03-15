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
            $.ui.expandopanel.prototype._init.call(this); //call super init first
            this.element.addClass("history-panel");
            this.getContent().append("<h6>Cumulative Byte and File Counts Over Time</h6>");

            this.getContent().append(this._createDiv(this._SUMMARIES_GRAPH_ELEMENT));
            this.getContent().append(this._createDiv("summaries-legend"));
            this.getContent().append(this._createDiv(this._DETAIL_GRAPH_ELEMENT));

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
                    if(summaries.length > 0){
                        that.toggle();
                    }

                    that._initTimeSeriesGraph(summaries);
                }).fail(function(err){
                    alert("failed to retrieve time series");
                });
        },

        _getSummaries: function(){
            return dc.store.GetStorageStats(
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
            /*
            summariesGraph.unbind("plotclick");
            */
            /*
            summariesGraph.bind("plotclick", function (event, pos, item) {
                if (item) {
                    plot.unhighlight();
                    plot.highlight(item.series, item.datapoint);
                    var datum = item.series.data[item.dataIndex];
                    var date = new Date(datum[0]);
                    var reportId = datum[2];
                    var message = "Loading...";
                    dc.busy(message, {modal: true});

                    that._loadDetail(that.options.storeId, that.options.spaceId, reportId, date)
                        .done(function(){
                            dc.done();
                        });
                }
            });
            */
            /*
            if(summaries.length > 0){
                var summary = summaries[0];
                this._loadDetail(
                        that.options.storeId, 
                        that.options.spaceId, 
                        new Date(summary.timestamp));
            }
            */
        },
        
        
        _loadDetail: function(storeId, spaceId, date){
            var that = this;
            /*
            return $.when(dc.store.GetSpaceStats(
                    storeId, 
                    spaceId, 
                    date,
                    date)
             ).done(function(response){
                 var detail = $("#"+that._DETAIL_GRAPH_ELEMENT);
                 detail.empty();
                 var metrics = response.metrics;
                 metrics.date = date;
                 if(!spaceId){
                    var p = dc.chart.loadSpacesMetricsPanel(detail, metrics);
                    p.bind("plotclick", function (event, pos, item) {
                        //alert("clicked space not yet implemented!");
                    });
                    
                 }else{
                     $.each(response.metrics.spaceMetrics, function(i,sm){
                         if(spaceId == sm.spaceName){
                             metrics = sm;
                             metrics.reportId = reportId;
                             metrics.date = date;
                             return false;
                         }
                     });
                 }
             }); 
             */
        }
    }));
})();
