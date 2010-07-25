/**
 * 
 * created by Daniel Bernstein
 */

/////////////////////////////////////////////////////////////////////////////////////
///selectable list widget
///
/////////////////////////////////////////////////////////////////////////////////////

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
		,   itemActionClass: "dc-action-panel"
			
	},
	
	_currentItem: null,
	
	dataMap: new Array(),
	
	_restyleSiblings: function(siblings){
		siblings.removeClass("dc-selected-list-item dc-checked-selected-list-item dc-checked-list-item");
		siblings.find("input[type=checkbox][checked]").closest("." +this.options.itemClass).addClass("dc-checked-list-item");
	},
	
	_styleItem:  function(target){
		if(target != undefined){
			var item = $(target).nearestOfClass(this.options.itemClass);
			$(item).removeClass("dc-checked-selected-list-item dc-checked-list-item dc-selected-list-item");
			var checked = $(item).find("input[type=checkbox][checked]").size() > 0;
			$(item).addClass(checked ? "dc-checked-selected-list-item" : "dc-selected-list-item");
			this._restyleSiblings(item.siblings());
		}else{
			this._restyleSiblings(this.element.children());
		}
		
	},

	_getSelectedItems: function(){
		var array =  this.element.find("."+this.options.itemClass + " input[type=checkbox][checked]").toArray();	
		if(array == null || array == undefined){
			array = new Array();
		}
		
		return array;
	},
	
	_fireCurrentItemChanged: function(item, notify){
		this._styleItem(item);
		this._currentItem = {
			item:item, 
			data: this._getDataById($(item).attr("id")),
		};
		
		var fire = (notify == undefined || notify == null || notify == true);
		if(fire){
			this.element.trigger(
				"currentItemChanged",
				{	
					item:this._currentItem.item, 
					data: this._currentItem.data,
					selectedItems: this._getSelectedItems()
				}
			);
		}
	},
	
	_fireSelectionChanged: function(){
		this.element.trigger("selectionChanged", {selectedItems: this._getSelectedItems()});
	},

	_fireItemRemoved: function(item){
		this.element.trigger("itemRemoved", {item: item});
	},



	_itemSelectionStateChanged: function(target){
		var item = $(target).closest("."+this.options.itemClass);
		var checkbox = $("input[type=checkbox]", item).first();
		var checked = checkbox.attr("checked");
		this._styleItem(item);
		this._fireSelectionChanged(item);
		
	},
	
	clear: function(){
		this.element.children().remove();	
		this.dataMap = new Array();
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
		//add item classes to all direct children
		$(that.element).children().each(function(i,c){
			that._initItem(c);
		});
	},
	
	addItem: function(item, data){
		this.element.append(item);
		this._initItem(item,data);
	},
	
	select: function(select){
		var that = this;
		$("input[type=checkbox]",this.element).attr("checked",select);
		that._styleItem();
		that._fireSelectionChanged();
	},
	
	removeById: function(elementId) {
		var item = $("[id='"+elementId+"']", this.element).first();
		this._removeDataById(elementId);
		item.remove();
		//var firstItem = $("." + this.options.itemClass, this.element).first();
		//var data = this._getDataById(elementId);
		this._fireCurrentItemChanged(null,null);
		this._fireItemRemoved(item);
	},


	_changeSelection: function(item, select){
		var checkbox = $("input[type=checkbox]", item).first();
		checkbox.attr("checked", select);
		this._itemSelectionStateChanged(checkbox);
		
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
				return e.data;
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
		 var first = this.element.children().first();
		 if(first != undefined && first !=null){
			 this._fireCurrentItemChanged(first, true);
		 }
	},
	
	currentItem:function(){
		return this._currentItem;
	},
	

	_initItem: function(item,data){
		var that = this;
		var options = this.options;
		var itemClass = options.itemClass;
		var actionClass = options.itemActionClass;
		$(item).addClass(itemClass);

		if(options.selectable){
			$(item).prepend("<input type='checkbox'/>");
			$(item).children().first().change(function(evt){
				 that._itemSelectionStateChanged(evt.target);
			});
		}

		$(item).children("div")
		.last()
		.addClass("float-r")
		.addClass(actionClass);


		
		//bind mouse action listeners
		$(item).find("."+actionClass).andSelf().click(function(evt){
			var item = $(evt.target).nearestOfClass(itemClass);
			that._fireCurrentItemChanged(item);
			evt.stopPropagation();
		}).dblclick(function(evt){
			var item = $(evt.target).nearestOfClass(itemClass);
			that._fireCurrentItemChanged(item);
			evt.stopPropagation();
		}).mouseover(function(evt){
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

	}
});

