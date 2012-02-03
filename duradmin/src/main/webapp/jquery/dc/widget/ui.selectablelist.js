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
		siblings.find("input[type=checkbox][checked]").closest("." +this.options.itemClass).addClass("dc-checked-list-item");
	},
	
	_styleItem:  function(target){
		if(target != undefined && target != null){
			var item = $(target).nearestOfClass(this.options.itemClass);
			$(item).removeClass("dc-checked-selected-list-item dc-checked-list-item dc-selected-list-item");
			var checked = $(item).find("input[type=checkbox][checked]").size() > 0;
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
			if($(e).find("input[type=checkbox][checked]").size() > 0){
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
	
	_fireCurrentItemChanged: function(item, notify){
		
		if(item != null){
			this._currentItem = {
					item:item, 
					data: this._getDataById($(item).attr("id")),
				};
		}else{
			this._currentItem = null;
		}
		
		$("input[type=checkbox][checked]",this.element).attr("checked", false);
		this._styleItem(item);
		
		var fire = (notify == undefined || notify == null || notify == true);
		if(fire){
			this.element.trigger(
				"currentItemChanged",
				{	
					currentItem:this._currentItem, 
					selectedItems: this._getSelectedItems()
				}
			);
		}
	},
	
	_fireSelectionChanged: function(){
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
		var checkbox = $("input[type=checkbox]", item).first();
		var checked = checkbox.attr("checked");
		this._fireSelectionChanged(item);
	},
	
	clear: function(){
		this._currentItem = null;
		this.element.children("."+this.options.itemClass).remove();	
		this.dataMap = new Array();
		this._fireCurrentItemChanged(null,null);
		$(this._footer).html('');
	},
	
	/**
	 * Initialization 
	 */
	_init: function(){
		var that = this;
		var options = this.options;
		var itemClass = options.itemClass;
		var actionClass = options.itemActionClass;
		this.clear();

		this._footer = $.fn.create("div").addClass("dc-selectablelist-footer").html("");
		$(that.element).append(that._footer);
		
		//add item classes to all direct children
		$(that.element).children("."+this.options.itemClass).each(function(i,c){
			that._initItem(c);
		});
		
		that.element.bind("selectionChanged", function(evt, state){
			var selectionChanged = that.options.selectionChanged;
			if(selectionChanged){
				selectionChanged(evt,state);
			}
		});
		
	},
	
	addItem: function(item, data, selected, /*optional boolean*/disabled){
		this.setFooter('');
		this._footer.before(item);
		this._initItem(item,data, disabled);
		
		if(selected && this.options.selectable){
			$("input[type=checkbox]",item).attr("checked", true);
		}
		
		return item;
	},
	
	select: function(select){
		var that = this;
		$("input[type=checkbox]",this.element).not("[disabled]").attr("checked",select);
		that._fireSelectionChanged();
	},
	
	removeById: function(elementId) {
		var item = $("[id='"+elementId+"']", this.element).first();
		var data = this._removeDataById(elementId);
		item.remove();
		this._fireCurrentItemChanged(null,null);
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

	_initItem: function(item,data, selectionDisabled){
		var that = this;
		var o = this.options;
		var itemClass = o.itemClass;
		var actionClass = o.itemActionClass;
		$(item).addClass(itemClass);

		if(this.options.selectable){
		    var checkbox = $("<input type='checkbox'/>");
		    //checkbox holder serves to create a clickable
		    //buffer zone around the checkbox in order to
		    //make it easier for the user to check.
		    var checkboxHolder = $("<span class='cb-holder'></span>");
            checkboxHolder.append(checkbox);
            $(item).prepend(checkboxHolder);
            if(selectionDisabled != undefined){
                checkbox.disable(selectionDisabled);
                if(selectionDisabled){
                    checkbox.makeHidden();
                }else{
                    checkboxHolder.click(function(evt){
                        if(evt.target == this){
                            if(checkbox.is(":checked")){
                                checkbox.removeAttr("checked");
                            }else{
                                checkbox.attr("checked", "checked");
                            }
                            that._itemSelectionStateChanged(checkbox);
                        }
                        evt.stopPropagation();
                    });
                }
            }
            
            $(item).children().first().change(function(evt){
                 that._itemSelectionStateChanged(evt.target);
                 evt.stopPropagation();
            });
        }

		$(item).children("div")
		.last()
		.addClass("float-r")
		.addClass(actionClass);

		var clickHandler = function(evt){
			var item = $(evt.target).nearestOfClass(itemClass);
			if($(evt.target).attr("type") != "checkbox"){
				if(that.options.clickable){
					that._fireCurrentItemChanged(item);
				}else if(that.options.selectable){
					item.find(":checkbox").click();
					that._itemSelectionStateChanged(evt.target);
				}

				evt.stopPropagation();
			}
		};
		
		//bind mouse action listeners
		$(item).find("."+actionClass).andSelf()
		.click(clickHandler)
		.dblclick(clickHandler)
		.mouseover(function(evt){
			$(evt.target).nearestOfClass(itemClass)
						 .find("."+actionClass)
						 .makeVisible();
		}).mouseout(function(evt){
			$(evt.target).nearestOfClass(itemClass)
						 .find("."+actionClass)
						 .makeHidden();
		});
		
		//hide all actions to start
		$(item).find("."+actionClass).makeHidden();	
		
		//add the data to the map
		that._putData($(item).attr("id"), data);
		
		return item;
	}
});

})();


