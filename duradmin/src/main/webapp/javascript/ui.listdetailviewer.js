/**
 * This jquery plugin combines a selectablelist with a "detail" pane.
 * created by Daniel Bernstein
 */
$.widget("ui.listdetailviewer", 
	{  
		_init: function(){ 
			var that = this;
			var o = this.options;
			//reference the selectablelist if it exists
			var list = $("#"+o.selectableListId).first();
			if(list == null || list == undefined){
				list = $.fn.create("div").attr("id", o.selectableListId);
				$(this.element).append(list);
			}

			var dt = $("#"+o.detailId).first();
			if(dt == null || dt == undefined){
				dt = $.fn.create("div").attr("id", o.detailId);
				$(this.element).append(detail);
			}
			
			
			//initialize list
			list.selectablelist({ selectable: false })
				.bind("currentItemChanged", function(evt, state){
					if(state.data == null || state.data == undefined){
						that._showNoSelection();
					}else{
						var detail = that._prepareDetail(state.data);
						that._getDetail().replaceContents(detail,o.detailLayout);
					}
				});
			
		}, 
		
		destroy: function(){ 

		}, 
		
		options: {
				selectableListId: "selectable-list"
			,	detailId:  "list-detail"
			,   detailClass: "dc-detail"
			,   detailPreparer: function(data){
					 var that = this;
					 var detail = $.fn.create("div");
					 var proplist = $.fn.create("table");
					 detail.append(proplist);
					 for(i in data){
						 proplist.append(
							 $.fn.create("tr")
							 	.append($.fn.create("td").html(i))
							 	.append($.fn.create("td").html(data[i]))
						 );
					 }
					 
					 return detail;
				}
		},
		
		//default behavior removes the children
		_showNoSelection: function(){
			this._clearDetail();
		},
		
		_clearDetail: function(){
			 $("#"+this.options.detailId, this.element)
				.children().remove();			
			
		},
		
		_getDetail: function(){
			 return $("#"+this.options.detailId, this.element);
		},
		
		
		//load detail
		//default behavior: iterator through members and write them to an table
		_prepareDetail: function(data){
			return this.options.detailPreparer(data);
		},
	}
);
