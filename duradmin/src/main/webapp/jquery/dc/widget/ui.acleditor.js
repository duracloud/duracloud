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

$.widget("ui.stacklayout",
    {  
        _stack: null,
        
        _activePanel: null,
        
        _init: function(){ 
            var that = this;
        }, 
        
        destroy: function(){ 
        
        }, 
        
        add: function(panel){
            if(!this._stack){
                this._stack = [];
            }
            
            if(!panel){
                throw "must specify child panel";
            }

            this._stack.push(panel);
            this.element.append(panel);
            
            if(this._stack.length == 1){
                this._activePanel = panel;
            }else{
                panel.hide();
            }
        },

        activate: function(panel){
            var that = this;
            var stack = this._stack;
            for(i in stack){
                if(stack[i] === panel && stack[i] !== this._activePanel){
                    $(that._activePanel).trigger("deactivating");
                    stack[i].trigger("activating");
                    this._activePanel.fadeOut("fast", function(){
                        that._activePanel.trigger("deactivated");
                        stack[i].fadeIn("fast", function(){
                            stack[i].trigger("activated");
                        });
                    });
            
                    this._activePanel = panel;
                    return;
                }
            }
        },
    }
);

/**
 * Like a panel that consists of a button bar and a content area.
 */
$.widget("ui.controlpanel",
    {  
        _header: null,
        _content: null,
        _init: function(){ 
            var that = this;
            that.element.addClass('dc-controlpanel');
            this._header = $.fn.create("div").addClass('dc-controlpanel-header');
            this._content = $.fn.create("div")
            this.element.bind("activated", function(){that._activate()});
            this.element.bind("deactivated", function(){that._deactivate()});

            var form = $.fn.create("form").attr("onsubmit", "return false;");

            form.append(that._header);
            form.append(that._content);
            this.element.append(form);
            this._header.append($("<div class='dc-expando-status dc-busy'></div>").hide());
        }, 

        _busy: function(){
            $(".dc-busy",this.element).show();
        },
        
        _idle: function(){
            $(".dc-busy",this.element).hide();
        },
        
        _activate: function(){ 

        }, 

        _deactivate: function(){ 

        }, 

        destroy: function(){ 
        
        }, 
        
        content: function(){
            return this._content;
        },
        
        addButton: function(btnDef){
            var that = this;
            var btn = $.fn.create("button");

            if(btnDef.id)  btn.attr("id", btnDef.id);

            if(btnDef.iconClass) {
                btn.append("<i class='pre " + btnDef.iconClass + "'></i>");
            }

            btn.append(btnDef.text);
            
            if(btnDef.disabled){
                btn.attr("disabled", "true");
            }
            
            btn.click(function(evt){
                if(btnDef.click){
                   btnDef.click(evt);
                }else{
                    that.element.trigger(btnDef.id ? btnDef.id : btnDef.text);
                }
                
                evt.stopPropagation();
                return false;
            });
            
            
            this._header.append(btn);
            return btn;
        },
    }
);

/**
 * A save panel control panel.
 */

$.widget("ui.savecancelpanel",
        $.extend({}, $.ui.controlpanel.prototype, 
        {  
            _init: function(){ 
                $.ui.controlpanel.prototype._init.call(this); //call super init first
                var that = this;

                var saveButtonText = this.options.saveButtonText;
                if(!saveButtonText) saveButtonText = "Save";
                var saveButton = that.addButton({
                    id:"save", 
                    text: saveButtonText, 
                    disabled: true,
                    click: function(evt){ 
                        that._fireSaveStart();
                        that._save();
                        return false;
                    }
                });
                
                var cancelButton = this.addButton({
                    id:"cancel", 
                    text:"Cancel", 
                    disabled: false,
                });
                
                $(this.element).bind("changed", function(){
                    saveButton.disable(false);
                });

                $(this.element).bind("savestart", function(){
                    that._busy();
                    saveButton.disable(true);
                    cancelButton.disable(true);
                });

                $(this.element).bind("unchanged savesuccess", function(){
                    saveButton.disable(true);
                    cancelButton.disable(false);
                    that._idle();
                });

                $(this.element).bind("savefailure", function(){
                    saveButton.disable(false);
                    cancelButton.disable(false);
                    that._idle();
                    alert("save operation failed");

                });

                $(this.element).bind("savefailure", function(){
                });

                

            }, 

            _fireChanged: function(){
                this.element.trigger("changed");
            },

            _fireUnchanged: function(){
                this.element.trigger("unchanged");
            },

            _fireSaveStart: function(){
                this.element.trigger("savestart");
            },

            _fireSaveSuccess: function(){
                this._fireUnchanged();
                this.element.trigger("savesuccess");
            },

            _fireSaveFailure: function(){
                this._fireUnchanged();
                this.element.trigger("savefailure");
            },

            _save: function (){
                this._fireSaveSuccess();
            },
            
            destroy: function(){ 
                $.ui.controlpanel.prototype.destroy.call(this); //call super init first
            }, 
        }
    )
);

