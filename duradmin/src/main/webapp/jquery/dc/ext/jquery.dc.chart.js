/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

/**
 * 
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 */

var dc;
(function(){
        ///////////////////////////////////////////////////////////////////////
        ////duracloud chart utils
        ///////////////////////////////////////////////////////////////////////
        if(!dc){
            dc = {};
        }

        var chart = {};

        chart.plotPieChart = function(element, data, labelValueFormatter){
            var labelFormatter;
            
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
                                        return "<div style='max-width:50px; "+
                                                    "font-size:7pt; "+
                                                    "text-align:center; "+
                                                    "padding:2px; "+
                                                    "color:white; "+
                                                    "overflow:hidden'>"+
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
                        dc.chart.showTooltip(pos.pageX, pos.pageY,
                                   labelFormatter(item.series.label, item.series));
                    }
                }
                else {
                    $("#tooltip").remove();
                    previousSlice = null;            
                }
            });
        };

        chart.toFixed = function (value, decimalplaces){
            return new Number(value+"").toFixed(parseInt(decimalplaces));
        };

        chart.plotBarChart = function (element, data, xTickFormatter, yTickFormatter, labelFormatter){
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
                        var y = item.datapoint[1].toFixed(2);
                        
                        dc.chart.showTooltip(item.pageX, item.pageY,
                                   labelFormatter(item.series.label, y));
                    }
                }
                else {
                    $("#tooltip").remove();
                    previousPoint = null;            
                }
            });
        };

        chart.showTooltip = function(x, y, contents) {
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
        };

        /**
         * converts an array of arbitray objects into something
         * flot pie chart can read.
         */
        chart.formatPieChartData = function(inputArray, label, value){
             var data = new Array();
             $.each(inputArray,function(i){
                 data.push({
                     label: inputArray[i][label],
                     data: inputArray[i][value],
                 });
             });
             
             return dc.chart.simpleSort(data, "label");
        };
        
        chart.sum = function(inputArray){
           var result = 0;
           $.each(inputArray, function(i,val){
               result += val.data;
           });
           
           return result;
        };

        chart.simpleSort = function(array, field){
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
        };

        chart.formatBarChartData = function(inputArray, label, value){
             var data = new Array();
             $.each(inputArray,function(i){
                 data.push({
                     bars:{show:true},
                     label: inputArray[i][label],
                     data:  [[(i+1),0],[(i+1),inputArray[i][value]]],
                 });
             });
             
             return dc.chart.simpleSort(data, "label");
        };
        
        chart.formatDataTable = function(firstColumnFieldName, firstColumnDisplayName,  metrics){
            var dt = {};
            var defaultCssClass = "label number";
            var formatGB = function(bytes){
                return dc.formatGB(bytes, 3, false);
            };
            
            dt.columnDefs = 
                [
                 {name: firstColumnDisplayName}
                ,{name: "Gigabytes",cssClass:defaultCssClass, formatter: formatGB}
                ,{name: "Files",cssClass:defaultCssClass}
                ];
            dt.rows = [];
            $.each(metrics, function(i,val){
                dt.rows.push([val[firstColumnFieldName], val["byteCount"], val["objectCount"]]);
            });
            
            return dt;
        };

        chart.createDataButton = function(dataTable, title){
           return  $.fn.create("button")
            .append("<i class='pre data-table'></i>")
            .append("Data")
            .click(function(evt){
                var d, table;
                d = $.fn.create("div");
                d.append("<h3>"+title+"</h3>");
                table = dc.createTable(dataTable.rows, dataTable.columnDefs);
                $(table).addClass("tablesorter");
                $(table).tablesorter({sortList: [[0,0],] });
                d.append(table);
                d.dialog({
                    autoOpen: true,
                    height: 300,
                    width: 500,
                    modal: false,
                    buttons: {
                        "Close":function(){
                            d.dialog("close");
                        }
                    },
                });
            });            
        };
        
        chart.loadSpacesMetricsPanel = function(panel, metrics, date){
            var p = $.fn.create("div");
            panel.append(p);
            p.piemetricspanel({metrics: metrics, title:"Spaces as of ", field:"spaceId", date: date});
            return p;
        };

        /*
        chart.loadMimetypeMetricsPanel = function(panel, metrics, admin){
            var p = $.fn.create("div");
            panel.append(p);
            p.piemetricspanel({metrics: metrics.mimetypeMetrics, title:"Mime Types as of ", field:"mimetype", admin:admin, date: metrics.date});
            return p;
        }
        */

        //assign to the package.
        dc.chart = chart;

})();    

/**
 * A panel with a title, a graph, an associated data table.
 */
(function(){
    $.widget("ui.graphpanel", {  
        options: {
            title: "Placeholder Title",
            dataDialogTitle: "Placeholder Data Dialog Title",
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
            $(this.element).append("<div class='header'><span class='title'></span><div class='button-panel'></div></div>");
            $(this.element).append("<div class='dc-graph'></div>");
            $(this.element).append("<div>Total: <span class='total'></span></div>");

            this._title(this.options.title);
            this._total(this.options.total);

            if(this.options.dataTable){
                var dataBtn;
                
                dataBtn = dc.chart.createDataButton(this.options.dataTable);
                            
                this._addButton(dataBtn);
            }
        }, 
        
        _title: function(title){
            $(".title", this.element).html(title);
        },

        _total: function(total){
            $(".total", this.element).html(total);
        },

        _addButton: function(button){
            $(".button-panel", this.element).append(button);
        },

    });
    
    $.widget("ui.piemetricspanel", {
        options: {
            title: "Placeholder",
            metrics: null,
            field: null,
            date: null,
            admin: false,
           
        },
        
        _init: function(){
            var data, dataTable,bytes, files;
            var field = this.options.field;
            var metrics = this.options.metrics;
            var admin = this.options.admin;
            var date = this.options.date;
            var formattedDate = date.toString("MMM d, yyyy");
            
            var header = $("<h4><span>"+this.options.title+ " " + formattedDate+"</span></h4");
            dataTable = dc.chart.formatDataTable(field, "File Types", metrics);
            header.append(dc.chart.createDataButton(dataTable, "File Types"));
            if(admin){
                var link = $("<a href='javascript:void(0)' class='float-r'>Show All Spaces</a>");
                header.append(link);
            }

            this.element.append(header);
            bytes = $.fn.create("div").addClass("dc-graph");
            var  bytesHolder = $.fn.create("div").addClass("dc-graph-holder");
            bytesHolder.append(bytes);
            bytesHolder.append("<h6>File Size</h6>");

            data = dc.chart.formatPieChartData(
                    metrics, 
                    field, 
                    "byteCount");
            
            var bytesTotal = dc.chart.sum(data);
            bytesHolder.append("<p>Total: "+dc.formatGB(bytesTotal,2)+"</p>");
            this.element.append(bytesHolder);

            dc.chart.plotPieChart(
                bytes, 
                data, 
                function(x){ return dc.formatGB(x,2);}
            );

            files = $.fn.create("div").addClass("dc-graph");
            var  filesHolder = $.fn.create("div").addClass("dc-graph-holder");
            filesHolder.append(files);
            filesHolder.append("<h6>File Count</h6>");

            data = dc.chart.formatPieChartData(
                    metrics,
                    field, 
                    "objectCount");

            var fileTotal = dc.chart.sum(data);
            filesHolder.append("<p>Total: "+fileTotal+"</p>");
            this.element.append(filesHolder);

            dc.chart.plotPieChart(files, data, 
                    function(x){ return x;}         
            );

        }
    });
})();