/**
 * 
 * created by Daniel Bernstein
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
		
		var header = $(this.element).children().first();
		var content = $(this.element).children().last();
		var title = options.title;
		
 		//set the title if not null
		if(title != null){
			header.html(title);
		}
		
		//add toggle button
		if($("."+togglerClass,this.element).size() == 0){
			header.append("<a class='"+togglerClass+"'></a>");
		}
		
		var toggler = header.children().first();
		
		//style the header
		header.addClass(options.headerClass);
		header.addClass(options.clearfix);
		
		
		//add toggle to the header 
		header.click(function(evt){
			toggler.toggleClass(options.togglerClassClose);
			content.slideToggle("fast");
		});
		
		//style the content 
		content.addClass(options.contentClass);
		content.addClass(options.clearfix);

		if(!options.open){
			content.css("display", "none");
		}

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
				if(d != null){
					var table = dc.createTable(d, ["label", "value"]);
					this.append(table);
				}
			}, 
			
			destroy: function(){ 
				//tabular destroy here
				$.ui.expandopanel.prototype.destroy.call(this); // call the original function 
			}, 
			
			options: $.extend({}, $.ui.expandopanel.prototype.options, {
				data: [["a1","b1"], ["a2","b2"]],
			}),
		}
	)
); 

