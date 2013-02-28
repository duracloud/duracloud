/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

/**
 * Spaces Manager
 * 
 * @author Daniel Bernstein
 */

var spacesListPane, contentItemListPane,detailPane, spacesManagerToolbar;

$(function(){
    /**
     * Load form validators
     */
    (function(){
        //reusable validators that are used with various forms.
        //used in conjunctions with the jquery.validate.js and jquery.form
        $.validator
        .addMethod("mimetype", function(value, element) { 
          return  value == null || value == '' || /^(\w[-]?)*\w\/(\w[-+]?)*\w$/.test(value);
        }, "Invalid Mimetype");

        $.validator
        .addMethod("startswith", function(value, element) { 
          return  /^[a-z0-9]/.test(value); 
        }, "Invalid");

        $.validator
            .addMethod("endswith", function(value, element) { 
              return  /[a-z0-9.]$/.test(value); 
            }, "Invalid");
        
        $.validator.addMethod("spacelower", function(value,element){return /^[a-z0-9.-]*$/.test(value);}, 
                "Invalid");
        
        $.validator.addMethod("notip", function(value,element){return !(/^[0-9]+.[0-9]+.[0-9]+.[0-9]+$/.test(value));}, 
                "Invalid");

        $.validator.addMethod("dotnum", function(value,element){return !(/^.*([.][0-9])[^.]*$/.test(value));},
                "Invalid");   

        $.validator.addMethod("misc", function(value,element){return !(/^.*([.][-]|[-][.]|[.][.]).*$/.test(value));}, 
                "Invalid");

        $.validator
        .addMethod("illegalchars", function(value, element) { 
            return  !(/^.*([\\]|[?]).*$/.test(value));
        }, "A Content ID cannot contain  '?' or '\\'");

        $.validator
        .addMethod("reserved", function(value, element) {
            return  !(/^(init|stores|spaces|security|task|acl)$/.test(value));
        }, "A Space ID cannot be a reserved name");
        //end validator definitions        
    })();

    $.fx.speeds._default = 10;
	/**
	 * This singleton class listens to changes in the history and delegates the event's data to a list of 
	 * handler functions.  The handlers are called sequentially in the order they were added. 
	 * Once a handler returns true, the rest of the handlers are ignored.
	 */
	HistoryManager = (function(){
	    var handlers = []; //an internal list of handlers
	    var contextPath = "/duradmin/spaces/sm/";
	    var requestQueue = $.Deferred();
	    
	    //make sure subsequent requestQueue.then() calls are processed immediately.
	    requestQueue.resolve();
	    
        var _buildUrl = function(obj) {
            var relative = obj.storeId;
            var spaceId = obj.spaceId;
            if(spaceId != null && spaceId != undefined){
                relative += "/" + spaceId;
            }
            
            var contentId = obj.contentId;
            if(contentId != null && contentId != undefined){
                relative += "/" + contentId;
            }
            return contextPath+relative;
        };
        
        var buildStateFromUrl = function(location) {
            var state = {};            
            var pathname = location.pathname;

            //this check is necessary for non html5 history api
            //compliant browsers.
            var hash = location.hash;
            if(hash && hash.indexOf("#" + contextPath) == 0){
                pathname = "/duradmin/" + hash.substring(1, hash.length);
            }
            
            if(pathname){
                var index = pathname.indexOf(contextPath);
                if(index == -1){
                    return state;
                }
                
                var subpathname = pathname.substring(index+contextPath.length);
                
                var first = subpathname.indexOf("/");
                if(first > 0){
                    state.storeId = subpathname.slice(0, first);
                    var second = first + subpathname.substring(first+1).indexOf("/");
                    if(second > first){
                        state.spaceId = subpathname.slice(first+1, second+1);
                        if(subpathname.length > second){
                            var contentId = subpathname.substring(second+2);
                            var qmIndex = contentId.indexOf("?");
                            if(qmIndex > 0){
                                contentId = contentId.substring(0, qmIndex);
                            }
                            state.contentId = decodeURIComponent(contentId);
                        }
                    }else{
                        state.spaceId = subpathname.substring(second+2);
                    }
                }else{
                    if(subpathname.length > 0){
                        state.storeId = subpathname;
                    }
                }
            }
            
            return state;
        };
        
	    var instance = {
            pushState: function(data){
                var url = _buildUrl(data);
                var title = "DuraCloud";
                window.History.pushState(data, title, url);
                if($.browser['msie']){
                    instance.change(data);
                }
            },               
                
            /**
             * Returns true if the change was handled.
             * @param state
             * @returns {Boolean}
             */    
            change: function(/*string*/state) {
                var params = state, handled = false;

	            $.each(handlers, function(i, handler){
	                handled = handler(params);
	                if(handled){
                        return false;
	                }
	            });
	            return handled;
	        },
	        
	        /**
	         * Adds a change handler to the list of handlers.
	         * Handler functions are processed in the order they were added.
	         * @param handler
	         */
	        addChangeHandler: function(handler){
	            if(!(typeof(handler) == "function")){
	                throw "handler must be a function";
	            }
	            handlers.push(handler);
	        },
	        
	        queue: function(deferredRequest){
	            requestQueue = requestQueue.then(deferredRequest);
	        }
	    };

        $(window).bind("popstate pushstate statechanged", function(evt){
            var state = window.History.getState().data;
            if(!state){
                state = buildStateFromUrl(window.location);
            }
            instance.change(state);
        });

        //This is a solution to the a problem related to differences in the way
        //the browser handles popstate on page load. Chrome and Safari issue a
        //'popstate' event on page load; firefox does not
        //cf http://stackoverflow.com/questions/6421769/popstate-on-pages-load-in-chrome
        if($.browser['mozilla']){
            var unpopped = ('state' in window.history);
            if(unpopped){
                setTimeout(function(){
                    var evt = document.createEvent("PopStateEvent");
                    evt.initPopStateEvent("popstate", false, false, null);
                    window.dispatchEvent(evt);
                });
            }
        }
        
        if($.browser['msie']){
            setTimeout(function(){
                var state = buildStateFromUrl(window.location);
                instance.change(state);
            });
        }
        
        return instance;
        
	})();
	
	
    $('#page-content').spacesmanager({storeProviders: storeProviders});
});

