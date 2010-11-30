/**
 * created by Daniel Bernstein
 */

/////////////////////////////////////////////////////////////////////////////////////
///on off switch  widget
///
/////////////////////////////////////////////////////////////////////////////////////
$.widget("ui.onoffswitch",{
	/**
	 * Default values go here
	 */
	options: {
      initialState: "on"
		, onStateClass: "on left"
		, onIconClass: "unlock"
		, offStateClass: "off right"
		, offIconClass: "lock"
		, onText: "On"
		, offText: "Off"
	},
	
	
	/**
	 * Initialization 
	 */
	_init: function(){
		var that = this;
		//clear the element state
		$(that.element).html("");
		var o = this.options;
		var leftButtonOnState = this._createButton(o.onText, o.onStateClass, o.onIconClass, false);
		var rightButtonOnState = this._createButton(o.offText, o.offStateClass, o.offIconClass, true);
		var leftButtonOffState = this._createButton(o.onText, o.onStateClass, o.onIconClass, true);
		var rightButtonOffState = this._createButton(o.offText, o.offStateClass, o.offIconClass, false);
		
		rightButtonOnState.click(function(evt){
			evt.preventDefault();
			that._fireOffEvent();
		});
		
		var onState = $(document.createElement("span"))
							.addClass("button-holder")
							.addClass("button-holder-on")
							.append(leftButtonOnState)
							.append(rightButtonOnState);
		
		$(this.element).append(onState);


		leftButtonOffState.click(function(evt){
			evt.preventDefault();
			that._fireOnEvent(evt);
		});
		var offState = $(document.createElement("span"))
							.addClass("button-holder")
							.addClass("button-holder-off")
							.append(leftButtonOffState)
							.append(rightButtonOffState);
		$(this.element).append(offState);

		
		this._switch(o.initialState);
	},
	
	_fireOnEvent: function(evt){
		var that = this;
		this.element.trigger("turnOn", {
			success:function(evt){ 
				that._switch("on")
			},
			
			failure: function(evt){
				alert("no turn on failure handler defined");
			},
		});
	},

	_fireOffEvent: function(){
		var that = this;
		this.element.trigger("turnOff", {
			success:function(evt){ 
				that._switch("off")
			},
			
			failure: function(evt){
				alert("no turn offfailure handler defined");
			},
		});
	},

	_switch: function(state){
		$(".button-holder", this.element).css("display","none");

		if(state =="on"){
			$(".button-holder-on", this.element).css("display","inline-block");
		}else{
			$(".button-holder-off", this.element).css("display","inline-block");
		}
	},
	
	on: function(){
		this._switch("on");
		this._fireOnEvent();
	},
	
	_createButton: function(text, stateClass, iconClass, clickable)
	{
		var button;
		var icon = $(document.createElement("i"))
			.addClass("pre")
			.addClass(iconClass);
		if (clickable)
		{
			button = $(document.createElement("button"));
		}
		else
		{
			button = $(document.createElement("span"));	
		}
		button
			.addClass("switch")
			.addClass(stateClass) // need to change to be "left" for on, "right" for off
			.append(icon)
			.append(text);
		return button;
	}
});


//an open close switch
$.widget("ui.accessswitch", 
		$.extend({}, $.ui.onoffswitch.prototype, 
			{  //extended definition 
				_init: function(){ 
					$.ui.onoffswitch.prototype._init.call(this); //call super init first
				}, 
				
				
				destroy: function(){ 
					$.ui.onoffswitch.prototype.destroy.call(this); // call the original function 
				}, 
				
				options: $.extend({}, $.ui.onoffswitch.prototype.options, 
						{
					  	      initialState: "on"
							, onStateClass: "on left"
							, onIconClass: "unlock"
							, offStateClass: "off right"
							, offIconClass: "lock"
							, onText: "Open"
							, offText: "Closed"
						}
				),
			}
		)
	);



