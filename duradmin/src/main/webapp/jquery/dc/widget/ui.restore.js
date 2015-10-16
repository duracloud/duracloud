/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

/**
 * A stack layout control:  a vertical stack of div.
 * created by Daniel Bernstein
 */



(function() {
/**
 * Displays information about a space that was restored from a snapshot.
 * created by Daniel Bernstein
 */
$.widget("ui.restore",
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
				title: "Restore",
			    storeId: null,
			    restoreId: null,
			}),
			
			_createUI: function () {
			    var that = this;
			    var storeId = that.options.storeId;
                var form;

                dc.store.GetRestore(that.options.storeId,
                                    that.options.restoreId)
                .always(function(){
                    dc.done();
                })
                .done(function(restore){
                    
                    var data = [];
                    data.push(["Status", restore.status]);
                    data.push(["Status Message", restore.statusText]);
                    data.push(["Start Date", (restore.startDate ? new Date(restore.startDate).toString():"")]);
                    data.push(["End Date", (restore.endDate ? new Date(restore.endDate).toString():"")]);
                    data.push(["Expiration Date", (restore.expirationDate ? new Date(restore.expirationDate).toString():"")]);
                    data.push(["Snaphshot ID", restore.snapshotId]);
                    
                    var table = dc.createTable(data);
                    
                    that.getContent().append(table);
                })
                .error(function( jqXHR, 
                                textStatus, 
                                errorThrown ) {
                  dc.displayErrorDialog(jqXHR, SnapshotErrorMessage.UNAVAILABLE, null, false);

                });


			},
		}
	)
); 

})();
