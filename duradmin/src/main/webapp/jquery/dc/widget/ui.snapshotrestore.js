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
 * ACL Editor: used for displaying and manipulating acls on a space.
 * created by Daniel Bernstein
 */
$.widget("ui.snapshotrestore",
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
				title: "Snapshot Restore",
			    storeId: null,
			}),
			
			_loadSnapshots: function (node){
			    var that = this;
			    dc.store
                .GetSnapshots(that.options.storeId)
                .always(function(){
                    dc.done();
                })
                .done(function(data){
                    var snapshots = data.snapshots;
                    var onclick = function(){
                        node.find("div").removeClass("selected");
                        this.addClass("selected");
                        form.find("#snapshotId").val(this.attr("id"));
                    };
                    
                    $.each(snapshots, function(i,snapshot){
                        var snapshotId = snapshot.snapshotId;
                        var div = $.fn.create("div").attr({"id": snapshotId}).addClass("snapshot").css("display", "block");
                        div.append("<span style='display:block'>" +snapshotId + "</span>");
                        div.append("<span style='display:block'> Status: " +snapshot.status + "</span>");
                        div.append("<p>Description:  " +snapshot.description + "</p>");
                        node.append(div);
                        div.click(onclick);
                    });
                })
                .fail(function( jqXHR, 
                                textStatus, 
                                errorThrown ) {
                    alert("error retrieving list of snapshots: " + 
                           errorThrown + 
                           " - " + textStatus + " - " +
                           jqXHR.responseText);
                });
			},
			
			_createUI: function () {
			    var that = this;
			    var storeId = that.options.storeId;
                var form;
			    var panel = $.fn.create("div");
			    panel.empty();
                var d = $("#snapshot-restore-dialog");
                
                d.dialog({
                    autoOpen: false,
                    show: 'blind',
                    hide: 'blind',
                    height: 350,
                    resizable: false,
                    closeOnEscape:true,
                    modal: true,
                    width:550,
                    buttons: {
                        'Restore': function(){
                            var snapshotId = form.find("#snapshotId").val();

                            $(this).dialog('close');
                            if(!snapshot){
                                alert("Please click on a snapshot to select.");
                                return false;
                            }
                            
                            dc.busy("Requesting snapshot restore...", {modal: true});
                            dc.store
                                .RestoreSnapshot(storeId, snapshotId)
                                .always(function(){
                                    dc.done();
                                })
                                .done(function(properties){
                                    alert("The restore is in process. Status: "
                                            + properties["status"]
                                            + ". The snapshot will be restored to "
                                            + properties["spaceId"]
                                            + "!");
                                })
                                .fail(function( jqXHR, 
                                                textStatus, 
                                                errorThrown ) {
                                    alert("error: " + 
                                           errorThrown + 
                                           "; status=" + 
                                           textStatus);
                                });
                        },
                        Cancel: function() {
                            $(this).dialog('close');
                        }
                    },
                    close: function() {
                    },
                    open: function(){
                        form = $("#snapshot-restore-form");
                        form.clearForm();
                        
                        that._loadSnapshots(form);
                        
                    }
                });
                
                 var btn = $("<button>Restore Snapshot</button>").click(function(e){
                     d.dialog("open");
                 });
                 
		        panel.append(btn);
			    
                return panel;
			},
		}
	)
); 

})();
