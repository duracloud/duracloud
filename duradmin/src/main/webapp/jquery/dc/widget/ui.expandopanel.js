/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

/**
 * @author Daniel Bernstein
 */

/////////////////////////////////////////////////////////////////////////////////////
///selectable list widget
///
/////////////////////////////////////////////////////////////////////////////////////

/**
 * expando panel widget
 */
$.widget("ui.expandopanel",{
	/**
	 * Default values go here
	 */
	options: {
			open: true,
			togglerClass: "dc-toggler",
			togglerClassClose: "dc-toggler-close",
			title: null,
			headerClass: "segment-header",
			contentClass: "segment-content",
	},
	
	_header: null,
	
	/**
	 * Initialization 
	 */
	_init: function(){
		var that = this;
		var options = this.options;
		var togglerClass = options.togglerClass;
		var clearFixClass = "clearfix";
		
		//add children if none are defined in html
		while($(this.element).children().size() < 2){
			$(this.element).append(document.createElement("div"));
		};
		
		this._header = $(this.element).children().first();
		var content = $(this.element).children().last();
		var title = options.title;
		
 		//set the title if not null
		if(title != null){
			this._header.html(title);
		}
		
		//add toggle button
		if($("."+togglerClass,this.element).size() == 0){
			this._header.append("<a class='"+togglerClass+"'></a>");
		}
		
		var toggler = this._header.children().first();
		
		//style the header
		this._header.addClass(options.headerClass);
		this._header.addClass(options.clearfix);
		
		
		//add toggle to the header 
		this._header.click(function(evt){
			toggler.toggleClass(options.togglerClassClose);
			content.slideToggle("fast");
		});
		
		//style the content 
		content.addClass(options.contentClass);
		content.addClass(options.clearfix);

		if(!options.open){
			content.css("display", "none");
            toggler.toggleClass(options.togglerClassClose);
		}

	},
	
	toggle: function(){
	    $(this._header).trigger("click");
	},
	
	getContent: function(){
		return $(this.element).children().last();
	},
	
	append: function(/*dom node*/ node){
		this.getContent().append(node);
	},
});

/**
 * Tabular Expando Panel: used for displaying lists of static properties
 */
$.widget("ui.tabularexpandopanel", 
	$.extend({}, $.ui.expandopanel.prototype, 
		{  //extended definition 
			_init: function(){ 
				$.ui.expandopanel.prototype._init.call(this); //call super init first
				//add the table if it is not null
				var d = this.options.data;
				this.setData(d);
			}, 

			setData: function(data){ 
				var tableClass = "ui-tabularexpandopanel-table";
				$("."+tableClass, this.element).remove();
				if(data != null){
					var table = dc.createTable(data, ["label", "value"]);
					$(table).addClass(tableClass)
					        .attr("role", "presentation");

					this.append(table);
				}

			}, 

			destroy: function(){ 
				//tabular destroy here
				$.ui.expandopanel.prototype.destroy.call(this); // call the original function 
			}, 
			
			options: $.extend({}, $.ui.expandopanel.prototype.options, {
				data: [],  //2 dimensional array
			}),
		}
	)
); 

