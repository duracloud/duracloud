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

                var rtmpSwitchControl = $.fn.create("div").attr("id", "rtmpSwitchControl")
                                            .append(this._createSwitchHolder("rtmpSwitchHolder", "RTMP Streaming"))
                                            .append("<div><p>Enables Streaming using RTMP (Real-Time Messaging Protocol for this space.  </p></div>");
                panel.append(rtmpSwitchControl);

                // deploy/undeploy switch
                // definition and bindings
                $(".streaming-switch",rtmpSwitchControl).onoffswitch({
                    initialState : space.streamingEnabled ? "on" : "off",
                    onStateClass : "on left",
                    onIconClass : "checkbox",
                    offStateClass : "right",
                    offIconClass : "x",
                    onText : "On",
                    offText : "Off"
                }).bind("turnOff", function(evt, future) {
                    rtmpSwitchControl.busy();
                    $.when(dc.store.UpdateSpaceStreaming(space.storeId, space.spaceId, false)).done(function() {
                        future.success();
                    }).always(function() {
                        rtmpSwitchControl.idle();
                    });
                }).bind("turnOn", function(evt, future) {
                    rtmpSwitchControl.busy();
                    $.when(dc.store.UpdateSpaceStreaming(space.storeId, space.spaceId, true)).done(function() {
                        future.success();
                    }).always(function() {
                        rtmpSwitchControl.idle();
                    });
                });

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
                    });
                }).bind("turnOn", function(evt, future) {
                    hlsSwitchControl.busy();
                    $.when(dc.store.UpdateSpaceHlsStreaming(space.storeId, space.spaceId, true)).done(function() {
                        future.success();
                    }).always(function() {
                        hlsSwitchControl.idle();
                    });
                });


                return panel;
            }
		}
	)
); 

})();
