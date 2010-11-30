/**
 * Dashboard Manager
 * 
 * @author Daniel Bernstein
 */
var centerLayout


$(document).ready(function() {
	centerLayout = $('#page-content').layout({
	// minWidth: 300 // ALL panes
		north__size: 			50	
	,	north__paneSelector:     ".center-north"
	,   north__resizable:   false
	,   north__slidable:    false
	,   north__spacing_open:			0			
	,	north__togglerLength_open:		0			
	,	north__togglerLength_closed:	0			

	,   west__size:				400
	,	west__minSize:			400
	,   west__resizable:   true
	,   west__slidable:    true

	,   west__paneSelector:     "#dynamic-panel"
	,	center__paneSelector:	"#static-panel"
	});
	
	var users = $("#current-users").expandopanel({title: "Current Users"});
	$(users).expandopanel("getContent").append("<h3>Coming soon.</h3>");

	var announcements = $("#announcements").expandopanel({title: "Announcements"});
	/*
	   $.getFeed({
	       url: '/duradmin/feed?url=http://twitter.com/statuses/user_timeline/38821410.rss',
	       success: function(feed) {
			 var content = $(announcements).expandopanel("getContent");
			 content.append("<h3>"+ feed.title + "</h3>");
	       }
	   });
	*/
	 var content = $(announcements).expandopanel("getContent");
	 content.append("<h3>Coming soon.</h3>");

});