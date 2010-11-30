/**
 * This jquery plugin is a fancy flyout select box
 * created by Daniel Bernstein
 */
(function(){
		$.widget("ui.flyoutselect", 
		{  
			_init: function(){ 
				var that = this;
				var o = this.options;
				var widgetClass = o.widgetClass;
				
				$(this.element).addClass(widgetClass)
				var selection = $.fn.create("button")
							.addClass("primary")
							.append($.fn.create("span").addClass(widgetClass + "-selected-text"));
				selection.append($.fn.create("i").addClass("post arw-down-liteblue"));
				
				$(this.element).append(selection);
				
				var ul = $.fn.create("ul");
				$(this.element).append(ul);
				
				$(this.element).hover(
						function() { $('ul', this).css('display', 'block'); },
						function() { $('ul', this).css('display', 'none'); }
				);
					
				this._selectedIndex = o.selectedIndex;
	
				this._rerender(o.data);
	
			}, 
			
			_rerender: function(data){
				var that = this;
				var ul = $("ul", this.element).first();
				var widgetClass = this.options.widgetClass;
				ul.children().remove();
				
				var selectedTextClass = widgetClass + "-selected-text";
				for(i in data){
					if(i == this._selectedIndex){
						$("."+ selectedTextClass ,this.element).html(data[i].label);
					}else{
						var item = $.fn.create("li");
						var itemTextHolder = $.fn.create("a").html(data[i].label);
						item.append(itemTextHolder);
						ul.append(item);
						item.click(function(evt){
							var label = $(evt.target).html();
	
							var data = that.options.data;
							for(d in data){
								if(data[d].label == label){
									that._changeValueByIndex(d);
								}
							}
						});
					}
				}
			},
			
			_changeValueByIndex: function(/*index of new value*/i, notify){
				var fire = true;
				if(notify != undefined & notify != null){
					fire = notify;
				}

				var that = this;
				var data = this.options.data;
				this._selectedIndex = i;	
				$("ul", that.element).fadeOut("slow",function(){
					that._rerender(data);
				});
				
				if(fire){
					this.element.trigger("changed", {target: that.element, value: data[i]});
				}
			},
			
			_selectedIndex: 0,
			
			value: function(){
				return this.options.data[this._selectedIndex];
			},

			setValueById: function(id, /*optional*/ notify){
				var data = this.options.data;
				var index = null;
				for(i in data){
					if(data[i].id == id){
						index = i;
					}
				}
					
				if(index == null){
					throw("id [" + id + "] not associated with any elements in this list");
				}
					
				if(this._selectedIndex != index){
					this._changeValueByIndex(index, notify);
				}
			},

			destroy: function(){ 
	
			}, 
			
			options: {
				widgetClass: "fos-widget",
				data: null,
				selectedIndex: 0, 
				
				       
			},
			
		}
	);
})();

