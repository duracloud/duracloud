/**
 * 
 * created by Daniel Bernstein
 */

var centerLayout;
var usersListPane;
var detailPane;
$(document).ready(function() {

	//alert("starting jquery execution");
	var userDetailPaneId = "#user-detail-pane";
	var detailPaneId = "#detail-pane";
	var usersListViewId = "#users-list-view";
	var usersListId = "#users-list";
	
	centerLayout = $('#page-content').layout({
		west__size:				400
	,   west__paneSelector:     usersListViewId
	,   west__onresize:         "usersListPane.resizeAll"
	,	center__paneSelector:	detailPaneId
	,   center__onresize:       "detailPane.resizeAll"
	});
	
	
	
	var usersListPaneLayout = {
			north__paneSelector:	".north"
		,   north__size: 			35
		,	center__paneSelector:	".center"
		,   resizable: 				false
		,   slidable: 				false
		,   spacing_open:			0			
		,	togglerLength_open:		0	
	};
			
	usersListPane = $(usersListViewId).layout(usersListPaneLayout);
	
	//detail pane's layout options
	var detailLayoutOptions = {
				north__paneSelector:	".north"
				,   north__size: 			120
				,	center__paneSelector:	".center"
				,   resizable: 				false
				,   slidable: 				false
				,   spacing_open:			0
				,	togglerLength_open:		0
				
	};
	
	detailPane = $(detailPaneId).layout(detailLayoutOptions);
	
	var loadUserDetail = function(user){
		
		if(user == null){
			
			$(detailPaneId).fadeOut("slow", function(){
				$(this).html('');
			});
			return;
		};
		var userDetailPane = $(userDetailPaneId).clone();

		
		//set the title of the pane
		$(".user-name", userDetailPane.first()).html(user.username);
		
		var centerPane = $(".center",userDetailPane.first());
		centerPane.html("");
		
		/*
		var userDetails = new Array();
		for(i in user){
			userDetails.push([i, user[i]]);
		}
		
		centerPane.append(
			$.fn.create("div")
			.tabularexpandopanel(
				{title: "Details", 
				 data: userDetails,
                 }
			)
		);
		*/
		$(".change-password-button", userDetailPane).click(function(evt){
			var d = $("#change-password-dialog");
			$("#username",d).val(user.username);
			$(".username",d).html(user.username);
			d.dialog("open");
		});

		$(".delete-user-button", userDetailPane).click(function(evt){
			$.ajax({ url: "/duradmin/admin", 
				dataType: "json",
				method: "POST",
				data: "username="+user.username+"&verb=remove",
				cache: false,
				context: document.body, 
				success: function(data){
					$(usersListId).selectablelist("removeById", data.user.username);
					$(detailPaneId).html('');
				},
			    error: function(xhr, textStatus, errorThrown){
					alert("unable to delete user: " + textStatus);
			    },
			});		
			
		});

		$(detailPaneId).replaceContents(userDetailPane, detailLayoutOptions);



		
	};
	

	

	var loadUserList = function(users){
		var usersList = $(usersListId);
		usersList.selectablelist({selectable: false});
		usersList.selectablelist("clear");
		
		if(users == null || users == undefined || users.length == 0){
			usersList.append($.fn.create("span").addClass("dc-message").html("No users defined."));
			return;
		}
		
		var defaultSet = false;
		
		for(i in users){
			var user = users[i];
			insertUserIntoList(user);
			if(!defaultSet){
				loadUserDetail(user);
				defaultServiceSet = true;
			}
		}
		
		//bind for current item change listener
		usersList.bind("currentItemChanged", function(evt,state){
			loadUserDetail(state.data);
		});

	};
	
	var insertUserIntoList = function(user){
		var item =  $.fn.create("div");
		var actions = $.fn.create("div");
		item.attr("id", user.username)
			   .html(user.username)
			   .append(actions);
		$(usersListId).selectablelist('addItem',item,user);	   
	};
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Dialogs
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	$('#add-user-dialog').dialog({
		autoOpen: false,
		show: 'fade',
		hide: 'fade',
		resizable: false,
		height: 300,
		closeOnEscape:true,
		modal: true,
		width:700,
		
		buttons: {
			Add: function(){
				var that = this;
				$(that).dialog("disable");
				$.ajax({ url: "/duradmin/admin", 
					dataType: "json",
					method: "POST",
					data: $("form", this).serialize(),
					cache: false,
					context: document.body, 
					success: function(data){
						$(that).dialog("enable");
						$(that).dialog("close");
						insertUserIntoList(data.user);
						loadUserDetail(data.user);
					},
				    error: function(xhr, textStatus, errorThrown){
						$(that).dialog("enable");
						alert("unable to add user: " + textStatus);
				    },
				});		
			},
			Cancel: function(){
				$(this).dialog("close");
			},
		
		},
		close: function() {
	
		},
		
		open: function(e){	
		},
		
	});

	
	
	$(".add-user-button").click(function(){
		$("#add-user-dialog").dialog("open");
	});

	
	$('#change-password-dialog').dialog({
		autoOpen: false,
		show: 'fade',
		hide: 'fade',
		resizable: false,
		height: 200,
		closeOnEscape:true,
		modal: true,
		width:600,
		
		buttons: {
			Save: function(){
				var that = this;
				$(that).dialog("disable");
				$.ajax({ url: "/duradmin/admin", 
					dataType: "json",
					method: "POST",
					data: $("form", this).serialize(),
					cache: false,
					context: document.body, 
					success: function(data){
						$(that).dialog("close");
						$(that).dialog("enable");
					},
				    error: function(xhr, textStatus, errorThrown){
						$(that).dialog("enable");
						alert("unable to change password: " + textStatus);
				    },
				});		
			},
			Cancel: function(){
				$(this).dialog("close");
			},
		
		},
		close: function() {
	
		},
		
		open: function(e){	
		},
		
	});



	$(".ui-dialog-titlebar").hide();

	$("#page-content").glasspane({});
	
	loadUserList(users);
});