(function() {
/**
 * ACL Editor: used for displaying and manipulating acls on a space.
 * created by Daniel Bernstein
 */
$.widget("ui.acleditor",
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
				title: "Permissions", 
			    space: null,
			}),

			_createUI: function () {
			    var stack, 
			        listPanel,
			        addPanel,
			        editPanel;
			    
                stack = $.fn.create("div").stacklayout();
                
                //define list pane
                this._listPanel =  $.fn.create("div").aclreadonlypanel({space:this.options.space});
                listPanel = this._listPanel;

                addPanel = $.fn.create("div").acladdpanel({space:this.options.space});
                editPanel = $.fn.create("div").acleditpanel({space:this.options.space});

                stack.stacklayout("add", listPanel);
                stack.stacklayout("add", addPanel);
                stack.stacklayout("add", editPanel);

                //add  listPane button handlers
                listPanel.bind("add", function(){
                    addPanel.acladdpanel("load");
                    stack.stacklayout("activate", addPanel);
                });

                listPanel.bind("edit", function(){
                    var acls = listPanel.aclreadonlypanel("acls");
                    editPanel.acleditpanel("acls", acls);
                    stack.stacklayout("activate", editPanel);

                });


                //bind add/edit panel buttons
                $(addPanel).add(editPanel).bind("savesuccess", function(){
                    listPanel.aclreadonlypanel("load");
                    stack.stacklayout("activate", listPanel);
                });

                $(addPanel).add(editPanel).bind("cancel", function(){
                    stack.stacklayout("activate", listPanel);
                });

                listPanel.aclreadonlypanel("load", this.options.space.acls);
                
                return stack;
			},

            acls: function(/* optional */acls) {
                return this._listPanel.aclreadonlypanel("acls", acls);
            },
		}
	)
); 


$.widget("ui.acleditpanel",
    $.extend({}, $.ui.savecancelpanel.prototype, {  
        _init: function(){ 
            var that = this;
            $.ui.savecancelpanel.prototype._init.call(this); //call super init first
            $("form", this.element)
            .append($.fn.create("input")
                        .attr("type", "hidden")
                        .attr("name", "spaceId")
                        .attr("value", this.options.space.spaceId))
            .append($.fn.create("input")
                    .attr("type", "hidden")
                    .attr("name", "storeId")
                    .attr("value", this.options.space.storeId));
        },
        
        acls: function(acls, message){
           var that = this;
           this.content().children().remove();
           if(acls && acls.length > 0){
               var table = createPermissionsTable(acls,false);
               $("input[type='checkbox']", table).change(function(){
                   that._fireChanged();
               });
               this.content().append(table);            
           }else{
               if(message){
                   this.content().append("<p>"+message+"</p>")
               }
           }
        },

        _save: function(){
            var that = this;
            dc.store.UpdateSpaceAcls($("form", this.element).serialize(), false, {
                success: function(acls){
                    if(acls){
                        that.acls(acls);
                    }                
                    that._fireSaveSuccess();
                },

                failure: function(){
                    that._fireSaveFailure();
                },
            });

            return true;
        }
    })
);

$.widget("ui.acladdpanel",
    $.extend({}, $.ui.acleditpanel.prototype, {  
        _init: function(){ 
            $.ui.acleditpanel.prototype._init.call(this); //call super init first
        },
        
        load: function(){
            var that = this;
            var space = this.options.space;
            dc.store.GetUnassignedSpaceAcls(space.storeId, space.spaceId,{
                success: function(acls){
                    if(acls){
                        that.acls(acls, "All permissions have already been assigned to this space.");
                    }                
                },
            });
        },
        
        _save: function(){
            var that = this;
            dc.store.UpdateSpaceAcls($("form", this.element).serialize(), true, {
                success: function(acls){
                    if(acls){
                        that.acls(acls);
                    }     
                    
                    that._fireSaveSuccess();
                },
                failure: function(){
                    that._fireSaveFailure();
                },

            });
        }
    })
);

/**
 * displays a readonly list of ACLS
 */