(function(){
    
    /**
     * The base pane defines functions that are common to some or all of the panel widgets.
     */
    $.widget("ui.basepane",{
        _layoutOptions: null,
        _layout: null,
        
        _init: function(){
            this._configureLayout();
        },
        
        _storeId: null,
        
        resizeAll:function(){
            if(this._layout){
                this._layout.resizeAll();
            }
        },
        
        _isObjectAlreadyDisplayedInDetail: function(objectId){
            return(this._storeId + "/" + objectId == $("#detail-pane .object-id").html());
        },
        
        _isPrimary: function(){
            return storeProviders[0].id == this._storeId;
        },
        
        _configureLayout: function(){
            var layoutOptions = this._layoutOptions;
            if(this.options){
                if(this.options.layoutOptions){
                    layoutOptions = this.options.layoutOptions;
                }
            }
            
            if(layoutOptions){
                this._layout = $(this.element).layout(layoutOptions);
            }
        },
        

        _isReadOnlyStorageProvider: function(){
            return !this._isPrimary() && !this._isRoot();
        },
        
        _isReadOnly: function(/*space or contentItem obj*/obj){
            return obj.callerAcl != "WRITE" || this._isReadOnlyStorageProvider();
        },
        
        _hasRole: function(/*string*/ role){
            //user is defined globally in spaces-manager.jsp
            var i = 0;
            for(i in user.authorities){
                if(user.authorities[i] == role){
                    return true;
                }
            }
            
            return false;
        },
        
        _isAdmin: function(){
            return this._hasRole('ROLE_ADMIN');
        },

        _isRoot: function(){
            return this._hasRole('ROLE_ROOT');
        },

        _notEmpty: function(value){
            return value != null 
                    && value != undefined 
                    && value.length != 0;
        },
        
        _deleteContentItem: function(evt, contentItem){
            var that = this;
            evt.stopPropagation();
            if(!dc.confirm("Are you sure you want to delete \n" + contentItem.contentId + "?")){
                return;
            }
            dc.store.DeleteContentItem(contentItem, {
                begin: function(){
                    dc.busy( "Deleting content item...", {modal: true});
                },
                success:function(){
                    dc.done();
                    $(that.element).trigger("contentItemDeleted", contentItem);
                },
                failure: function(message){
                    dc.done();
                    alert("failed to delete contentItem: " + message);
                },
            });
        },
        


        
        _serialize: function(obj){
            var str = "";
            for(p in obj){
                str += "&" + p + "=" + encodeURIComponent(obj[p]);
            }
            return str;
        },
        
        _createGenericJobCallback: function(updateText){
            var that = this;
            return { 
                changed: function(job){
                    dc.log("changed:" + job);
                    var p = job.getProgress();
                    dc.busy(updateText + p.successes, {modal: true});
                },
                cancelled: function(job){
                    dc.log("cancelled:" + job);
                    dc.done();
                }, 
                done: function(job){
                    var p = job.getProgress();
                    dc.log("done:" + job);
                    var message = "Successfully updated " + p.successes + " item(s).";
                    if(p.failures > 0){
                        message +=" However there were some errors: " + p.failures + " were not updated successfully.";
                    }
                    dc.done(message);
                },
            };
        },
        
        _initializePropertiesDialog: function(saveFunction){
            var d = $("#add-remove-properties-dialog");
            d.dialog({
                autoOpen: false,
                show: 'blind',
                hide: 'blind',
                height: 600,
                resizable: false,
                closeOnEscape:true,
                modal: true,
                width:500,
                buttons: {
                    'Save': saveFunction,
                    Cancel: function() {
                        $(this).dialog('close');
                    }
                },
                close: function() {},
                open: function(){}
            });
            var pane = $(".center", d);
            pane.empty();
            return d;
        },
        
        /**
         * @param contentItem
         * @param callback - an object with an onProceed() function
         *                 that is called if the content item exists and the user has said
         *                 continue with the operation or the content item doesn't exist.
         */
        _checkIfContentItemExists: function(contentItem, callback){
            dc.store.CheckIfContentItemExists(
                    contentItem, 
                    { 
                        success: function(exists){
                            if(exists){
                                if(!confirm("A contentId named '" + contentItem.contentId + 
                                            "' already exists in '" + contentItem.spaceId +
                                            "' already exists. Overwrite?")){
                                    if(callback.onCancel) callback.onCancel();
                                    return;
                                }
                            }

                            if(callback.onProceed) callback.onProceed();

                        },
                        
                        failure: function(message){
                            alert("check for existing content item failed: " + message);
                        }
                    }
                );          
        },
        

      // ///////////////////////////////////////////////////////////
      // Spaces / Content Ajax calls
      // ///////////////////////////////////////////////////////////
      _handleCopyClick: function(dialog, contentItems){
          var that = this;
          var d = dialog;
          
          var form = $("form", d),
          destStoreId       = $("#destStoreId", d).val(),
          destSpaceId       = $("#spaceId", d).val(),
          contentId         = $("#contentId", d).val();
          navigateToCopy    = $("#navigateToCopy", d).is(":checked"),
          deleteAfterCopy   = $("#deleteAfterCopy", d).is(":checked"),
          overwriteExisting = $("#overwriteExisting", d).is(":checked");
          
          if(form.valid() || contentItems.length > 1){
              d.dialog("close");
              dc.busy("Performing copy...", {modal: true});

              var job = dc.util.createJob("copy-items",true);   
              
              for(i in contentItems){
                  job.addTask({
                      _contentItem: contentItems[i],
                      execute: function(callback){
                          var theOther = this;
                          
                          var destContentId = contentId;
                          if(!destContentId || destContentId.length == 0){
                              destContentId = theOther._contentItem.contentId;
                          }
                          //define copy function
                          var doCopy = function(){
                             dc.store.copyContentItem(
                                  theOther._contentItem.storeId, 
                                  theOther._contentItem.spaceId, 
                                  theOther._contentItem.contentId, 
                                  destStoreId, 
                                  destSpaceId, 
                                  destContentId, 
                                  deleteAfterCopy, 
                                  {
                                      success: function(copiedContentItem){
                                          if(deleteAfterCopy){
                                              $(document).trigger("contentItemDeleted", theOther._contentItem);
                                          }
                                          
                                          if(that._storeId == copiedContentItem.storeId && 
                                                  that._spaceId == copiedContentItem.spaceId){
                                              $(document).trigger("contentItemAdded", theOther._contentItem);

                                          }
                                          
                                          callback.success();
                                      }
                                  }
                              );                                      
                          };//end copy function definition
                          
                          if(overwriteExisting){
                              doCopy();
                          }else{
                              that._checkIfContentItemExists(
                                  {
                                      storeId:destStoreId,
                                      spaceId:destSpaceId,
                                      contentId:destContentId,
                                  },
                                  {
                                      onCancel: function(){
                                          callback.success();
                                      },
                                      onProceed: function(){
                                          doCopy();
                                      }
                                  }
                              );
                          }
                      },
                  });
              }

              job.execute(
                  { 
                      changed: function(job){
                          var total = contentItems.length;
                          if(total > 1){
                              dc.log("changed:" + job);
                              var p = job.getProgress();
                              dc.busy("Processed " + p.successes + "/" + total, {modal: true});
                          }
                      },
                      cancelled: function(job){
                          dc.log("cancelled:" + job);
                          dc.done();
                      }, 
                      done: function(job){
                          dc.log("done:" + job);
                          dc.done();
                          
                          if(contentItems.length == 1 && job.getProgress().successes > 0){
                             var copiedContentItem =  {
                                  storeId:destStoreId,
                                  spaceId:destSpaceId,
                                  contentId:contentId,
                              };
                              
                              if(navigateToCopy){
                                  HistoryManager.pushState(copiedContentItem);
                              }                                  
                          }
                      },
                  }
              );
          }
      },
      
      _copyContentItems: function(evt, contentItems){
          var that = this;
          //initialize the dialog proper
          var d = $("#copy-content-item-dialog");

          d.dialog({
              autoOpen: true,
              show: 'blind',
              hide: 'blind',
              height: 400,
              resizable: false,
              closeOnEscape:true,
              modal: true,
              width:650,
              buttons: {
                 "OK": function(){
                     that._handleCopyClick(d,contentItems);
                 },
                 "Cancel": function(){
                   $(this).dialog('close');
                 },
              },
              
              open: function(){
                  var theOther = this;
                  var destStoreIdField = $("#destStoreId", theOther);
                  var contentIdField = $("#contentId", theOther);
                  var spaceSelect = $("#spaceId", theOther);
                  var loadWriteableSpaces = function(storeId, spaceId){
                      dc.store.GetSpaces(
                          storeId,
                          true,
                          {
                           success:function(spaces){
                              var selectedSpaceId = spaceId ? spaceId : spaceSelect.val();
                              spaceSelect.children().remove();
                              spaceSelect.append("<option value=''>Choose</option>");
                              $.each(spaces, function(index,space){
                                  spaceSelect.append("<option>"+space.spaceId+"</option>");
                              });
                              spaceSelect.val(selectedSpaceId);
                           },
                          },
                          false //make it a synchronous call 
                      );
                  };
                  
                  //attach listener if destStoreId is a select box (ie multiple stores available)
                  //load list of spaces
                  destStoreIdField.change(
                    function(evt){
                        var dest = this;
                        loadWriteableSpaces($(dest).val());
                    }
                  );
                  
                  
                  $.validator
                      .addMethod(
                          "contentIdAlreadyInSpace", 
                          function(value, element) { 
                              return !(destStoreIdField.val() == contentItems[0].storeId && 
                                       spaceSelect.val() == contentItems[0].spaceId &&
                                       contentItems[0].contentId == value);
                          },
                          "New content id equals current id. " +
                          "Change it or copy to another space."
                      );
                  
                  $.validator.addMethod(
                          "spaceIdNotEmpty", 
                          function(value, element) { 
                             return !(value == null || value.trim().length == 0);
                          },
                          "Select a space"
                      );

                  $.validator.addMethod(
                          "availableSpaces", 
                          function(value, element) { 
                              return $(element).children().length > 1;
                          },
                          "There are no writable spaces " +
                          " in this content provider.<br/>" +
                          " Please contact your DuraCloud admin for help."
                      );                
                  
                  var validator = $("form",theOther).validate({
                      rules: {
                          contentId: {
                              required: true,
                              minlength: 1,
                              illegalchars: true,
                              contentIdAlreadyInSpace: true,
                          },
                          spaceId: {
                              availableSpaces: true,
                              spaceIdNotEmpty: true,
                          },
                          
                      }
                  });
                  
                  //on change event above doesn't seem to work for select boxes
                  $("select", theOther).change(function(){
                      validator.form(); //validates the form.
                  });
                  
                  validator.resetForm();
                  var first = contentItems[0];
                  var sourceStoreId = first.storeId;
                  

                  $("#storeId", theOther).val(sourceStoreId);
                  destStoreIdField.val(sourceStoreId);
                  var navigateToCopy = $("#navigateToCopy", theOther);
                  var multiple = contentItems.length > 1;
                  if(multiple){
                      contentIdField.closest("li").hide();
                      navigateToCopy.closest("li").hide();
                  }else{
                      contentIdField.closest("li").show();
                      contentIdField.val(first.contentId);
                      navigateToCopy.closest("li").show();

                  }
                  
                  loadWriteableSpaces(sourceStoreId, first.spaceId);
                  
                  setTimeout(function(){
                      contentIdField.get(0).select();
                  },100);
              },
          });
      },
    });

    
    /**
     * This widget defines the spaces manager as a whole and contains within it the detail panes,
     * dialogs, spaces and content item list widgets as well as other functions that coordinate the 
     * various panels.
     */
    $.widget("ui.spacesmanager",$.extend({}, $.ui.basepane.prototype, {
        _spacesListViewId: "spaces-list-view",
        _contentItemListViewId: "content-item-list-view",
        _listBrowserId: "list-browser",
        _detailPaneId: "detail-pane",
        _spacesListPane: null,
        _contentItemListPane: null, 
        _detailPane: null,
        _detailManager: null,
        _spacesArray: [],
        _addSpaceDialog: null,
        _layoutOptions: {
                north__paneSelector:    ".north"
            ,   north__size:            60
            ,   center__paneSelector:   ".center"
            ,   resizable:              false
            ,   slidable:               false
            ,   spacing_open:           0           
            ,   togglerLength_open:     0   
        },
        
        _init: function(){
            var that = this;
            this._configureLayout();

            this._spacesListPane = $('#'+this._spacesListViewId,this.element);
            this._spacesListPane.spaceslistpane({layoutOptions:this._layoutOptions});
            
            this._initContentItemListPane();
            this._initializeDetailManager();
            this._initStoreSelectbox();
            this._initAddSpaceDialog();
            this._initHistoryHandlers();
            
            this._spacesListPane.bind("itemRemoved", function(evt,state){
               if(that._detailManager.isSpaceDetailDisplayed()){
                   that._detailManager.showSpaces();
               }
            });
            
            $(document).bind("contentItemDeleted", function(evt,state){
                if(that._detailManager.isContentItemDisplayed(state)){
                    HistoryManager.pushState({storeId: state.storeId, spaceId: state.spaceId});
                }else {
                    var selected =  that._contentItemListPane.contentitemlistpane("selectedContentItems");
                    if(selected.length == 0){
                        HistoryManager.pushState({storeId: state.storeId, spaceId: state.spaceId, date: new Date()});
                    }
                }
             });
        },
        
        _initContentItemListPane: function(){
            var that = this;
            this._contentItemListPane = $('#'+this._contentItemListViewId,this.element);
            this._contentItemListPane.contentitemlistpane({layoutOptions:this._layoutOptions});

            this._contentItemListPane.bind("currentItemChanged", function(evt,state){
                that._handleContentListStateChangedEvent(evt,state);
            });

            this._contentItemListPane.bind("selectionChanged", function(evt,state){
                that._handleContentListStateChangedEvent(evt,state);
            });
        },
        
        _toggleCheckAllContentItems: function(checked){
            $("#check-all-content-items", this.element).attr("checked", checked);
        },

        
        _handleContentListStateChangedEvent: function(evt, state){
            var that = this;
            
            try{
                var selectedItems = state.selectedItems;
                var length = selectedItems.length;
                var currentItem = state.currentItem;
                var space = this._contentItemListPane.contentitemlistpane("currentSpace");

                if(length == 0){
                   that._toggleCheckAllContentItems(false);
                   if(currentItem){
                       var contentId = $(currentItem.item).attr("id");
                       HistoryManager.pushState(
                               {
                                   storeId: that._storeId, 
                                   spaceId: space.spaceId, 
                                   contentId: contentId
                               });
                   }else{
                       if(!this._detailManager.isSpaceDetailDisplayed()){
                           if(space.spaceId){
                               //ensures uniqueness of history entry 
                               //so that pushState event is fired.
                               space.date = new Date();
                               HistoryManager.pushState(space);
                           }
                       }
                   }
                }else{
                    if(length == 1){
                        that._toggleCheckAllContentItems(false);
                        var contentId = $(selectedItems[0]).attr("id");
                        var space = this._contentItemListPane.contentitemlistpane("currentSpace");

                        HistoryManager.pushState(
                                {
                                    storeId: that._storeId, 
                                    spaceId: space.spaceId, 
                                    contentId: contentId
                                });
                    }else{
                        var contentItems = this._contentItemListPane.contentitemlistpane("selectedContentItems");
                        this._detailManager.showMultiContentItems(contentItems);
                    }
                }
            }catch(err){
                dc.error(err);
            }
        },

        /**
         * Adds handlers for the various possible state changes.
         */
        _initHistoryHandlers: function (){
            var that = this;
            
             //content handler
             HistoryManager.addChangeHandler(function(params){
                 if(params['contentId']){
                     HistoryManager.queue(function(){
                         return that._loadContentItem(params);
                      });

                     return true;
                 }
             });

             //multi content handler
             HistoryManager.addChangeHandler(function(params){
                 if(params['spaceId'] && params['multi']){
                     var contentItems = that._contentItemListPane
                                            .contentitemlistpane("selectedContentItems");
                     that._detailManager.showMultiContentItems(contentItems);
                     return true;
                 }
             });

             //space handler
             HistoryManager.addChangeHandler(function(params){
                 if(params['spaceId']){
                     HistoryManager.queue(function(){
                        return that._loadSpace(params, true);
                     });
                     return true;
                 }
             });

             //multi spaces handler
             HistoryManager.addChangeHandler(function(params){
                 if(params['multi']){
                     var spaces = that._spacesListPane.spaceslistpane("selectedSpaces");
                     that._detailManager.showMultiSpaces(spaces);
                     return true;
                 }
             });
            
             //all spaces
             HistoryManager.addChangeHandler(function(params){
                 HistoryManager.queue(function(){
                     dc.busy("Loading...", {modal: true});
                     if(!params.storeId){
                         params.storeId = that.getStoreId();
                     }
                     return that.loadSpaces(params).then(function(){
                         that._detailManager.showSpaces();
                     }).always(function(){
                         dc.done();
                     });
                 });
                 return true;
             });
         },
        
        _handleAddSpaceClick: function(){
            var that = this;
            if($("#add-space-form", this._addSpaceDialog).valid()){
                var space = {
                    storeId: this.getStoreId(),
                    spaceId: $("#spaceId",this._addSpaceDialog).val(),
                };

                var publicFlag = $("#publicFlag", this._addSpaceDialog).is(":checked");
                 
                dc.store.AddSpace(
                    space,
                    publicFlag,
                    {
                        begin: function(){
                            dc.busy( "Adding space...",{modal: true});
                        },
                        success: function(space){
                            dc.done();
                            if(space == undefined){
                                alert("error: space is undefined");
                            }else{
                                that._spacesListPane
                                    .spaceslistpane("addSpaceToList",space);
                                that._spacesArray.push(space);
                                that._spacesArray.sort(function(a,b){
                                   return a.spaceId > b.spaceId;
                                });
                            
                                $("#spaces-list").selectablelist("setCurrentItemById", space.spaceId);
                                that._spacesListPane.spaceslistpane("scrollToCurrentSpace");
                            }
                            
                        },
                    }
                );
                this._addSpaceDialog.dialog("close");
            }
        },
        
        
        _initAddSpaceDialog: function(){
            var that = this;
            this._addSpaceDialog = $("#add-space-dialog");
            this._addSpaceDialog.dialog({
                autoOpen: false,
                show: 'blind',
                hide: 'blind',
                resizable: false,
                height: 425,
                closeOnEscape:true,
                modal: true,
                width:500,
                buttons: {
                    'Add': function(evt) {
                        that._handleAddSpaceClick();
                    },
                    Cancel: function(evt) {
                        $(this).dialog('close');
                    }
                },
                close: function() {},
                open: function(e){
                    $("#add-space-form").resetForm();
                    //wrapping in a setTimeout seems to be necessary 
                    //to get this to run properly:  the dialog must be 
                    //visible before the focus can be set.
                    setTimeout(function(){
                        $("#add-space-form #spaceId").focus();
                    });
                }
                
            });
            
            
            $("#add-space-form").validate({
                rules: {
                    spaceId: {
                        rangelength: [3,42],
                        startswith: true,
                        endswith: true,
                        spacelower: true,
                        notip: true,
                        dotnum: true,
                        misc: true,
                        reserved: true,
                    },
                },
                messages: {
                        
                }
            });

            //implements enter key behavior
            $("#add-space-form #spaceId").keypress(function(evt) {
              if(evt.which == 13){
                 evt.stopPropagation();
                 that._handleAddSpaceClick();
                 return false;
               }
            });
            
            //launcher 
            $('.add-space-button', this.element).live("click",function(evt){
                that._addSpaceDialog.dialog("open");
            });
            
            $('#add-space-help-content').expandopanel({
                
            });
        },
        
        _initializeDetailManager: function(){
            var that = this;
            var currentlyDisplayedDetail = null;
            var currentlyDislayedObject = null;
            this._detailPane = $('#'+this._detailPaneId, this.element);
            
            this._detailManager = (function(){
                return {
                    
                   isSpaceDetailDisplayed: function(){
                       return currentlyDisplayedDetail == "space";
                       
                   },

                   isContentItemDisplayed: function(contentItem){
                       if(currentlyDisplayedDetail == "contentItem" 
                               && currentlyDisplayedObject
                               && contentItem.storeId == currentlyDisplayedObject.storeId 
                               && contentItem.spaceId == currentlyDisplayedObject.spaceId 
                               && contentItem.contentId == currentlyDisplayedObject.contentId) {
                           return true;
                       }
                       
                       return false;
                   },

                   
                   showSpaces: function(params){
                       currentlyDisplayedDetail = "spaces";
                       var storeId = (params ? params.storeId:null);
                       if(storeId == null){
                           storeId = that.getStoreId();
                       }
                       that._detailPane
                           .replaceContents($("#spacesDetailPane").clone())
                           .spacesdetail({storeId:storeId});
                       that._detailPane.spacesdetail("load", storeId);
                       that._clearContents();
                   },

                   showEmpty: function(params){
                       currentlyDisplayedDetail = "empty";
                       that._detailPane
                           .replaceContents($("#genericDetailPane").clone());
                   },

                   showSpace: function(space){
                       currentlyDisplayedDetail = "space";
                       currentlyDisplayedObject = space;
                       that._detailPane
                       .replaceContents($("#spaceDetailPane").clone());
                       that._detailPane.spacedetail();
                       that._detailPane.spacedetail("load", space);
                   },
                   
                   showContentItem: function(contentItem){
                       currentlyDisplayedDetail = "contentItem";
                       currentlyDisplayedObject = contentItem;

                       that._detailPane
                           .replaceContents($("#contentItemDetailPane").clone());
                       that._detailPane.contentitemdetail();
                       that._detailPane.contentitemdetail("load", contentItem);
                       
                   },
                   
                   showMultiSpaces: function(spaces){
                       currentlyDisplayedDetail = "spacesMulti";

                       that._detailPane
                       .replaceContents($("#spaceMultiSelectPane").clone());
                       that._detailPane.spacesmultiselectdetail({storeId: that.getStoreId()});
                       that._detailPane.spacesmultiselectdetail("spaces", spaces);
                       that._clearContents();
                   },

                   showMultiContentItems: function(contentItems){
                       currentlyDisplayedDetail = "contentMulti";

                       that._detailPane
                           .replaceContents($("#contentItemMultiSelectPane").clone());
                       var space = that._spacesListPane.spaceslistpane("currentSpace");
                       that._detailPane.contentmultiselectdetail({
                           storeId: that.getStoreId(),
                           readOnly: that._isReadOnly(space)
                       });
                       that._detailPane.contentmultiselectdetail("contentItems", contentItems);
                   },

                };
            })();
        },
        
        _clearContents: function(){
            $("#content-item-list-status").fadeOut();
            $("#content-item-list").selectablelist("clear", false);
            $("#content-item-list-view").find("button,a,input").fadeOut();
            $("#content-item-list-view").val('');
            
        },

        _clearSpaces: function(){
            $("#spaces-list").selectablelist("clear", false);
        },
        
        _getStoreType: function(storeId) {
            for(i in storeProviders){
                var store = storeProviders[i];
                if(storeId == store.id){
                    return store.type;
                }
            }

            return 'no provider found with id = ' + storeId;
        },
        
        detailManager: function(){
            return this._detailManager;
        },
        
        ////////////////////////////////////////////
        //  provider selection defs start
        ////////////////////////////////////////////

        _PROVIDER_SELECT_ID: "provider-select-box",

        _initStoreSelectbox: function(){
            var PROVIDER_COOKIE_ID = "providerId";
            var options = {
                data: this.options.storeProviders, 
                selectedIndex: 0,
            };

            var cookie = dc.cookie(PROVIDER_COOKIE_ID);
            
            if(cookie != undefined){
                for(i in options.data)
                {
                    var pid = options.data[i].id;
                    if(pid == cookie){
                        options.selectedIndex = i;
                        break;
                    }
                }
            }
            
            $("#"+this._PROVIDER_SELECT_ID).flyoutselect(options).bind("changed",function(evt,state){
                dc.cookie(PROVIDER_COOKIE_ID, state.value.id);
                HistoryManager.pushState({storeId:state.value.id});
            });         
        },
        
        setStoreId: function(storeId){
            $("#"+this._PROVIDER_SELECT_ID,this.element)
                .flyoutselect("setValueById", storeId, false);
        },
        
        getStoreId: function(){
            var provider = $("#"+this._PROVIDER_SELECT_ID)
                                .flyoutselect("value");
            return provider.id;
        },
        
        _loadSpace:function(params, showDetail){
            var that = this;
            if(showDetail == undefined){
                showDetail = true;
            }

            dc.busy("Loading...", {modal:true});
            var retrieveSpace = 
                    dc.store.GetSpace2(params)
                            .success(function(data){
                                var space = data.space;
                                that._spacesListPane.spaceslistpane("setCurrentById", space.spaceId);
                                that._loadContentItems(space);
                                
                                if(showDetail){
                                    that._displaySpace(space,params);
                                }
                            }).fail(function(){
                                if(retrieveSpace.status == 404){
                                    alert(params.spaceId + " does not exist.");
                                    this._detailManager.showEmpty();
                                }                           
                            });
            
            return $.when(that.loadSpaces(params),retrieveSpace)
                       .always(function(){
                           dc.done();
                       });
        },
        
        _loadContentItem: function(params){
            var that = this;
            dc.busy("Loading...", {modal:true});
            
            var space = that._contentItemListPane.contentitemlistpane("currentSpace");

            var loadSpace;
            if(space.spaceId == params.spaceId && space.storeId == params.storeId){
                loadSpace = $.Deferred().resolve();
            }else{
                loadSpace = that._loadSpace(params, false);
            }

            var retrieveContentItem = function(){
                dc.busy("Loading item...", {modal: true});
                var deferred =  dc.store.GetContentItem(params.storeId,params.spaceId, params.contentId,{
                    failure: function(text, xhr){
                        if(xhr.status == 404){
                            alert(params.contentId + " does not exist.");
                        }else{
                            dc.displayErrorDialog(xhr, text, text);
                        }
                    },
    
                    success: function(contentItem){
                        that._detailManager.showContentItem(contentItem);
                    },
                }).done(function(){
                    dc.done();
                });
            };
            
            return $.when(loadSpace).then(retrieveContentItem);
        },

        _displaySpace: function(space){
            this._detailManager.showSpace(space);
        },

        _loadContentItems: function(space){
            this._contentItemListPane.contentitemlistpane("load", space);
        },
        
        /**
         * params {
         *    storeId,
         *    forceRefresh,
         *    showDetail,
         * }
         */
        loadSpaces: function(optionalParams){
            var that = this;
            var forceRefresh = false;
            if(optionalParams.forceRefresh){
                forceRefresh = optionalParams.forceRefresh;
            }
            
            var storeId = optionalParams.storeId;
            
            if(storeId == undefined){
                storeId = this.getStoreId();
                forceRefresh = true;
            }

            if(storeId == this._storeId && !forceRefresh){
                return $.Deferred().resolve();
            }
            
            this._storeId = storeId;
            this.setStoreId(this._storeId);
            
            this._clearContents();
            this._clearSpaces();
            
            this._spacesListPane.spaceslistpane("showStatus", "Loading...");
            $("#provider-logo").removeClass();
            $("#provider-logo").addClass(this._getStoreType(this.getStoreId()) + '-logo');
            
            var jqxhr = dc.store.GetSpaces2({
                             storeId: storeId,
                         }).done(function(data){
                             that._spacesArray = data.spaces;
                             that._spacesListPane.spaceslistpane("load",that._spacesArray,storeId);
                             
                         }).fail(function(xhr, message){
                                dc.logXhr(xhr, message);
                                that._spacesListPane.spaceslistpane("hideStatus");
                         });
            return jqxhr;
        },
        
        _configureLayout: function(){
            var listBrowserLayout = null, 
                that = this;
            //perform layout first
            $(this.element).layout({
                north__size:            50  
            ,   north__paneSelector:     ".center-north"
            ,   north__resizable:   false
            ,   north__slidable:    false
            ,   north__spacing_open:            0           
            ,   north__togglerLength_open:      0           
            ,   north__togglerLength_closed:    0           

            ,   west__size:             800
            ,   west__minSize:          600
            ,   west__paneSelector:     "#"+that._listBrowserId
            ,   west__onresize:         function(){listBrowserLayout.resizeAll();}
            ,   center__paneSelector:   "#"+that._detailPaneId
            ,   center__onresize:       function(){that._detailPane.resizeAll();}
            });


            listBrowserLayout = $('#'+that._listBrowserId).layout({
                    west__size:             300
                ,   west__minSize:          260
                ,   west__paneSelector:     "#"+that._spacesListViewId
                ,   center__paneSelector:   "#"+that._contentItemListViewId
                ,   center__onresize:        function(){that._contentItemListPane.contentitemlistpane("resizeAll");}
            });
        },
    }));

    /**
     * This widget defines the spaces list  panel which includes the selectable list
     * as well as the filter and add space button.
     */
    $.widget("ui.spaceslistpane", $.extend({}, $.ui.basepane.prototype, {
        _layoutOptions: {
                    north__paneSelector:    ".north"
                ,   north__size:            60
                ,   center__paneSelector:   ".center"
                ,   resizable:              false
                ,   slidable:               false
                ,   spacing_open:           0           
                ,   togglerLength_open:     0   
        },
        
        _spaces: [],
        _spacesList: null,
        
        _init: function(){
            $.ui.basepane.prototype._init.call(this);
            this._initSelectAll();
            this._initSpacesList();
            this._initFilter();
            
        },
        
        _initFilter: function(){
            var that = this;
            //bind space filter field to enter key
            $(".dc-item-list-filter",this.element).bind("keyup", $.debounce(500,function(evt){
                that.load(that._spaces, that._storeId);
            }));
        },

        _initSpacesList: function(){
            var that = this;
            this._spacesList = $("#spaces-list",this.element);
            this._spacesList.selectablelist({selectable: true});

            this._spacesList.bind("currentItemChanged", function(evt,state){
                that._handleSpaceListStateChangedEvent(evt, state);
            });

            this._spacesList.bind("selectionChanged", function(evt,state){
                that._handleSpaceListStateChangedEvent(evt, state);
            });

            $(document).bind("spaceDeleted", function(evt,state){
                that._spacesList.selectablelist("removeById", state.spaceId);
            });

        },
        
        setCurrentById: function(spaceId){
          this._spacesList.selectablelist("setCurrentItemById", spaceId, false);  
        },

        _toggleCheckAll: function(checked){
            $("#check-all-spaces", this.element).attr("checked", checked);
        },
        
        /**
         * This method adds uniqueness to a state object to ensure that 
         * when the state is pushed onto the history stack it causes 
         * a pushstack event to fire.  If no such uniqueness occurs, the 
         * event will not fire and the content will not be updated.  For example, 
         * clicking on a space that is already loaded, will not be refreshed unless
         * the state object is made unique.
         */
        _createUniqueStateObject: function(state){
            state.time = new Date();
            return state;
        },
        
        _handleSpaceListStateChangedEvent: function(evt, state){
            var that = this;
            try{
                var selectedItems = state.selectedItems;
                var length = selectedItems.length;
                var newState;
                if(length == 0){
                    //uncheck 'check all' box
                    that._toggleCheckAll(false);
                    var currentItem = state.currentItem;
                    if(currentItem){
                        var spaceId = $(currentItem.item).attr("id");
                        if(spaceId){
                            newState = that._createUniqueStateObject({
                                                                storeId: that._storeId, 
                                                                spaceId:spaceId});
                            HistoryManager.pushState(newState);
                        }else{
                            dc.error("spaceId is undefined");
                        }
                    }else{
                        HistoryManager.pushState({storeId: that._storeId});
                    }
                }else{
                    if(length == 1){
                        that._toggleCheckAll(false);
                        var spaceId = $(selectedItems[0]).attr("id");
                        newState = that._createUniqueStateObject({
                            storeId: that._storeId, 
                            spaceId:spaceId});
                        HistoryManager.pushState(newState);
                    }else{
                        HistoryManager.pushState({storeId: that._storeId, multi:true});
                    }
                }
            }catch(err){
                dc.error(err);
            }
        },
        
        
        _initSelectAll: function(){
            var that = this;
            $(".dc-check-all", this.element)
                .click(function(evt){
                    var checked = $(evt.target).is(":checked");
                    that._spacesList.selectablelist("select", checked);
                });
        },
        
        showStatus:function(message){
            $("#space-list-status",this.element).html(message).fadeIn("fast");
        },

        hideStatus:function(){
            $("#space-list-status",this.element).fadeOut("fast");
        },
        
        currentSpace: function(){
          var obj =  this._spacesList.selectablelist("currentItem");  
          if(obj && obj.data){
              return obj.data;
          }else{
              return null;
          }
        },

        addSpaceToList: function(space){
            var disabled = this._isReadOnly(space);
            var node =  $.fn.create("div");
            node.attr("id", space.spaceId)
                   .html(space.spaceId);
            this._spacesList.selectablelist(
                    'addItem',node,space, false, disabled);    
        },
        
        selectedSpaces: function(){
            var spaces =  this._spacesList.selectablelist("getSelectedData");
            var storeId = this._storeId;
            var i;
            for(i = 0; i < spaces.length; i++){
                var s = spaces[i];
                
                if(s.storeId == undefined){
                    s.storeId = storeId;
                }
            };
            return spaces;
        },

        load: function(spaces, storeId){
            this._spaces = spaces;
            this._storeId = storeId;
            var that = this;
            var filter = $("#space-filter").val();
            this._spacesList.selectablelist("clear",false);
            var firstMatchFound = false;
            $.each(this._spaces,function(i, space){
                if(!filter || space.spaceId.toLowerCase().indexOf(filter.toLowerCase()) > -1){
                    that.addSpaceToList(space);
                    if(!firstMatchFound){
                        //$("#spaces-list").selectablelist('setCurrentItemById',space.spaceId);    
                        firstMatchFound = true;
                    }
                }
            });      
            
            var addSpaceButton = $(".add-space-button", this.element);
            if(!this._isAdmin() || this._isReadOnlyStorageProvider()) {
                addSpaceButton.hide();
            }else{
                addSpaceButton.show();
            }

            var cb = $(".dc-check-all", this.element);
            if(this._isReadOnlyStorageProvider()) {
                cb.makeHidden();
            }else{
                cb.makeVisible();
            }

            
            this.hideStatus();
        },
        
        scrollToCurrentSpace: function(){
            var current = this._spacesList.selectablelist("currentItem"); 
            
            if(current != null && current != undefined && 
                    current.data != null && current.data != undefined ){
                 this._spacesList
                    .closest(".dc-item-list-wrapper")
                    .scrollTo(current.item);
            }
        },

    
    }));
    
    
    /**
     * This widget defines the content list pane which includes the selected list, filter box, navigation
     * links and related buttons.
     */
    $.widget("ui.contentitemlistpane", $.extend({}, $.ui.basepane.prototype, {
         _layoutOptions: {
                north__paneSelector:    ".north"
            ,   north__size:            60
            ,   center__paneSelector:   ".center"
            ,   resizable:              false
            ,   slidable:               false
            ,   spacing_open:           0           
            ,   togglerLength_open:     0   
        },
        _spaceId: null,
        _init: function(){
            var that = this;
            $.ui.basepane.prototype._init.call(this);
            this._initSelectAll();
            
            this._initContentItemList();

            this._initBulkUploadButton();
            //handle enter key behavior on filter.
            $(this.element).find(".dc-item-list-filter").bindEnterKey(function(evt){
                that._reloadContents(that._spaceId, null, function(space){that._load(space);});
            });
        },
        
        currentSpace: function(){
          return {storeId: this._storeId, spaceId: this._spaceId};  
        },
        
        selectedContentItems: function(){
            var that = this;
            var contentItems =  this._getList().selectablelist("getSelectedData");
            $.each(contentItems, function(i,ci){
                if(!ci.spaceId){
                    ci.spaceId = that._spaceId;
                }
                if(!ci.storeId){
                    ci.storeId = that._storeId;
                }
            });
            
            return contentItems;
        },

        _initBulkUploadButton: function(){
          
          //open bulk upload tool window only if it is not already open
            //otherwise simply activate it.
            var uploadWindows = {};
                currentWindow = null, 
                windowName = null;
                
            $('.bulk-add-content-item',this.element).click(
                function(evt){
                    var link = $(evt.target),
                        windowName = link.attr("href"),
                        currentWindow = uploadWindows[windowName];
                    
                    if( currentWindow && !currentWindow.closed ){
                        $(currentWindow).focus();
                    }else{
                        currentWindow = window.open(
                            link.attr("href"),
                            windowName,
                            "menubar=0,resizable=0,width=850,height=400");
                    
                        uploadWindows[windowName] = currentWindow;
                    }
                    evt.stopPropagation();
                    return false;
                }
            );
        },
        _initContentItemList: function(){
            var that = this;
            this._getList().selectablelist({selectable: true});

            this._getList().bind("selectionChanged", function(evt,state){
                var that = this;
                if(state.selectedItems.length == 0){
                    //uncheck 'check all' box
                    $("#check-all-content-items", that.element).attr("checked", false);
                }
            });
            
            $(document).bind("contentItemDeleted", function(evt, state){
                that._getList().selectablelist("removeById", state.contentId);
            });

        },
        


        _reloadContents: function(spaceId, marker, handler, message){
            this._getList().selectablelist("clear");
            var prefix = this._getFilterText();
            dc.store.GetSpace(
                    this._storeId,
                    spaceId, 
                    {
                        begin: function(){
                            dc.busy((message ? message :"Filtering content items..."), {modal:true});
                        },
                        success: function(space){
                            dc.done();
                            if(space == undefined || space == null){
                                that._showContentItemListStatus("Error: space not found.");
                            }else{
                                handler(space);
                            }
                        }, 
                    },
                    {
                        prefix: prefix,
                        marker: marker,
                    });
        },
        
        _initSelectAll: function(){
            var that = this;
            $(".dc-check-all", this.element)
                .click(function(evt){
                    var checked = $(evt.target).is(":checked");
                    that._getList().selectablelist("select", checked);
                });
        },
        
        _addContentItemsToList: function(space){
            var that = this,
                readOnly = this._isReadOnly(space); 
            
            $.each(space.contents,function(i,value){
                that._addContentItemToList({
                    contentId:value,
                    spaceId:space.spaceId,
                    storeId:space.storeId,
                }, readOnly);
            });
        },
        
        _addContentItemToList: function(contentItem, readOnly){
            var that = this, node, actions, content, deleteButton, copyButton;
            actions = $.fn.create("div");
            copyButton = 
                 $("<button title='copy content item' class='copy-button icon-only'>" +
                         "<i class='pre copy'></i>" +
                         "</button>")
                 .click(function(evt){
                     evt.stopPropagation();
                     that._copyContentItems(evt,[contentItem]);
                 });

            if(!this._isReadOnlyStorageProvider()){
                actions.append(copyButton);
            }

            
            if(!readOnly){
                deleteButton = 
                    $("<button title='delete content item' class='delete-space-button icon-only'>" +
                            "<i class='pre trash'></i>" +
                            "</button>")
                    .click(function(evt){
                        that._deleteContentItem(evt,contentItem);
                    });

                actions.append(deleteButton);
            }
            

            content = $.fn.create("span");
            content.attr("class", "dc-item-content")
                      .html(contentItem.contentId);
            node =  $.fn.create("div");
            node.attr("id", contentItem.contentId)
                   .append(content)
                   .append(actions);
            
            var item =  this._getList()
                            .selectablelist('addItem',node, contentItem, false, readOnly);
            return item;
            
        },

        _getList: function(){
          return $("#content-item-list");  
        },
        
        _showContentItemListStatus: function(text){
            var contentItemListStatus = $("#content-item-list-status",this.element);
            if(!text){
                contentItemListStatus.fadeOut("fast").html('');
            }else{
                contentItemListStatus.html(text).fadeIn("slow");
            }
        },
        
        _createShowMoreLink: function(){
            var link, that = this;
            link = $.fn.create("a");
            link.html('show more')
            .addClass("dc-link")
            .addClass("dc-show-more-link")
            .click(function(){that._showMoreHandler();});
            return link;
        },
        
        _getFilterText: function(){
            return $("#content-item-filter",this.element).val();
        },
        
        _clearFilterText: function(){
            return $("#content-item-filter",this.element).val("");
        },
        
        _showMoreHandler: function(){
            var that = this,  list, itemData, lastItem, prefix, marker;
            list = this._getList();
            itemData = list.selectablelist("lastItemData");
            lastItem = list.selectablelist("lastItem");
            marker = null;

            if(itemData != null){
                marker = itemData.contentId;
                prefix = this._getFilterText();
                dc.store.GetSpace(
                        itemData.storeId,
                        itemData.spaceId, 
                        {
                            begin: function(){
                                dc.busy("Loading more content items...", {modal:true});
                                lastItem.addClass("dc-selectablelist-hl");      
                        },
                            success: function(s){
                                dc.done();
                                that._addContentItemsToList(s);
                                that._updateNavigationControls(s);
                            }, 
                            failure:function(info){
                                setTimeout(function(){
                                    alert("Failed to retrieve more content items:" + info);
                                },200);

                                dc.done();
                            },
                        },
                        {prefix: prefix, marker: marker}
                    );
            }
            
        },
        
        _updateNavigationControls: function(space){
            var list,listView, listCount,totalCount,statusTxt = null;
            
            listView = this.element;        
            list = this._getList();
            listCount = list.selectablelist("length");
            listView.find(".dc-show-more-link").remove();
            
            if(space.properties.count == 0){
                statusTxt = "";
            }else{
                var itemCount = space.properties.count;
                
                if(space.itemCount && space.itemCount > -1){
                    itemCount = space.itemCount;
                }
                
                totalCount = (this._getFilterText() == '' ? itemCount : "?");
                
                if(listCount == 0 && space.contents.length == 0){
                    statusText = "";
                }else{
                    statusTxt = "Showing 1 - " + listCount + " of " + totalCount;
                }
            }

            this._showContentItemListStatus(statusTxt);

            if(space.contents.length > 199){
                listView.find(".dc-item-list-controls").html('').append(this._createShowMoreLink());
                list.selectablelist("setFooter", this._createShowMoreLink());
            }else{
                if(space.properties.count == 0){
                    list.selectablelist("setFooter",$.fn.create("div").html("This space is empty."));
                }else{
                    list.selectablelist("setFooter",'');
                }

            }
        },

        load: function(space){
            this._clearFilterText();
            this._load(space);
            $("#check-all-content-items").attr("checked", false);

        },
        
        _load: function(space){
    
            this._spaceId = space.spaceId;
            this._storeId = space.storeId;
            var list,listView, readOnly = this._isReadOnly(space); 
    
            listView = $(this.element);       
            list = this._getList();
            
            list.selectablelist("clear", false);
            
            listView
                .find("button,input,a")
                .fadeIn();
    
            $(".bulk-add-content-item", listView)
                .attr("href", "/duradmin/spaces/bulk-upload?storeId="+space.storeId + "&spaceId=" + escape(space.spaceId))
                .attr("target", "bulk-upload-" + escape(space.spaceId));
                
            var refreshButton = $(".refresh-space-button",listView); 
            refreshButton.unbind("click");
            refreshButton.click(function(){
                    window.location.reload();
            });
            
            this._addContentItemsToList(space);
            this._updateNavigationControls(space);
    
            if(readOnly){
                $(".add-content-item-button, .bulk-add-content-item", listView)
                    .hide();
            }
            
            var cb = $(".dc-check-all", this.element);
            if(this._isReadOnlyStorageProvider()) {
                cb.makeHidden();
            }else{
                cb.makeVisible();
            }

            var synctool = $(".get-synctool-button", this.element);
            synctool.unbind("click").click(function() {
                window.location = "http://docs.duraspace.org/duracloud/${project.version}/downloads/duracloud-sync-${project.version}.jar?attachment=true";
                window.open("https://wiki.duraspace.org/display/DURACLOUDDOC/DuraCloud+Sync+Tool", "_blank");
            });

        },
    }));

    /**
     * This is a base class for all the detail panes.
     */
    $.widget("ui.basedetailpane", $.extend({}, $.ui.basepane.prototype, {
        _layoutOptions: {
                north__paneSelector:    ".north"
            ,   north__size:            100
            ,   center__paneSelector:   ".center"
            ,   resizable:              false
            ,   slidable:               false
            ,   spacing_open:           0
            ,   togglerLength_open:     0
            
       },
       
       _init: function(){
           $.ui.basepane.prototype._init.call(this);
           this._storeId = this.options.storeId;
       },
       
       _setObjectName: function(name){
           $(".object-name", this.element).empty()
                                          .prepend(name)
                                          .attr("title", name);
       },

       _getStoreId: function(){
           return this._storeId;
       },

       _setObjectId: function(objectId){
           $(".object-id", this.element).html(this._getStoreId()+"/"+objectId);   
       },
       
       _appendToCenter: function(node){
           $(".center", this.element).append(node);
       },

       _loadAclPane: function(space, readOnly){
           var viewerPane =  $.fn.create("div")
                                 .attr("id", "acl-editor")
                                 .acleditor({open: true, space: space, readOnly: readOnly});
           this._appendToCenter(viewerPane);
           return viewerPane;      
       },
       
       _loadPropertiesPane: function(extendedProperties, /*bool*/ readOnly){
           var viewerPane = this._createPropertiesPane(extendedProperties, readOnly);
           this._appendToCenter(viewerPane);
           return viewerPane;
       },

       _createPropertiesPane: function(extendedProperties, /*bool*/readOnly){
           var viewerPane = $.fn.create("div")
                           .propertiesviewer({title: "Properties", readOnly: readOnly})
                           .propertiesviewer("load",extendedProperties);
           return viewerPane;
       },

       _createTagPane: function(tags, /*bool*/readOnly){
           var viewerPane = $.fn.create("div")
                           .tagsviewer({title: "Tags", readOnly: readOnly})
                           .tagsviewer("load",tags);
           return viewerPane;
       },

       _loadTagPane: function(tags, /*bool*/readOnly){
           var viewerPane = this._createTagPane(tags,readOnly);
           this._appendToCenter(viewerPane);
           return viewerPane;
       },

       _loadProperties: function(/* array */ properties){
           var propertiesDiv = $(".detail-properties", this.element).first();
           
           if(propertiesDiv.size() == 0){
               propertiesDiv = $.fn.create("div").addClass("detail-properties");
               propertiesDiv.tabularexpandopanel({
                                    title: "Details", 
                                    data: properties});
               this._appendToCenter(propertiesDiv);
           }else{
               $(propertiesDiv).tabularexpandopanel("setData", properties);
           }
           
           return propertiesDiv;
       },
       
       _isPubliclyReadable: function(acls){
           if(!acls){
               return false;
           }
           
           var value = false;
           $.each(acls, function(i,acl){
               if(acl.publicGroup){
                   value = true;
                   return true;
               }
           });
           return value;
       },
       
       _makeSpacePubliclyReadable: function(/*event object*/evt, storeId, spaceId, successFunc){
           var that = this;
           dc.busy("Making space public...");
           dc.store.UpdateSpaceAcls("storeId="+storeId+
                                   "&spaceId="+escape(spaceId) + 
                                   "&read=group-public", 
                                   true,
                                   {
                                     success:function(acls){
                                         dc.done();
                                         that.setAcls(acls); 
                                         if(successFunc){
                                             successFunc();
                                         }

                                     },
                                   });
       },

       _createMakePublicButton: function(storeId, spaceId, successFunc){
           var that = this;
           var button = $.fn.create("button")
                           .addClass("featured")
                           .css("margin-left","10px")
                           .html("Make space publicly readable");
               
           button.click(function(evt){
               that._makeSpacePubliclyReadable(
                       evt,
                       storeId, 
                       spaceId,
                       successFunc);
               
           });     
           
           $(document).unbind("acls-updated");
           $(document).bind("acls-updated",function(evt, acls){
               if(!that._isPubliclyReadable(acls)){
                   button.show();
               }else{
                   button.hide();
                   $("#make-public-warning").hide();
                   
               }
           });
           
           return button;
       },
       
       setAcls: function(acls){
           var aclEditor = $("#detail-pane #acl-editor");
           aclEditor.acleditor("acls", acls);
           if(!aclEditor.length){
               $(document).trigger("acls-updated", [acls]);
           }
       },

       _formatBulkPropertiesUpdateParams: function(params){
           var data = "";
           data += this._formatPropertiesList(params.propertiesToRemove, "remove");
           data += this._formatPropertiesList(params.propertiesToAdd, "add");
           data += this._formatParamList(params.tagsToRemove, "tag", "remove");
           data += this._formatParamList(params.tagsToAdd, "tag", "add");
           return data;
       },

       _formatPropertiesList: function(list, fieldNameModifier){
           var i, list,item,data;
           data = "";
           for(i = 0; i < list.length; i++){
               item = list[i];
               data += "&properties-name-"+fieldNameModifier+"-" + i + "=" + encodeURIComponent(item.name);
               data += "&properties-value-"+fieldNameModifier+"-" + i + "=" + encodeURIComponent(item.value);
           }
           return data;
       },

       _formatParamList: function(list, fieldPrefix, fieldNameModifier){
           var i, list,item,data;
           data = "";
           for(i = 0; i < list.length; i++){
               item = list[i];
               data += "&" + fieldPrefix + "-" + fieldNameModifier + "-"+ i + "=" + encodeURIComponent(item);
           }
           return data;
       },
       
       _openContentItemDialog: function(saveFunction, contentItem){
           var d = $('#edit-content-item-dialog');

           // prepare edit dialog
           d.find("input[name=storeId]").val(contentItem ? contentItem.storeId : "");
           d.find("input[name=spaceId]").val(contentItem ? contentItem.spaceId : "");
           d.find("input[name=contentId]").val(contentItem ? contentItem.contentId : "");
           d.find("input[name=contentMimetype]").val(contentItem ? contentItem.properties.mimetype : "");
           
           d.dialog({
               autoOpen: false,
               show: 'blind',
               hide: 'blind',
               height: 250,
               resizable: false,
               closeOnEscape:true,
               modal: true,
               width:500,
               buttons: {
                   'Save': saveFunction,
                   Cancel: function() {
                       $(this).dialog('close');
                   }
               },
               close: function() {},
               open: function(e){
                   var form = $("#edit-content-item-form",this);
                   form.validate({
                       rules: {
                           contentMimetype: {
                               required:true,
                               minlength: 3,
                               mimetype: true,
                           },
                       },
                       messages: {
                               
                       }
                   });

                   $("input",this).bindEnterKey(saveFunction);
               }
           });
           d.dialog("open");
       },
       
       _addSpaceProperties: function(spaceId, name, value, callback){
           var data = "properties-name=" + encodeURIComponent(name) +"&properties-value="+encodeURIComponent(value);
           dc.ajax(this._createSpacePropertiesCall(spaceId, data, "addProperties", callback));
       },

       _removeSpaceProperties: function(spaceId, name,callback){
           var data = "properties-name=" + encodeURIComponent(name);
           dc.ajax(this._createSpacePropertiesCall(spaceId, data, "removeProperties", callback));
       },

       _addSpaceTag: function(spaceId, tag, callback){
           var data = "tag="+ encodeURIComponent(tag);
           dc.ajax(this._createSpacePropertiesCall(spaceId, data, "addTag", callback));
       },

       _removeSpaceTag: function(spaceId, tag,callback){
           var data = "tag="+encodeURIComponent(tag);
           dc.ajax(this._createSpacePropertiesCall(spaceId, data, "removeTag", callback));
       },

       _addRemoveSpaceProperties: function(spaceId, params,callback){
           dc.ajax(this._createSpacePropertiesCall(spaceId, this._formatBulkPropertiesUpdateParams(params), "addRemove", callback));
       },
       
       _createSpacePropertiesCall: function(spaceId, data, method,callback){
           var newData = data + "&method=" + method;
           var storeId = this._storeId;
           return {
               url: "/duradmin/spaces/space?storeId="+storeId+"&spaceId="+encodeURIComponent(spaceId) +"&action=put",
               type: "POST",
               data: newData,
               cache: false,
               context: document.body, 
               success: function(data){
                   callback.success();
               },
               error: function(xhr, textStatus, errorThrown){
                   //dc.error("get spaces failed: " + textStatus + ", error: " + errorThrown);
                   callback.failure(textStatus);
               },
           };
       },
       
       _createContentItemPropertiesCall: function(spaceId, contentId, data, method,callback){
           var newData = data + "&method=" + method;
           var storeId = this._storeId;
           return {
               url: "/duradmin/spaces/content?storeId="+storeId+"&spaceId="+encodeURIComponent(spaceId) +"&contentId="+encodeURIComponent(contentId) +"&action=put",
               type: "POST",
               data: newData,
               cache: false,
               context: document.body, 
               success: function(data){
                   callback.success();
               },
               failure: function(textStatus){
                   callback.failure(textStatus);
               },
           };
       },
       
       _addContentItemProperties: function(spaceId, contentId, name, value, callback){
           var data = "properties-name=" + encodeURIComponent(name) +"&properties-value="+encodeURIComponent(value);
           dc.ajax(this._createContentItemPropertiesCall(spaceId, contentId, data, "addProperties", callback));
       },

       _removeContentItemProperties: function(spaceId, contentId, name,callback){
           var data = "properties-name=" + encodeURIComponent(name);
           dc.ajax(this._createContentItemPropertiesCall(spaceId,contentId, data, "removeProperties", callback));
       },

       _addContentItemTag: function(spaceId, contentId, tag, callback){
           var data = "tag="+ encodeURIComponent(tag);
           dc.ajax(this._createContentItemPropertiesCall(spaceId,contentId, data, "addTag", callback));
       },

       _removeContentItemTag: function(spaceId, contentId, tag,callback){
           var data = "tag="+encodeURIComponent(tag);
           dc.ajax(this._createContentItemPropertiesCall(spaceId, contentId, data, "removeTag", callback));
       },
       
       _addRemoveContentItemProperties: function(spaceId, contentId, params,callback){
           dc.ajax(
                   this._createContentItemPropertiesCall(
                   spaceId,
                   contentId,
                   this._formatBulkPropertiesUpdateParams(params),
                   "addRemove", 
                   callback));     
       },
     }));

    
    $.widget("ui.basemultidetailpane", $.extend({}, $.ui.basedetailpane.prototype, {
        
        _init: function(){
            $.ui.basedetailpane.prototype._init.call(this);
        },
 
        _preparePropertiesDialog: function(targetListDataType){
            var that = this;
            var items, getFunction;
            if(targetListDataType == "contentItem"){
                items = this._contentItems;
                getFunction = function(ci,callback){
                    dc.store.GetContentItem(ci.storeId, ci.spaceId, ci.contentId, callback);
                };
            }else{
                items = this._spaces;
                getFunction = function(space,callback){
                    dc.store.GetSpace(space.storeId, space.spaceId, callback);
                };
            }

            this._aggregatePropertiesFromSelection(items,getFunction,{
                success: function(data){
                    that._loadPropertiesDialog(data,targetListDataType);
                },
                failure: function(text){
                    alert("unable to load selection:" + text);
                },
            });

        },

        _appendToListIfNew: function(newItems, itemList, equalsFunc) {
            var toAppend = [];
            var i,j,ni,item,append;
            for(i = 0; i < newItems.length; i++){
                ni = newItems[i];
                if(itemList.length == 0){
                    toAppend.push(ni);
                }else{
                    append = true;
                    for(j = 0; j <  itemList.length; j++){
                        item = itemList[j];
                        if(equalsFunc != undefined){
                            if(equalsFunc(ni,item)){
                                append = false;
                                break;
                            }
                        }else{
                            if(ni == item){
                                append = false;
                                break;
                            }
                        }
                    }
                    
                    if(append){
                        toAppend.push(ni);
                    }
                }
            }

            for(i = 0; i < toAppend.length; i++){
                itemList.push(toAppend[i]);
            }
        },


        _aggregatePropertiesFromSelection: function(items, getFunction, fcallback){
            var that = this;
            dc.busy("Loading selection...", {modal: true});
            var propertiesLists = [];
            var tagLists = [];
            var job = dc.util.createJob("load-content-items");  
            for(i in items){
                job.addTask({
                    _item: items[i],
                    execute: function(callback){
                        getFunction(
                                this._item,
                                {
                                    success:function(obj){
                                        propertiesLists.push(obj.extendedProperties);
                                        tagLists.push(obj.properties.tags);
                                        callback.success();
                                    },
                                    failure: function(message){
                                        callback.failure();
                                    },
                                }                   
                        );
                    },
                });
            }

            job.execute({ 
                changed: function(job){
                    dc.debug("changed:" + job);
                    var p = job.getProgress();
                    dc.busy(p.successes  + " content items loaded...", {modal: true});
                },
                cancelled: function(job){
                    dc.debug("cancelled:" + job);
                    dc.done();
                }, 
                done: function(job){
                    dc.log("done:" + job);
                    dc.done();
                    var properties = [];
                    var tags = [];
                    var i;
                    for(i = 0; i < propertiesLists.length; i++){
                        that._appendToListIfNew(propertiesLists[i],properties, function(a,b){ return a.name == b.name && a.value == b.value;});
                    }
                    
                    for(i = 0; i < tagLists.length; i++){
                        that._appendToListIfNew(tagLists[i],tags);
                    }
                    
                    fcallback.success({
                        properties: properties,
                        tags: tags,
                    });
                }, 
            });
        },
        
        _equals:function(a,b){
            return (a.name == b.name && a.value == b.value);
        },
        
        _removeValueFromList: function(value, list, equals){
            var i = -1,
                el;
            
            for(i in list){
                el = list[i];
                if(equals != undefined ? equals(value, el) : value == el){
                    list.splice(i,1);
                    return el;
                }
            }
            return null;
        },
        
        _loadPropertiesDialog: function(data, targetListType){
            var that = this;
            var propertiesToBeAdded = [];
            var propertiesToBeRemoved = [];
            var tagsToBeAdded = [];
            var tagsToBeRemoved = [];

            var mp = this._createPropertiesPane(data.properties);
            
            
            $(mp).bind("dc-add", function(evt, future){
                evt.stopPropagation();
                var value = future.value;
                future.success();
                //if in the removed list, remove from remove list
                that._removeValueFromList(value,propertiesToBeRemoved, that._equals);
                that._removeValueFromList(value,propertiesToBeAdded, that._equals);
                propertiesToBeAdded.push(value);
            }).bind("dc-remove", function(evt, future){
                evt.stopPropagation();
                future.success();
                var value = future.value;
                if(that._removeValueFromList(value, propertiesToBeAdded, that._equals) == null){
                    that._removeValueFromList(value, propertiesToBeRemoved, that._equals);
                    propertiesToBeRemoved.push(value);
                }
            });

            var tag = that._createTagPane(data.tags);

            $(tag).bind("dc-add", function(evt, future){
                evt.stopPropagation();
                var value = future.value[0];
                future.success();
                that._removeValueFromList(value,tagsToBeRemoved);
                that._removeValueFromList(value,tagsToBeAdded);
                tagsToBeAdded.push(value);
            }).bind("dc-remove", function(evt, future){
                evt.stopPropagation();
                var value = future.value;
                future.success();
                if(that._removeValueFromList(value, tagsToBeAdded) == null){
                    that._removeValueFromList(value, tagsToBeRemoved);
                    tagsToBeRemoved.push(value);
                }
            });

            
            var saveFunction = function(){
                var msg = "Applying the following changes: \n";
                for(i in propertiesToBeRemoved){
                    var m = propertiesToBeRemoved[i];
                    msg +="\tremoving: " + m.name + "=" + m.value + "\n";
                }

                for(i in tagsToBeRemoved){
                    msg +="\tremoving: " + tagsToBeRemoved[i] + "\n";
                }

                for(i in propertiesToBeAdded){
                    var m = propertiesToBeAdded[i];
                    msg +="\tadding: " + m.name + "=" + m.value + "\n";
                }

                for(i in tagsToBeAdded){
                    msg +="\tadding: " + tagsToBeAdded[i] + "\n";
                }
                
                if(confirm(msg)){
                    var params = {
                        propertiesToRemove: propertiesToBeRemoved,
                        propertiesToAdd:    propertiesToBeAdded,
                        tagsToRemove:     tagsToBeRemoved, 
                        tagsToAdd:        tagsToBeAdded,
                    };
                    
                    if(targetListType == "contentItem"){
                        params.contentItems = that._contentItems;
                        that._bulkUpdateContentProperties(params);
                    }else{
                        params.spaces = that._spaces;
                        that._bulkUpdateSpaceProperties(params);
                    }

                    d.dialog("close");
                    dc.busy("Preparing to perform update...", {modal: true});
                    
                }
            };
            
            
            var d = that._initializePropertiesDialog(saveFunction);
            var center = $(".center", d);
            center.append(mp);
            center.append(tag);
            dc.done();
            d.dialog("open");
            
        },
        
        _bulkUpdateContentProperties: function(params){
            var that = this;
            var job = dc.util.createJob("bulk-update-content-properties");
            var contentItems = params.contentItems;
            var i;
            for(i = 0; i < contentItems.length; i++){
                var contentItem = contentItems[i];
                job.addTask({
                    _contentItem: contentItem,
                    execute: function(callback){
                        var theOther = this;
                        var citem = theOther._contentItem;
                        that._addRemoveContentItemProperties(citem.spaceId, citem.contentId, params,callback);
                    },
                });
            }
            job.execute(this._createGenericJobCallback("Updating content items: "));
        },

        _bulkUpdateSpaceProperties: function(params){
            var that = this;
            var job = dc.util.createJob("bulk-update-space-properties");
            $.each(params.spaces, function(i, space){
                job.addTask({
                    _space: space,
                    execute: function(callback){
                        that._addRemoveSpaceProperties(this._space.spaceId, params,callback);
                    },
                });
            });
            job.execute(this._createGenericJobCallback("Updating spaces: "));
        },
    
    }));
    /**
     * This widget defines the detail view to be displayed when no spaces are loaded.
     */
    $.widget("ui.spacesdetail", $.extend({}, $.ui.basedetailpane.prototype, {
        _init: function(){
            $.ui.basedetailpane.prototype._init.call(this);
            this._setObjectName("Spaces");
        },     
        
        load: function(storeId){
          this._storeId = storeId;
          
          if(this._isAdmin()){
              var history = $.fn.create("div");
              this._appendToCenter(history);
              history.historypanel({storeId: this._storeId});
          }
        },

    }));

    /**
     * This widget defines the detail that is displayed when multiple spaces are selected
     */
    $.widget("ui.spacesmultiselectdetail", $.extend({}, $.ui.basemultidetailpane.prototype, {
        _init: function(){
            $.ui.basemultidetailpane.prototype._init.call(this);
            this._initPane();
        },        
        
        _spaces: [],
        _title: "{count} space(s) selected.",
        
        
        _updateTitle: function(count){
            var title = this._title.replace("{count}", count);
            this._setObjectName(title);
        },

        spaces: function(spaces){
          this._spaces = spaces;  
          this._updateTitle(this._spaces.length);
          
        },
        _initPane: function(){
            var that = this;
            
            // attach delete button listener
            var deleteButton = $(".delete-space-button",this.element);
            deleteButton.click(function(evt){
                var confirmText = "Are you sure you want to delete multiple spaces?";
                var busyText = "Deleting spaces";
                var spaces = that._spaces;
                if(spaces.length < 2){
                    confirmText = "Are you sure you want to delete this space?";
                    busyText = "Deleting space";
                }

                if(!dc.confirm(confirmText)){
                    return;
                }
                dc.busy(busyText, {modal: true});

                var job = dc.util.createJob("delete-spaces");   

                $.each(spaces, function(i,space){
                    job.addTask({
                        _space: spaces[i],
                        execute: function(callback){
                            var theOther = this;
                            dc.store.DeleteSpace(this._space, {
                                success:function(){
                                    callback.success();
                                    $(document).trigger("spaceDeleted", theOther._space);
                                },
                                failure: function(error, xhr){
                                    callback.failure();
                                    dc.displayErrorDialog(xhr,"Unable to delete '" + theOther._space.spaceId +"'");
                                },
                            });
                        },
                    });
                });

                job.execute(
                    { 
                        changed: function(job){
                            dc.log("changed:" + job);
                            var p = job.getProgress();
                            dc.busy("Deleting spaces: " + p.successes, {modal: true});
                        },
                        cancelled: function(job){
                            dc.log("cancelled:" + job);
                            dc.done();
                        }, 
                        done: function(job){
                            dc.log("done:" + job);
                            dc.done();
                            HistoryManager.pushState({storeId: that._storeId});
                    }, 
                });
            });

            if(!this._isAdmin()){
                deleteButton.hide();
            }
            
            var editPropsButton = $(".add-remove-properties-button",this.element);
            editPropsButton.click(function(evt){
                that._preparePropertiesDialog("space");
            });
            
            if(this._isReadOnlyStorageProvider()){
                deleteButton.hide();
                editPropsButton.hide();
            }
            
        },
        
    }));

    
    /**
     * This widget defines the detailed view of a space
     */
    $.widget("ui.spacedetail", $.extend({}, $.ui.basedetailpane.prototype, {
        _spaceId: null,
        _init: function(){
            var that = this;
            $.ui.basedetailpane.prototype._init.call(this);
            $("#recount").die().live("click",function(){
                $(this).parent().empty().append("Recounting " + that._createThrobberHtml());
                that._pollItemCount(
                        {   storeId: that._storeId, 
                            spaceId: that._spaceId,
                        }, 
                        true);
                
            });
        },                   
        
        _createThrobberHtml:function(){
           return "<img src='/duradmin/images/wait.gif'/>";    
        },
        
        _extractSpaceProperties: function(space){
            var itemCount;
            if(space.itemCount == null || 
                    space.itemCount == undefined || 
                        parseInt(space.itemCount) < 0) {
                itemCount = space.properties.count + 
                                ": performing exact count " + 
                                    this._createThrobberHtml();               
            }else{
                itemCount = space.itemCount + " <button id='recount'><i class='pre refresh'></i>Recount</button>";
            };
            
            var spaceProps = [
                    ['Items', itemCount],
                    ['Created', space.properties.created]
            ];

            if(space.properties.size){
                spaceProps.push(['Size', space.properties.size]);
            }
            
            var bitIntegrityResult = space.bitIntegrityResult;
            if(bitIntegrityResult){
                var completionDate = bitIntegrityResult.completionDate;
                var result = bitIntegrityResult.result;
                var reportContentId = bitIntegrityResult.reportContentId;

                spaceProps.push(["Last Health Check", "<div class='health-check "+
                                     bitIntegrityResult.result+"'>"+completionDate+" - "
                                     + result
                                     + " <a id='report-viewer' href=''>[report]</a>"
                                     + "</div>" ]);
            }

            var propertiesDiv = this._loadProperties(spaceProps);

            if(bitIntegrityResult){
                dc.reportOverlayOnClick(
                        $("#report-viewer", propertiesDiv),
                        bitIntegrityResult.reportContentId);
            }
        },

        _getSpaceMetrics: function(space){
            return dc.store.GetStorageReportDetail(space.storeId, space.spaceId, null);
        },

        _createMimetypeGraphPanel: function(space){
            var that = this;
            var mimetypePanel =  $.fn.create("div");
            //get metrics ajax call
            $.when(this._getSpaceMetrics(space))
             .done(function(response) { 
                $.each(response.metrics.spaceMetrics, function(i, metrics){
                    if(space.spaceId == metrics.spaceName){
                        metrics.reportId = response.reportId;
                        metrics.date = response.metrics.date;

                        dc.chart.loadMimetypeMetricsPanel(mimetypePanel, metrics);
                        return false;
                    }
                });
            });

            return mimetypePanel;
        },
        
        _pollItemCount: function(space, recount){
            var that = this;
            
            dc.store.GetSpace(
                    space.storeId,
                    space.spaceId, 
                    {
                        success: function(s){
                            if(that._isObjectAlreadyDisplayedInDetail(s.spaceId)){
                                if(s != undefined && s != null){
                                    that._extractSpaceProperties(s);
                                    if(s.itemCount == null || parseInt(s.itemCount) < 0){
                                        setTimeout(function(){
                                            recount = false;
                                            that._pollItemCount(s);
                                        }, 5000);
                                    }
                                }
                            }
                        }, 
                    },
                    {
                        recount: recount
                    }
                );              
        },

        
        load: function(space){
            var that = this;
            this._storeId = space.storeId;
            this._spaceId = space.spaceId;
            this._setObjectName(space.spaceId);
            this._setObjectId(space.spaceId);
            
            var readOnly = this._isReadOnly(space);

            var deleteSpaceButton = $(".delete-space-button",this.element);
            deleteSpaceButton.hide();
            if(this._isAdmin() && !this._isReadOnlyStorageProvider()){
                deleteSpaceButton.show();

                // attach delete button listener
                deleteSpaceButton.click(function(evt){
                    var deferred = that._deleteSpace(evt,space);
                    deferred.then(function(){
                        HistoryManager.pushState({storeId: space.storeId});
                    });
                });
            }

            var switchHolder = $(".streaming-switch-holder");
            switchHolder.hide();                
            if(this._isAdmin() && space.primaryStorageProvider){
                switchHolder.show();                
                    
                    //deploy/undeploy switch definition and bindings
                    $(".streaming-switch",that.element).onoffswitch({
                                initialState: space.streamingEnabled ? "on" : "off"
                                            , onStateClass: "on left"
                                            , onIconClass: "checkbox"
                                            , offStateClass: "right"
                                            , offIconClass: "x"
                                            , onText: "On"
                                            , offText: "Off"
                    }).bind("turnOff", function(evt, future){
                        switchHolder.busy();
                        $.when(dc.service.UpdateSpaceStreaming(space.storeId, space.spaceId, false))
                         .done(function(){
                             future.success();
                         }).always(function(){
                             switchHolder.idle();
                         });
                    }).bind("turnOn", function(evt, future){
                        switchHolder.busy();
                        $.when(dc.service.UpdateSpaceStreaming(space.storeId, space.spaceId, true))
                         .done(function(){
                             future.success();
                         }).always(function(){
                             switchHolder.idle();
                         });
                        
                    });
            }
            
            if(this._isAdmin()){
                var makePublicButton = this._createMakePublicButton(
                        space.storeId, 
                        space.spaceId);
                
                $(makePublicButton).insertAfter(deleteSpaceButton);

                if(this._isPubliclyReadable(space.acls)){
                    makePublicButton.hide();
                }
                
                this._loadAclPane(space, readOnly);
            }

            this._extractSpaceProperties(space);

            if(space.itemCount == null || parseInt(space.itemCount) < 0){
                //attach poller if itemCount is null or -1
                
                setTimeout(function(){
                    that._pollItemCount(space);
                }, 5000);
                
                
            }

            this._loadHistoryPanel(space);
        },

        _loadHistoryPanel: function(options){
            var history = $.fn.create("div");
            this._appendToCenter(history);
            history.historypanel(options);
        },
        
        _deleteSpace: function(evt, space) {
            evt.stopPropagation();
            if(!dc.confirm("Are you sure you want to delete \n" + space.spaceId + "?")){
                return;
            }
            
            return dc.store.DeleteSpace(space, {
                begin: function(){
                    dc.busy( "Deleting space...",{modal: true});
                },
                
                success:function(){
                    dc.done();
                    $(document).trigger("spaceDeleted", space);
                },
                
                failure: function(message){
                    dc.done();
                    alert("failed to delete space!");
                },
            });
        },
        
        

    }));
    
    /**
     * This widget defines detail view to be displayed when multiple content items are selected.
     */
    $.widget("ui.contentmultiselectdetail", $.extend({}, $.ui.basemultidetailpane.prototype, {
        _contentItems: [],
        _init: function(){
            var that = this;
            $.ui.basemultidetailpane.prototype._init.call(this);
            var readOnly = this.options.readOnly || (this._isReadOnlyStorageProvider());
        
            // attach delete button listener
            var deleteButton = $(".delete-content-item-button",this.element);
            if(readOnly){
                deleteButton.hide();
            }else{
                deleteButton.click(function(evt){
                    that._handleDeleteButtonClick(evt);
                });
            }

            //attach mimetype edit listener
            var editButton = $(".edit-selected-content-items-button",this.element);
            if(readOnly){
                editButton.hide();
            }else{
                editButton.click(function(evt){
                    that._handleEditButtonClick(evt);
                });
            }

            var addRemoveProperties = $(".add-remove-properties-button",this.element);
            if(readOnly){
                addRemoveProperties.hide();
            }else{
                addRemoveProperties.click(function(evt){
                    that._preparePropertiesDialog("contentItem");
                });
            }
            
            var copyButton = $(".copy-content-item-button",this.element);

            if(!this._isReadOnlyStorageProvider()){
                copyButton.click(function(evt){
                    that._copyContentItems(evt, that._contentItems);
                });     
            }else{
                copyButton.hide();
            }
        },
        
        contentItems: function(contentItems){
            this._contentItems = contentItems;
            this._updateTitle(this._contentItems.length);
        },
        _updateTitle: function(count){
            this._setObjectName(
                    "{count} content item(s) selected."
                                    .replace("{count}", count));
        },
        
        _handleDeleteButtonClick: function(evt){
            var that = this;
            var contentItems = that._contentItems;

            var confirmMessage = "Are you sure you want to delete multiple content items?";
            var busyMessage = "Deleting content items...";
    
            if(contentItems.length < 2)
            {
                confirmMessage = "Are you sure you want to delete the content item?";
                busyMessage = "Deleting content item...";
            }
    
            if(!dc.confirm(confirmMessage)){
                return;
            }
            
            //identify space from first contentitem.
            var space = {storeId: contentItems[0].storeId, 
                         spaceId: contentItems[0].spaceId};
            
            dc.busy(busyMessage, {modal: true});
            var job = dc.util.createJob("delete-content-items");
            var deletedContentItems = [];
            var i;
            for(i = 0; i < contentItems.length; i++){
                job.addTask({
                    _contentItem: contentItems[i],
                    execute: function(callback){
                        var that = this;
                        dc.store.DeleteContentItem(this._contentItem, {
                            success:function(){
                                deletedContentItems.push(that._contentItem);
                                callback.success();
                            },
                            failure: function(message){
                                callback.failure();
                            },
                        });
                    },
                });
            }
    
            job.execute(
                { 
                    changed: function(job){
                        dc.log("changed:" + job);
                        var p = job.getProgress();
                        dc.busy("Deleting content items: " + p.successes, {modal: true});
                    },
    
                    cancelled: function(job){
                        dc.log("cancelled:" + job);
                        dc.done();
                    }, 
                    done: function(job){
                        dc.log("done:" + job);
                        dc.done();
                        $.each(deletedContentItems, function(i,ci){
                            $(document).trigger("contentItemDeleted", ci);
                        });

                }, 
            });
        },
        
        _handleEditButtonClick: function(evt){
            var that = this;
            this._openContentItemDialog(function(){
                var form = $("#edit-content-item-form");

                if(form.valid()){
                    dc.busy("Preparing to update content items...", {modal: true});
                    $('#edit-content-item-dialog').dialog("close");
                    var contentItems = that._contentItems;
                    var job = dc.util.createJob("update-content-items");    
                    $.each(contentItems, function(i, contentItem){
                        contentItem.contentMimetype = $("input[name=contentMimetype]", form).val();
                        job.addTask({
                            _contentItem: contentItem,
                            execute: function(callback){
                                var theOther = this;
                                var citem = theOther._contentItem;
                                var data = that._serialize(citem);
                                dc.store.UpdateContentItemMimetype(data, {
                                    success:function(){
                                        callback.success();
                                    },
                                    failure: function(message){
                                        callback.failure();
                                    },
                                });
                            },
                        });
                    });
    
                    job.execute(
                        { 
                            changed: function(job){
                                dc.log("changed:" + job);
                                var p = job.getProgress();
                                dc.busy("Updating content items: " + p.successes, {modal: true});
                            },
                            cancelled: function(job){
                                dc.log("cancelled:" + job);
                                dc.done();
                            }, 
                            done: function(job){
                                dc.log("done:" + job);
                                dc.done();
                        }, 
                    });
                }
            });    
        },
        
        
    }));

    
    /**
     * This widget defines the detail view of a content item.
     */
    $.widget("ui.contentitemdetail", $.extend({}, $.ui.basedetailpane.prototype, {
        _contentItem: null, 
        
        _layoutOptions: $.extend(
                            true,
                            {}, 
                            $.ui.spacesdetail.prototype._layoutOptions,{
                                north__size:150,
                            }),
        _init: function(){
            $.ui.basedetailpane.prototype._init.call(this);
        },      
        
        _extractContentItemProperties: function(contentItem){
            var m = contentItem.properties;
            return [
                        ["Space", contentItem.spaceId],
                        ["Size", dc.formatBytes(m.size)],
                        ["Modified", m.modified],
                        ["Checksum", m.checksum],
                   ];
        },
        
        load: function(/*object*/contentItem){
            var that = this, readOnly = false;
            this._contentItem = contentItem;
            this._storeId = contentItem.storeId;
            this._setObjectName(contentItem.contentId);
            this._setObjectId(contentItem.spaceId+"/"+contentItem.contentId);
            readOnly = this._isReadOnly(contentItem);

            $(".download-content-item-button", this.element)
                .attr("href", dc.store.formatDownloadURL(contentItem));

            var deleteContentButton = $(".delete-content-item-button",this.element);
            deleteContentButton.click(function(evt){
                    that._deleteContentItem(evt,contentItem);
                });
            if(readOnly) {
                deleteContentButton.hide();
            }

            
            var copyButton = $(".copy-content-item-button",this.element);
            if(!this._isReadOnlyStorageProvider()){
                copyButton.click(function(evt){
                    that._copyContentItems(
                            evt,[contentItem]);
                });
            }else{
                copyButton.hide();
            }
             
                
            var mimetype = contentItem.properties.mimetype;

            if(mimetype.indexOf("video") == 0){
                this._loadVideo(contentItem);
            }else if(mimetype.indexOf("audio") == 0){
                this._loadAudio(contentItem);
            }else {
                var viewerURL= dc.store.formatDownloadURL(contentItem, false);
                $(".view-content-item-button", this.element)
                    .attr("href", viewerURL)
                    .css("display", "inline-block");
            }

            $(".durastore-link", this.element)
                .attr("href", contentItem.durastoreURL);

            this._loadProperties(this._extractContentItemProperties(contentItem));
            // load the details panel
            var mimetype = contentItem.properties.mimetype;
            $(".mime-type .value", this.element).text(mimetype);
            $(".mime-type-image-holder", this.element).addClass(dc.getMimetypeImageClass(mimetype));

            var mp = this._loadPropertiesPane(contentItem.extendedProperties, readOnly);
            
            
            $(mp).bind("dc-add", function(evt, future){
                    var value = future.value;
                    that._addContentItemProperties(contentItem.spaceId, contentItem.contentId, value.name, value.value, future);
                }).bind("dc-remove", function(evt, future){
                    that._removeContentItemProperties(contentItem.spaceId, contentItem.contentId, future.value.name,future);
                });
            
            var tag = this._loadTagPane(contentItem.properties.tags, readOnly);

            $(tag).bind("dc-add", function(evt, future){
                var value = future.value[0];
                that._addContentItemTag(contentItem.spaceId, contentItem.contentId, value, future);
            }).bind("dc-remove", function(evt, future){
                var value = future.value;
                that._removeContentItemTag(contentItem.spaceId, contentItem.contentId, value, future);
            });

            var editContentItemButton = $(".edit-content-item-button",this.element);
            editContentItemButton.click(
                    function(evt){
                        that._handleEditContentItemClick(evt);
                    }
                );
            if(readOnly){
                editContentItemButton.hide();
            }
        },

        _handleEditContentItemClick:function(evt){
            var that = this;
            this._openContentItemDialog(function(){
                var form = $("#edit-content-item-form");
                var data = form.serialize();
                if(form.valid()){
                    var callback = {
                        success: function(contentItem){
                            dc.done();
                            that.load(contentItem);
                        },
                        failure: function(text){
                            dc.done();
                            alert("failed to update content item.");
                        },
                    };
                    $('#edit-content-item-dialog').dialog("close");
                    dc.busy("Updating mime type", {modal: true});
                    dc.store.UpdateContentItemMimetype(data, callback);
                }
            }, this._contentItem);
            
        },
        _loadPreview: function(contentItem){
            var that = this;
            //if space is not publicly visible and image viewer service is running, we must 
            //notify the user that the space must be opened.
        
           var options = {
                           'transitionIn'  :       'elastic',
                           'transitionOut' :       'elastic',
                           'speedIn'               :       600, 
                           'speedOut'              :       200, 
                           'overlayShow'   :       false,};

           var open = this._isPubliclyReadable(contentItem.acls);
           var imageViewerBaseURL = contentItem.imageViewerBaseURL;
           
           var viewerURL,thumbnailURL;

           if(imageViewerBaseURL && open){
                   options['width'] = $(document).width()*0.8;
                   options['height'] = $(document).height()*0.8;
                   options['type'] = 'iframe';
                   viewerURL = dc.store.formatJ2kViewerURL(imageViewerBaseURL, contentItem, open);
                   thumbnailURL = dc.store.formatThumbnail(contentItem, 2,imageViewerBaseURL, open);
           }else{
                   options['type'] = 'image';
                   viewerURL = dc.store.formatDownloadURL(contentItem,false);
                   thumbnailURL = dc.store.formatGenericThumbnail(contentItem);
           }
           
           var div = $.fn.create("div")
                                     .expandopanel({title: "Preview",});
           
           $(".view-content-item-button", this.element)
                   .css("display","inline-block")
                   .attr("href", viewerURL);

           var thumbnail = $.fn.create("img")
                                                   .attr("src", thumbnailURL)
                                                   .addClass("preview-image");
           
           var viewerLink = $.fn.create("a").append(thumbnail)
                                                   .attr("href", viewerURL)
                                                   .fancybox(options);      

           var wrapper = $.fn.create("div")
                                                   .addClass("preview-image-wrapper")
                                                   .append(viewerLink);

           var parent = $(viewerLink.parent());
           var loadingMessage = $("<div><h2><img src='/duradmin/images/wait.gif'/>Loading image...</h2>");
           parent.append(loadingMessage);
           thumbnail.hide();
           thumbnail.load(function(){
               loadingMessage.remove();
               thumbnail.show();
           });
           
           if(!open && imageViewerBaseURL && this._isAdmin()){
                   var warning = $.fn.create("div").addClass("warning").attr("id", "make-public-warning");
                   $(div).expandopanel("getContent").append(warning);
                   var button = this._createMakePublicButton(
                                   contentItem.storeId, 
                                   contentItem.spaceId, 
                                   function(){
                                       that._getContentItem(
                                               contentItem.storeId,
                                               contentItem.spaceId,
                                               contentItem.contentId,
                                               true);
                                                                         
                                   });

                   warning.append("<span>To use the JP2 Viewer you must grant the 'public'" +
                                       " group read access to this space.</span>")
                              .append(button);
           }
                    
           $(div).expandopanel("getContent").append(wrapper);
           this._appendToCenter(div);
        },

        _loadVideo: function(contentItem){      
            this._loadMedia(contentItem, "Watch","video");
        },

        _loadAudio: function(contentItem){      
            this._loadMedia(contentItem, "Listen","audio");
        },

        _loadMedia: function(contentItem, title,/*audio or video*/type){  
            //non primary content is not streamable.
            if(!contentItem.primaryStorageProvider){
                return; 
            }
            var that = this;
            var viewer = $.fn.create("div").attr("id", "mediaspace");
            var div = $.fn.create("div")
            .expandopanel({title: title});
            
            $(div).expandopanel("getContent").css("text-align", "center").append(viewer);

            this._appendToCenter(div);

            $.when(dc.service.GetStreamingStatus(contentItem.storeId, contentItem.spaceId))
             .done(function(result){
                 var streamingHost = null;
                if(result.streamingEnabled){
                    streamingHost = result.streamingHost;
                    if(streamingHost != null && streamingHost.indexOf("null") == -1){
                        that._writeMediaTag(result.streamingHost, contentItem);
                    }else{
                        viewer.append("<p>The streaming service for this space is starting up.</p>");
                        viewer.append("<p>Please try again in a few minutes by refreshing this page.</p>");
                    }
                }else{
                    viewer.append("<p>No streaming service is running against this space.</p>");
                    if(that._isAdmin()){
                        var streamingButton = $.fn.create("button").attr("id", "enable-streaming");
                        streamingButton.html("Enable streaming for this space");
                        streamingButton.click(function(evt){
                            $(evt.target).disable();
                            dc.busy("Enabling streaming service...");
                            dc.service.UpdateSpaceStreaming(
                                        contentItem.storeId, 
                                        contentItem.spaceId, true)
                                      .done(function(){
                                          HistoryManager.pushState(contentItem);
                                      })
                                      .always(function(){
                                          dc.done();
                                      });
                        });
                        viewer.append(streamingButton);
                    }
                    
                    //viewer.append("<p>The player below will work on HTML5 compliant browsers only.");
                    //viewer.append(that._createHTML5MediaTag(contentItem,type));
                }
             });
        },

        _writeMediaTag: function(streamingHost, contentItem){
            setTimeout(function(){
                //async necessary to let the DOM update itself so that the mediaspace dom element is present.
                var so = new SWFObject('/duradmin/jwplayer/player.swf','ply','350','216','9','#ffffff');
                so.addParam('allowfullscreen','true');
                so.addParam('allowscriptaccess','always');
                so.addParam('wmode','opaque');
                so.addVariable('skin','/duradmin/jwplayer/stylish.swf');
                so.addVariable('file', contentItem.contentId);
                so.addVariable('streamer', 'rtmp://' +streamingHost+ '/cfx/st');
                so.write('mediaspace');
            },1000);
        },
        
        _createHTML5MediaTag: function(contentItem,type){
            return type == 'audio' ? this._createHTML5AudioTag(contentItem) : this._createHTML5VideoTag(contentItem);
        },
        
        _createHTML5AudioTag: function(contentItem){
            return $.fn.create("audio")
            .attr("src", contentItem.viewerURL)
            .attr("loop", "false")
            .attr("preload", "false")
            .attr("controls", "true");
        },

        _createHTML5VideoTag: function(contentItem){
            return $.fn.create("video")
            .attr("src", contentItem.viewerURL)
            .attr("loop", "false")
            .attr("preload", "false")
            .attr("width", "350")
            .attr("height", "216")
            .attr("controls", "true");
        },
        

        
    }));
})();

