/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

(function() {
    /**
    * A snapshot display panel
    * created by Daniel Bernstein
    */

    $.widget("ui.snapshot",
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
				title: "Snapshot",
			    space: null,
			}),

			_displayProperties: function(properties, panel){
			    panel.empty();
			    panel.removeClass()
                panel.append("<p class='success'>A snapshot request has been issued.</p>");
			    var list = [];
			    var value = "";
			    var snapshotId;
			    for(i in properties){
			        value = properties[i];
			        if(i == "snapshotId" || 
			            i == "Snapshot ID" || 
			            i == "snapshot-id"){
			          snapshotId = value;
			          value = "<a id='snapshot-id' class='button' title='View Snapshot'>"+snapshotId+"</a>";
			        }

			        list.push([i, value]);
			    }

          panel.append(dc.createTable(list));
          $(panel).find("#snapshot-id").click(function(evt){
              $(document).trigger("navigateToSpace", {
                spaceId: snapshotId,
                snapshot: true
              });
          });
			},

			_createUI: function () {
			    var that = this;
			    var s = that.options.space;
                var form;
			    var panel = $.fn.create("div");
			    panel.empty();
			    if(s.snapshotInProgress){
			        dc.store
			          .GetSnapshotProperties(s.storeId, s.spaceId)
	                  .done(function(properties){
	                    that._displayProperties(properties, panel);
	                  })
	                  .error(function( jqXHR, textStatus, errorThrown ) {
	                    dc.displayErrorDialog(jqXHR, 
	                                          SnapshotErrorMessage.UNAVAILABLE, 
	                                          null, 
	                                          false);
	                  });
			    }

			    return panel;
			},
		}
	)
); 

})();
