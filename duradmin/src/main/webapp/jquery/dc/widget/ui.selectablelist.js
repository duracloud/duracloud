/**
 * 
 * created by Daniel Bernstein
 */

/////////////////////////////////////////////////////////////////////////////////////
///selectable list widget
///
/////////////////////////////////////////////////////////////////////////////////////
(function(){
/**
 * Selectable list widget
 */
$.widget("ui.selectablelist",{
	
	/**
	 * Default values go here
	 */
	options: {
	
			itemClass: "dc-item"
		,   selectable: true
		,   clickable: true
		,   itemActionClass: "dc-action-panel"
			
	},
	
	_currentItem: null,
	_footer: null,
	dataMap: new Array(),
	
	_restyleSiblings: function(siblings){
		siblings.removeClass("dc-selected-list-item dc-checked-selected-list-item dc-checked-list-item");
		siblings.find("input[type='checkbox']:checked").closest("." +this.options.itemClass).addClass("dc-checked-list-item");
	},
	
	_styleItem:  function(target){
		if(target != undefined && target != null){
			var item = $(target).nearestOfClass(this.options.itemClass);
			$(item).removeClass("dc-checked-selected-list-item dc-checked-list-item dc-selected-list-item");
			var checked = $(item).find("input[type='checkbox']:checked").size() > 0;
			$(item).addClass(checked ? "dc-checked-selected-list-item" : "dc-selected-list-item");
			this._restyleSiblings(item.siblings());
		}else{
			this._restyleSiblings(this.element.children("."+this.options.itemClass));
		}
		
	},

	_getSelectedItems: function(){
		var array =  new Array();
		var items = this.element.find("."+this.options.itemClass).toArray();
		$.each(items, function(i, e){
			if($(e).find("input[type='checkbox']:checked").size() > 0){
				array.push(e);
			}
		});

		return array;
	},
	
	getSelectedData: function(){
		var that = this;
		var selected = this._getSelectedItems();
		var selectedData = new Array();
		$.each(selected, function(i,selectedItem){
			selectedData.push(that._getDataById($(selectedItem).attr("id")));
		});
		
		return selectedData;
	},
	
	_setCurrentItem: function(item){
    this._currentItem = {
                         item:item, 
                         data: this._getDataById($(item).attr("id")),
                       };
	},
	
	_fireCurrentItemChanged: function(item, notify){
		
		if(item != null){
		  this._setCurrentItem(item);
		}else{
			this._currentItem = null;
		}
		
		
		$("input[type='checkbox']:checked",this.element).removeAttr("checked");
		this._styleItem(item);
		if(item){
		    $(item).find("input[type='checkbox']")
		           .not("[disabled]")
		           .attr("checked",true);
		}

        var selectedItems = this._getSelectedItems();
		
		var fire = (notify == undefined || notify == null || notify == true);
		if(fire){
			this.element.trigger(
				"currentItemChanged",
				{	
					currentItem:this._currentItem, 
					selectedItems: selectedItems
				}
			);
		}
	},
	
	_fireSelectionChanged: function(){
	  var selectedItems = this._getSelectedItems();
    
		if(selectedItems.length == 0){
		    this._fireCurrentItemChanged(null, false);
		}else if(selectedItems.length == 1){
		 this._currentItem = this._setCurrentItem(selectedItems[0]);
		}
		
		var ci = this._currentItem;
    this._styleItem(ci != null && ci.item != null ? ci.item : null);
    
		
		this.element.trigger(
			"selectionChanged", 
			{
				selectedItems: this._getSelectedItems(), 
				currentItem: this._currentItem,
			}
		);
	},

	_fireItemRemoved: function(item, data){
		this.element.trigger(
		  "itemRemoved", 
		  {
		      item: item,
		      data: data,
		  }
	    );
	},



	_itemSelectionStateChanged: function(target){
		var item = $(target).closest("."+this.options.itemClass);
		this._fireSelectionChanged(item);
	},
	
	clear: function(notify){
		this._currentItem = null;
		this.element.children("."+this.options.itemClass).remove();	
        this.element.children("."+this.options.itemClass + "-divider").remove(); 
		this.dataMap = new Array();
		this._fireCurrentItemChanged(null,notify);
		$(this._footer).html('');
	},
	
	/**
	 * Initialization 
	 */
	_init: function(){
		var that = this;
		this.clear(false);

		this._footer = $.fn.create("div").addClass("dc-selectablelist-footer").html("");
		$(that.element).append(that._footer);
		
		//add item classes to all direct children
		$(that.element).children("."+this.options.itemClass).each(function(i,c){
			that._initItem(c);
		});
		
		that.element.unbind().bind("selectionChanged", function(evt, state){
			var selectionChanged = that.options.selectionChanged;
			if(selectionChanged){
				selectionChanged(evt,state);
			}
		});
		
	},
	

    	addItem : function(item, 
    	                   data, 
    	                   selected, 
    	                   /* optional boolean */disabled, 
    	                   /*optional boolean*/noIndent, //suppresses indentation for non-selectable items.
    	                   /*optional String*/ dividerLabel //specifies label of the section 
    	                                                    //into which the item should be inserted.
    	                   ) {
    	var that = this;
      this.setFooter('');
      if(dividerLabel){
        var dividers = [];
        $("." + this.itemDividerClass(), this.element).each(function(i,e){
            dividers.push(e);
        });
        
        if(dividers.length > 0){
          
          $.each(dividers, function(i, div){
            if($(div).html() == dividerLabel){
               if(i == dividers.length-1){
                 that._footer.before(item);
               }else{
                 $(dividers[i+1]).before(item);
               }
               
               return false;
            }
          });
        }else{
          this._footer.before(item);
        }
      

        if (selected && this.options.selectable) {
          $("input[type=checkbox]", item).attr("checked", true);
        }
        
      }else{
          this._footer.before(item);
      }

      this._initItem(item, data, disabled, noIndent);

      return item;
    },
    
    itemDividerClass: function(){
      return this.options.itemClass+"-divider";
    },
	
    addDivider: function(label){
        this.setFooter('');
        
        var divider = $.fn.create("div").addClass(this.itemDividerClass());
        divider.html(label);
        this._footer.before(divider);
    },
	
	select: function(select){
		var that = this;
		that._select(select);
		that._fireSelectionChanged();
	},

    _select: function(select){
        var that = this;
        $("input[type=checkbox]",this.element).not("[disabled]").attr("checked",select);
    },

	removeById: function(elementId) {
		var item = $("[id='"+elementId+"']", this.element).first();
		var data = this._removeDataById(elementId);
		item.remove();
		
		if(this._currentItem){
		    if($(this._currentItem.item).attr("id") == elementId){
	            this._fireCurrentItemChanged(null);
		    }
		}
		this._fireItemRemoved(item, data);
	},

	idExists: function(elementId) {
		var item = $("[id='"+elementId+"']", this.element).first();
		if(item){
			return true;
		}else{
			return false;
		}
	},


	_getDataById: function(id){
		for(i in this.dataMap){
			var e = this.dataMap[i];
			if(e.key == id){
				return e.value;
			};
		}
		return null;
	},

	_removeDataById: function(id){
		for(i in this.dataMap){
			var e = this.dataMap[i];
			if(e.key == id){
				this.dataMap.splice(i,1);
				return e.value;
			};
		}
		return null;
	},

	_putData: function(id,data){
		this.dataMap[this.dataMap.length] = { key: id, value: data};
	},
	
	setCurrentItemById: function(id, notify){
	    if(this._currentItem){
	        if(this._currentItem.item.attr("id") == id){
	            return;
	        }
	    }
		this._fireCurrentItemChanged($("#"+id, this.element), notify);
	},
	
	setFirstItemAsCurrent: function(){
		 var first = this.element.children("."+this.options.itemClass).first();
		 if(first != undefined && first !=null){
			 this._fireCurrentItemChanged(first, true);
		 }
	},

	lastItemData: function(){
		 var last = this.lastItem();
		 if(last != undefined && last != null){
			 return this._getDataById($(last).attr("id"));
		 }
		 return null;
	},
	
	lastItem: function(){
		 return this.element.children("."+this.options.itemClass).last();
	},

	length: function(){
		 return this.element.children("."+this.options.itemClass).size();
	},

	
	currentItem:function(){
		return this._currentItem;
	},
	
	setFooter:function(footerContent){
		this._footer.html('');
		$(this._footer).append(footerContent);
	},

	_initItem : function(item, data, selectionDisabled, noIndent) {
      var that = this;
      var o = this.options;
      var itemClass = o.itemClass;
      var actionClass = o.itemActionClass;
      var checkbox;
      var checkboxHolder;

      if (selectionDisabled == undefined || selectionDisabled == null) {
        selectionDisabled = false;
      }

      $(item).addClass(itemClass);

      if (this.options.selectable && !(selectionDisabled && noIndent)) {
        var checkbox = $("<input type='checkbox'/>");
        checkbox.attr("aria-labelledby", item.attr("id"));
        //checkbox holder serves to create a clickable
        //buffer zone around the checkbox in order to
        //make it easier for the user to check.
        var checkboxHolder = $("<span class='cb-holder'></span>");
        checkboxHolder.append(checkbox);
        $(item).prepend(checkboxHolder);

        if (selectionDisabled != undefined) {
          checkbox.disable(selectionDisabled);
          if (selectionDisabled) {
            checkbox.makeHidden();
          } else {
            checkboxHolder.click(function(evt) {
              if (evt.target == this) {
                if (checkbox.is(":checked")) {
                  checkbox.removeAttr("checked");
                } else {
                  checkbox.attr("checked", "checked");
                }
                that._itemSelectionStateChanged(checkbox);
              }
              evt.stopPropagation();
            });
          }
        }

        $(item).children().first().change(function(evt) {
          that._itemSelectionStateChanged(evt.target);
          evt.stopPropagation();
        });
      }

      $(item).children("div").last().addClass("float-r").addClass(actionClass);

      var clickHandler = function(evt) {
        var item = $(evt.target).nearestOfClass(itemClass);
        if ($(evt.target).attr("type") != "checkbox") {

          if (that.options.clickable) {
            if (that.options.selectable) {
              that._select(false);
              item.find(":checkbox").not("[disabled]").attr("checked", true);
            }

            that._fireCurrentItemChanged(item);
          }

          evt.stopPropagation();
        }
      };

      //bind mouse action listeners
      $(item).find("." + actionClass).andSelf().click(clickHandler).dblclick(clickHandler).mouseover(function(evt) {
        $(evt.target).nearestOfClass(itemClass).find("." + actionClass).makeVisible();
      }).mouseout(function(evt) {
        $(evt.target).nearestOfClass(itemClass).find("." + actionClass).makeHidden();
      });

      //hide all actions to start
      $(item).find("." + actionClass).makeHidden();

      //add the data to the map
      that._putData($(item).attr("id"), data);
      return item;
    }
});

})();


