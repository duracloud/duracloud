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
$.widget("ui.chronopolis",
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
				title: "Chronopolis", 
			    space: null,
			    readOnly: false,
			}),
			
			_displayProperties: function(properties, panel){
			    panel.empty();
			    panel.removeClass()
                panel.append("<p class='success'>A snapshot request has been issued.</p>");
			    var list = [];
			    
			    for(i in properties){
			        list.push([i, properties[i]]);
			    }
			    
                panel.append(dc.createTable(list));
			},
			
			_createUI: function () {
			    var that = this;
			    var s = that.options.space;
                var form;
			    var panel = $.fn.create("div");
			    panel.empty();
			    if(!this.options.readOnly){
                    var d = $("#chronopolis-dialog");
                    
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
                            'Create': function(){
                                var data = form.serialize();
                                data += "&spaceId="+ s.spaceId
                                        + "&storeId="+ s.storeId;
                                
                                $(this).dialog('close');
                                dc.busy("Requesting space snapshot...", {modal: true});
                                dc.store
                                    .CreateSnapshot(data)
                                    .always(function(){
                                        dc.done();
                                    })
                                    .done(function(properties){
                                        alert("The snapshot is being generated!");
                                        that._displayProperties(properties,panel);
                                        $(document).trigger("staleSpace", that.options.space);
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
                            form = $("#snapshot-properties-form");
                            form.clearForm();
                        }
                    });

                    var btn = $("<button>Create Snapshot</button>").click(function(e){
                         d.dialog("open");
	                 });
	                 
			        panel.append(btn);
	                panel.append("<span class='info'>Invoking this feature will render this space read only.</span>");
			    }else{
			        dc.store
			          .GetSnapshotProperties(s.storeId, s.spaceId)
	                  .done(function(properties){
	                    that._displayProperties(properties, panel);
	                  })
	                  .fail(function( jqXHR, textStatus, errorThrown ) {
	                                          alert("error: " + 
                                                     errorThrown + 
                                                     "; status=" + 
                                                     textStatus);
	                  });
			    }
			    
                return panel;
			},
		}
	)
); 

})();