$.widget("ui.aclreadonlypanel",
    $.extend({}, $.ui.controlpanel.prototype, {  
        _editButton: null,
        _readOnly: false, 
        _init: function(){ 
            $.ui.controlpanel.prototype._init.call(this); //call super init first
            if(this.options.space.callerAcl != "WRITE"){
                this._readOnly = true;
            }
            
            this.addButton(
                    {
                      id:"add", 
                      text:"Add", 
                      iconClass:"plus",
                      disabled: this._readOnly,
                    });

            this._editButton = this.addButton(
                    {
                      id:"edit", 
                      text:"Edit", 
                      iconClass:"pencil",
                      disabled: true,
                    });
        },
        
        load: function(/*optional map*/ acls){
            //ajax call for acls goes here
            var that = this;
            var space = this.options.space;
            var loadFunc = function(acls){
                if(acls){
                    that.acls(acls, "There are no permissions assigned to this space.");
                }                
            };
            
            if(acls != undefined){
                loadFunc(acls);
            }else{
                dc.store.GetSpaceAcls(space.storeId, space.spaceId,{
                    success: function(acls){
                        loadFunc(acls);
                    },
                });
            }
        },
        
        _acls: null,
        acls: function(acls, message){
            
            if(acls == undefined){
                return this._acls;
            }else{
                this.content().children().remove();
                this._acls = acls; 
                if(this._acls && this._acls.length > 0){
                    this.content().append(createPermissionsTable(this._acls,true));
                    this._editButton.disable(this._readOnly);
                }else{
                    if(message){
                        this.content().append("<p>"+message+"</p>")
                    }

                    this._editButton.disable(true);

                }
            }
            
            $(this.element).trigger("acls-updated", [acls]);

        },
    })
);

var toArray = function(acls, readOnly){
    if(!acls) {
        return [0];
    }   

    var a = [];
    for(i in acls){
        var acl = acls[i];
        var read, write;
        if(!readOnly){
            
            read = createCheckbox("read", acl.name, acl.read);
            if(!acl.publicGroup){
                write = createCheckbox("write", acl.name,  acl.write);
                addCheckboxListeners(read, write);
            }else{
                write = "";
            }

        }else{
            var checkImage = createCheckImage();
            read = acl.read ? checkImage:"";
            write = acl.write ? checkImage:"";            
        }
        
        a.push([buildAclLabel(acl), 
                read, 
                write]);
    }
    
    return a;
};

var createCheckImage = function(){
    return "<i class='on checkbox' style='padding: 0px 8px;'></i>";
};

var buildAclLabel = function(acl){
    var iconClass = "user";

    var name = acl.name;
    var prefix = "group-";
    if(name.indexOf(prefix) == 0){
        iconClass = (acl.publicGroup ? "group group-public" : "group");
        name = name.replace(prefix, "");
    }
    
    return "<span class='" + iconClass + "'>" + name +"</span>";
    
    
};

var addCheckboxListeners = function (read, write) {
    var that = this;
    
    write.change(function(evt){
        if($(this).is(":checked")){
            setChecked(read, true);
        }
    });

    read.change(function(evt){
        if(!$(this).is(":checked")){
            setChecked(write, false);
        }
    });
};


var createPermissionsTable =  function(acls, readOnly){
    var that = this;
    var columnDefs;
    var read, write;
    var readCb, writeCb;
    var table;
    if(!readOnly){
        read = $("<span><input type='checkbox' id='read-all'/><label for='read-all'>Read</label></span>");
        readCb = $("input", read);
        
        readCb.change(function(){
            var checked = $(this).is(":checked");
            setChecked($("#read", table), checked);
            if(!checked){
                setChecked($("#write, #write-all", table), false);
            }
        });

        write = $("<span><input type='checkbox' id='write-all'/><label for='write-all'>Write</label></span>");
        writeCb = $("input", write); 
        writeCb.change(function(){
            var checked = $(this).is(":checked");
           setChecked($("#write", table), checked);
            if(checked){
                setChecked($("#read, #read-all", table), true);
            }
        });
        
        columnDefs = [{name:"User/Group"},{name: read},{name: write}];
    }else{
        columnDefs = [{name:"User/Group"},{name:"Read"},{name:"Write"}];
    }
        
    table =  dc.createTable(toArray(acls, readOnly), columnDefs);
    table.addClass("dc-acls");
    return table;
};

var setChecked = function (checkboxes, value){
    if(value === true || value ==='true'){
        checkboxes.attr("checked", "checked");
    }else{
        checkboxes.removeAttr("checked");
    }
};

var createCheckbox =  function (fieldName, value, checked){
    var that = this;
    var c =  $("<input type='checkbox' name='"+fieldName+"' value='"+value+"' id='"+fieldName+"' />");
    setChecked(c, checked);
    return c;
};

})();
