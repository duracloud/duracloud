/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

(function() {
    /**
    * A panel for enabling and disabling streaming
    * created by Daniel Bernstein
    */

    $.widget("ui.streaming",
	$.extend({}, $.ui.expandopanel.prototype, 
		{  
	        _listPanel: null, 
	        _init: function(){ 
				$.ui.expandopanel.prototype._init.call(this); //call super init first
				var that = this;
                this.getContent().append(this._createUI());
				
			}, 
			
			destroy: function(){ 
				//tabular destroy here
				$.ui.expandopanel.prototype.destroy.call(this); // call the original function 
			}, 

			options: $.extend({}, $.ui.expandopanel.prototype.options, {
				title: "Streaming",
			    space: null,
			}),

            _createSwitchHolder: function ( elementId, label) {
                var busy = $.fn.create("div").attr("class", "dc-busy").attr("style", "display:none");
                var streamingSwitch = $.fn.create("div").attr("class", "streaming-switch");
                var streamingHolder = $.fn.create("div")
                    .attr("id", elementId)
                    .attr("class", "streaming-switch-holder")
                    .attr("style", "display: inline-block;");

                streamingHolder.append(streamingSwitch)
                               .append(busy)
                               .append("<label>" + label + "</label>")

                return streamingHolder;
            },

            _refresh: function() {
                var that = this;
                dc.store.GetSpace2(that.options.space).success(function(data) {
                    that.options.space = data.space;
                    var contentPane = that.getContent();
                    contentPane.empty();
                    contentPane.append(that._createUI());
                }).fail(function() {
                    alert("Failed to retrieve space.");
                });
            },

			_createUI: function () {
                var that = this;
                var space = that.options.space;
                var form;
                var panel = $.fn.create("div");
                panel.empty();

                var hlsSwitchControl = $.fn.create("div").attr("id", "hlsSwitchControl")
                    .append(this._createSwitchHolder("hlsSwitchHolder", "HLS Streaming"))
                    .append("<div><p>Enables HTTP Live Streaming (HLS) for this space. Note: in order for HLS to work" +
                        " your content must be transcoded specifically for display in HLS compliant viewers. </p></div>");
                panel.append(hlsSwitchControl);

                $(".streaming-switch",hlsSwitchControl).onoffswitch({
                    initialState : space.hlsEnabled ? "on" : "off",
                    onStateClass : "on left",
                    onIconClass : "checkbox",
                    offStateClass : "right",
                    offIconClass : "x",
                    onText : "On",
                    offText : "Off"
                }).bind("turnOff", function(evt, future) {
                    hlsSwitchControl.busy();
                    $.when(dc.store.UpdateSpaceHlsStreaming(space.storeId, space.spaceId, false)).done(function() {
                        future.success();
                    }).always(function() {
                        hlsSwitchControl.idle();
                        that._refresh();
                    });
                }).bind("turnOn", function(evt, future) {
                    hlsSwitchControl.busy();
                    $.when(dc.store.UpdateSpaceHlsStreaming(space.storeId, space.spaceId, true)).done(function() {
                        future.success();
                    }).always(function() {
                        hlsSwitchControl.idle();
                        that._refresh();
                    });
                });

                var props = [];
                if (space.properties.hlsStreamingHost) {
                    props.push([ 'HLS Host', space.properties.hlsStreamingHost ]);
                }

                if (space.properties.hlsStreamingType) {
                    props.push([ 'HLS Type', space.properties.hlsStreamingType ]);
                }

                if(props.length > 0){
                    panel.append(dc.createTable(props));
                }

                return panel;
            }
		}
	)
); 

})();
