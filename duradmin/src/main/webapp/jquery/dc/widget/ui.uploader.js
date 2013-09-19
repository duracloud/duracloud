/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

/**
 * A basic HTML5 multi-file upload tool with graceful degradation.
 * If all features are supported by browser, user can drag and drop multiple
 * files onto a target, preview them, optionally modify contentIds inline, and upload
 * files while receiving progress feedback. On successful upload, the panel will 
 * present links enabling the user to navigate to the uploaded item.
 * 
 * @author Daniel Bernstein
 */

$.widget("ui.uploader", {  
        _dropPreview: null,
        _space: null,
        _progress: null,
        _progressPanel: null, 
        _dropTarget: null,
        _isDnDSupported:function(){
            return (typeof FileReader != 'undefined') &&
                   ('draggable' in document.createElement('span')) &&
                   (!!window.FormData) && 
                   ("upload" in new XMLHttpRequest);
        },
        _init: function(){ 
            var that = this;
 
            var dropTarget = $("#drop-target", this.element);

            var dp = $("#drop-preview",this.element);
            that._dropPreview =  dp;
            /**
             * configure drag and drop support only if it is supported.
             */
            if(this._isDnDSupported()){
                dropTarget.unbind();
                dropTarget.bind("dragover", 
                            function () { 
                                this.className = 'hover'; 
                                return false; 
                            });
                
                var removeHover = function () { 
                    this.className = ''; 
                    return false; 
                };

                dropTarget.bind("dragend", removeHover);
                dropTarget.bind("dragleave", removeHover);

                dropTarget.bind("drop", 
                            function (e) {
                                this.className = '';
                                e.preventDefault();
                                that._loadPreviewList(e.originalEvent.dataTransfer.files);
                            });
                that._dropTarget = dropTarget;
    
            }

            that._progress = $("#uploadprogress");

            that._progressPanel = that._progress.parent();
            
            var  singleUploadForm = $("#single-upload-form");

            singleUploadForm.validate({
                rules: {
                    file: {
                        required:true,
                    },
                },
                messages: {}
            });
            
            var formControls = singleUploadForm.find("button");
            
            singleUploadForm.find("#file")
                        .change(function(){
                              $("#single-upload-2").hide();
                              $("#single-upload-1").show();
                              var filename = singleUploadForm.find('#file').val().replace(/C:\\fakepath\\/i, '');
                              singleUploadForm.find("#path,#contentId").val(filename);

                              $(".cancel", singleUploadForm)
                              .unbind() 
                              .click(function(e){
                                  e.preventDefault();
                                  that._reset();
                              });
                              
                              $("#dnd-upload", that.element).hide();
                              that._status("The 'DuraCloud ID' field indicates the identifier a file will be known by in DuraCloud. You may update this if you wish, then click 'Upload' to start the transfer process.");
                        });
            
            
            singleUploadForm.ajaxForm({
                dataType: "text",
                beforeSerialize:function(form, options){
                    dc.checkSession();
                    that._status("Uploading...");
                    form.find("#spaceId").val(that._space.spaceId);
                    form.find("#storeId").val(that._space.storeId);
                    formControls.attr("disabled", "disabled");
                    form.hide();
                },
                success: function(data){
                    data = $.parseJSON(data);
                    dc.checkSession();
                    that._status("Successfully uploaded.", true);
                    that._progressPanel.hide();
                    //on success reset
                    that._addUploadAnotherButton();
 
                    if(that.options.clickContent){

                        that._dropPreview.append($("<p>Click on link below to view file.</p>"));

                        var item = $("<div class='upload-item'></div>");
                        item.append($("<a>"+data.results[0].contentId+"</a>")
                                .click(function(){
                                    that.options.clickContent(data.results[0]);
                                }));
                        that._dropPreview.append(item);
                        
                    }
                    
                    if(that.options.contentUploaded){
                        that.options.contentUploaded(data.results)   
                    }
                },
                
                uploadProgress: function (event, position, total, complete) {
                    that._status("Uploading...");
                    that._progressPanel.show();
                    that._progress.val(complete);
                    that._progress.html(complete);
                },
                
                error: function(xhr, status, errorThrown){
                    that._status("Upload failed", false);
                },
                
                complete: function(){
                    formControls.removeAttr("disabled");
                }
            });
            
            
            that._reset();
            
        },

        _isFile: function(file){
            return file.type || file.name.match(/[.][0-9a-zA-Z]*/g);
        },
        
        _previewfile: function (file,index, onload) {
            var that = this;
            if(!that._isFile(file)){
                if(!confirm(
                        "It looks like \"" + file.name + "\" may be a directory. " +
                        "Directory uploads are not supported through this dialog. " +
                        "\nTo upload directories, consider using the DuraCloud Sync Tool. " +
                        "\n\nTo skip this item, click 'Cancel'.")){
                    return;
                }
            }
            var item = $("<div class='upload-item form-fields'></div>").addClass("clearfix");
            item.css("vertical-align","middle");
            var ul = $("<ul></ul>");
            item.append(ul);
            ul.append("<li><label>File Name</label><input type='text' readonly='readonly' class='field clearfix' style='float:none' value='"+file.name+"'/></li>");

            var li = $("<li></li>");
            ul.append(li);
            li.append("<label>DuraCloud ID</label><input type='text' name='contentId' data-initial-value='"+file.name+"'value='"+file.name+"'/>");
            var removeBtn = $("<button class='trashbutton'><i class='trash'></i></button>")
                           .click(function(){
                               item.slideUp().remove();
                           });
            li.append(removeBtn);

            var reader = new FileReader();
            reader.onload = function (event) {
              var image = new Image();
              if(file.type.indexOf("image") == 0){
                  image.src = event.target.result;
                  image.width = 40; // a fake resize
                  image.height = 40;
                  image = $("<div class='upload-thumbnail float-l'></div>").append(image);
              }else{
                  image = "<div  class='upload-thumbnail mime-type-image-holder float-l mime-type-generic'></div>"
              }
              
              item.prepend(image);
              that._dropPreview.append(item);
              onload(index);
            };
            
            var unableToReadError =  function(err){
              alert("Unable to load '"+file.name+"\" due to " + err +". Ignoring this file...");  
            };
            
            reader.onerror = unableToReadError;
            
            try{
                reader.readAsDataURL(file);
            }catch(err) {
                console.log("unable to read as data url: file="+file.name);
                unableToReadError(err);
            }

        },
        
        space: function(/*space object*/space){
            this._space = space;
        },
        
        _reset: function(){
            var form = $("#single-upload-form",this.element);
            form[0].reset();
            form.show();

            $("#single-upload-2").show();
            $("#single-upload-1").hide();

            var dndUpload = $("#dnd-upload");

            this._dropPreview.children().remove();

            if(this._isDnDSupported()){
                this._dropTarget.show();
                dndUpload.show();
                this._status('Select a single file using the file chooser or drag and '+
                'drop files from your computer onto the dotted square to upload');

            }else{
                dndUpload.hide();
                this._status('Select a single file using the file chooser.');
            }
        },
        
        _status: function(statusText,/*boolean*/ success){
            var status = $(".status", this.element);
            status
            .removeClass("success")
            .removeClass("error")
            .find("p").html(statusText);
            
            if(success !== undefined){
                status.addClass(success ? 'success' : 'error');
            }
            
        },
        
        
        
        _uploadFiles: function(files){
            var that = this;
            var inputfields = that._dropPreview.find("input");

            var formData =  new FormData();
            formData.append("spaceId", that._space.spaceId);
            formData.append("storeId", that._space.storeId);
            
            $.each(inputfields, function(i,f){
               var node = $(f);
               
               var initialVal = node.attr("data-initial-value");
               var name = node.attr("name");

               //add only files that appear in form.
               if(name == 'contentId' && initialVal){
                   formData.append(node.attr("name"), node.val());

                   $.each(files, function(j, file){
                       if(file.name == initialVal){
                           formData.append('file', file);
                       }
                   });
               }
            });

            $("input, button", that._dropPreview).attr("disabled", "disabled");
            
            
            //using raw xhr because progress bar was not working with jquery: 
            //probably need to upgrade to jquery 1.10.* but that is non-trivial 
            //since it will require upgrading many other plugins - did not work 
            //right off that bat so I abandonned the upgrade for the time being.
            
            that._progressPanel.show();
            var status = $(".status", this.element);
            that._status("Uploading...");
            var xhr = new XMLHttpRequest();
            xhr.open('POST', '/duradmin/spaces/content/upload');
            xhr.onload = function() {
              that._progress.val(100);
              that._progress.html(100);
              var data = $.parseJSON(xhr.responseText);
              var results = data.results;
              if(data.exception){
                  that._status("Upload failed: " + data.exception.message, false);
                  
              }else{
                  that._status("Upload successful! Click on any item label below to see the content details.", true);
                  that._progressPanel.hide();
                  that._dropPreview.find("button").remove();
                  that._addUploadAnotherButton();
                  that._success(results);
                  
                  if(that.options.contentUploaded){
                      that.options.contentUploaded(results);   
                  }
              }
            };

            xhr.upload.onprogress = function (event) {
              if (event.lengthComputable) {
                var complete = (event.loaded / event.total * 100 | 0);
                that._progress.val(complete);
                that._progress.html(complete);
                if(complete == 100){
                    that._status("Finalizing upload...");
                }
              }
            };

            xhr.send(formData);
        },
        
        _addUploadAnotherButton: function(){
            var that = this;
            this._dropPreview.prepend(
                    $("<button id='upload-another'>Upload more</button>")
                        .click(function(e){
                            e.preventDefault();
                            that._reset();    
                        }
                    )
                );
        },
        
        _success: function(results){
            var that = this;
          //replace fields with links.
            $.each($(".upload-item",this.element), function(i, el){
                var node = $(el);
                var contentId = node.find("input[name='contentId']").val();
                $.each(results, function(j, result){
                    var link = $("<a>"+contentId+"</a>")
                    if(result.contentId == contentId){
                        link.click(function(){
                            var clickContent = that.options.clickContent;
                            if(clickContent){
                                clickContent(result);
                            }
                        });
                        node.children().not(":first-child").remove();
                        node.append(link);
                        return false;
                    }
                    
                });
            });            
        },
        _loadPreviewList: function(files) {
            var that = this;
            $.each(files, function(i, file){
                that._previewfile(file, i, function(index){
                    
                    //disable extraneous controls while previewing.
                    $("#single-upload-form", that.element).fadeOut();
                    $("#dnd-upload",that.element).fadeOut();

                    
                    if(index == files.length-1){
                        var cancel = $("<button>Cancel</button>")
                                        .click(function(e){
                                            e.preventDefault();
                                            that._reset();
                                        });

                        that._dropPreview.prepend(cancel);

                        var upload = 
                                $("<button>Upload</button>")
                                .click(function(e){
                                    e.preventDefault();
                                    that._uploadFiles(files);
                                });
                        
                        that._dropPreview.prepend(upload);
                        
                        that._status("The 'DuraCloud ID' field indicates the identifier a file will be known by in DuraCloud. You may update this if you wish, then click 'Upload' to start the transfer process.");
                    }
                });
            });
        },

        
        destroy: function(){ 
        
        }, 
    }
);

