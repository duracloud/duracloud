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

			_createUI: function () {
                var that = this;
                var space = that.options.space;
                var form;
                var panel = $.fn.create("div");
                panel.empty();

                var rtmpSwitchHolder = this._createSwitchHolder("rtmpSwitchHolder", "RTMP Streaming");
                panel.append(rtmpSwitchHolder);
                panel.append("<p>RTMP streaming tool tip here</p>")
                // deploy/undeploy switch
                // definition and bindings
                $(".streaming-switch",rtmpSwitchHolder).onoffswitch({
                    initialState : space.streamingEnabled ? "on" : "off",
                    onStateClass : "on left",
                    onIconClass : "checkbox",
                    offStateClass : "right",
                    offIconClass : "x",
                    onText : "On",
                    offText : "Off"
                }).bind("turnOff", function(evt, future) {
                    rtmpSwitchHolder.busy();
                    $.when(dc.store.UpdateSpaceStreaming(space.storeId, space.spaceId, false)).done(function() {
                        future.success();
                    }).always(function() {
                        rtmpSwitchHolder.idle();
                    });
                }).bind("turnOn", function(evt, future) {
                    rtmpSwitchHolder.busy();
                    $.when(dc.store.UpdateSpaceStreaming(space.storeId, space.spaceId, true)).done(function() {
                        future.success();
                    }).always(function() {
                        rtmpSwitchHolder.idle();
                    });
                });

                var hlsSwitchHolder = this._createSwitchHolder("hlsSwitchHolder", "HLS Streaming");
                panel.append(hlsSwitchHolder);
                panel.append("<p>HLS streaming tool tip here</p>")


                $(".streaming-switch",hlsSwitchHolder).onoffswitch({
                    initialState : space.hlsEnabled ? "on" : "off",
                    onStateClass : "on left",
                    onIconClass : "checkbox",
                    offStateClass : "right",
                    offIconClass : "x",
                    onText : "On",
                    offText : "Off"
                }).bind("turnOff", function(evt, future) {
                    hlsSwitchHolder.busy();
                    $.when(dc.store.UpdateSpaceHlsStreaming(space.storeId, space.spaceId, false)).done(function() {
                        future.success();
                    }).always(function() {
                        hlsSwitchHolder.idle();
                    });
                }).bind("turnOn", function(evt, future) {
                    hlsSwitchHolder.busy();
                    $.when(dc.store.UpdateSpaceHlsStreaming(space.storeId, space.spaceId, true)).done(function() {
                        future.success();
                    }).always(function() {
                        hlsSwitchHolder.idle();
                    });
                });


                return panel;
            }
		}
	)
); 

})();
