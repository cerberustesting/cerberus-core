/*
 * jQuery Loader Plugin
 * @version: 2.0.0
 * @requires jQuery v1.2.2 or later
 * @author : ${author}
 * @see : ${demoURL}
 * Small loader
 * usage : $.loader();
 * $.loader(options) -> options =
 *  {
 *		
 * }
 *
 * To close loader : $.loader("close");
 *
 */
var jQueryLoaderOptions = null;
(function($) {
	$.loader = function(option){
		switch(option)
		{
			case 'close':
				if(jQueryLoaderOptions){
					if($("#"+jQueryLoaderOptions.id)){
						$("#"+jQueryLoaderOptions.id +", #"+jQueryLoaderOptions.background.id).remove();
					}
				}
				return;
			case 'setContent':
				if(jQueryLoaderOptions){
					if($("#"+jQueryLoaderOptions.id)){
						if(arguments.length == 2)
						{
							$("#"+jQueryLoaderOptions.id).html(arguments[1]);
						}else{
							if(console){
								console.error("setContent method must have 2 arguments $.loader('setContent', 'new content');");
							}else{
								alert("setContent method must have 2 arguments $.loader('setContent', 'new content');");
							}
						}	
					}
				}
				return;
			default:
				var options = $.extend({
					content:"Loading ...",
					className:'loader',
					id:'jquery-loader',
					height:60,
					width:200,
					zIndex:30000,
					background:{
						opacity:0.4,
						id:'jquery-loader-background'
					}
				}, option);
		}
		jQueryLoaderOptions = options;
		var maskHeight = $(document).height();
		var maskWidth = $(window).width();
		var bgDiv = $('<div id="'+options.background.id+'"/>');
		bgDiv.css({
			zIndex:options.zIndex,
			position:'absolute',
			top:'0px',
			left:'0px',
			width:maskWidth,
			height:maskHeight,
			opacity:options.background.opacity
		});

		bgDiv.appendTo("body");
		if(jQuery.bgiframe){
			bgDiv.bgiframe();
		}
		var div = $('<div id="'+options.id+'" class="'+options.className+'"></div>');
		div.css({
			zIndex:options.zIndex+1,
			width:options.width,
			height:options.height
		});
		div.appendTo('body');
		div.center();
		div.html(options.content);
		//$(options.content).appendTo(div);
	};
	$.fn.center = function () {
		this.css("position","absolute");
	    this.css("top", ( $(window).height() - this.outerHeight() ) / 2+$(window).scrollTop() + "px");
	    this.css("left", ( $(window).width() - this.outerWidth() ) / 2+$(window).scrollLeft() + "px");
	    return this;
	};
})(jQuery);