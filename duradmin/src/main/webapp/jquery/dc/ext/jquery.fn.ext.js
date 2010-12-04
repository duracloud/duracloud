/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

/**
 * A collection of useful jquery extensions
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 */


$(function(){
	(function(){
		////////////////////////////////////////////////////////////////////
		//jquery extensions
		///////////////////////////////////////////////////////////////////
		/**
		 * Similar to hide/show jquery functions but toggles the visibility css 
		 * attribute instead of the display attribute.
		 */
		$.fn.makeVisible = function(isvisible) {
			return $(this).css("visibility", (isvisible == undefined || isvisible) ? "visible":"hidden");
		};
		
		$.fn.makeHidden = function() {
			return $(this).makeVisible(false);
		};

		
		/**
		 * This finds the nearest ancestor of the specified class.  It is just like jQuery.closest(selector) except
		 * that it includes the dom node your are calling the function on.
		 */
		$.fn.nearestOfClass = function(className){
			var nearest = (this.hasClass(className)) ? this : this.closest("." + className);
			return $(nearest);
		};

		
		$.fn.toggleOpenClosed = function(){
			$(this).nearestOfClass("button")
					.toggleClass("dialog-open")
					.toggleClass("dialog-closed");
		};

		/**
		 * Shorthand for document.createElement
		 * Returns the new element as a jQuery object.
		 */
		$.fn.create = function(tag){
			return $(document.createElement(tag));
			
		};
		
		/**
		 * Scrolls the specified dom element to the top of the 
		 * viewport (which is the object on which you're calling this function)
		 * if possible. If it can't scroll it to the top of the viewport it at 
		 * least scrolls the container so that the element is in view.
		 * @param DOM node or jquery object
		 */
		$.fn.scrollTo = function(element){
			var top = $(element).position().top;
			this.animate({scrollTop: top});
		};


		/**
		 * this method loads the children of the new contents
		 * into the target after emptying the contents
		 * with a fade in / fade out effect
		 */
		$.fn.replaceContents = function(/*the pane whose contents will be swapped in*/ newContents,  
										/*the layout for the target*/ layoutOptions){
			var target = this;
			$(target).fadeOut("fast", function(){
				$(target).empty().prepend($(newContents).children());
				$(target).fadeIn("fast");
				if(layoutOptions != null && layoutOptions != undefined){
					$(target).layout(layoutOptions);
				}
			});
			return $(target);
		};	

		/**
		 * Closes the dialog when the user clicks off of it.
		 */
		$.fn.closeOnLostFocus = function() { // on the open event
		    // find the dialog element
		    var dialogEl = this;        
		    $(document).click(function (e) { // when anywhere in the doc is clicked
		        var clickedOutside = true; // start searching assuming we clicked outside
		        $(e.target).parents().andSelf().each(function () { // search parents and self
		            // if the original dialog selector is the click's target or a parent of the target
		            // we have not clicked outside the box
		            if ($(this).first().attr("id") == $(dialogEl).attr("id")) {
		                clickedOutside = false; // found
		                return false; // stop searching
		            }
		        });
		        if (clickedOutside) {
		            $(dialogEl).dialog("close"); // close the dialog
		            // unbind this listener, we're done with it
		            $(document).unbind('click',arguments.callee); 
		        }
		    });
		};
		
		
		$.fn.bindEnterKey = function(func) {
			$(this).bind('keyup', function(e){
				if(e.keyCode==13){
		        	func(e);
			    }
			});		
			
			return this;
		};
		
		
		
		
	})();
});

