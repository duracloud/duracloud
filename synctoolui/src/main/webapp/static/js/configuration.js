/**
 * 
 * @author Daniel Bernstein
 */

$(function() {

    var addCancelButtonHandler = function(dialog) {
        $("#cancel", dialog).click(function(e) {
            $(dialog).dialog("close");
            e.preventDefault();
            return false;
        });
    }

    // add handler to open add directory dialog
    $("#add").click(function(e) {
        e.preventDefault();
        $("#add-dialog").dialog("open");
        return false;
    });

    // initialize add dialog
    $("#add-dialog")
            .dialog(
                    {
                        modal : true,
                        open : function() {
                            var dialog = this;
                            var addDirectoryUrl = "configuration/add";

                            // subroutine to handle replacing the dialog
                            // contents
                            var loadContent = function(jqxhr, dialog) {

                                // replace the content
                                $(dialog).empty().append($(jqxhr.responseText));

                                // attach cancel button listener
                                addCancelButtonHandler(dialog);
                                
                                $("#add", dialog).click(function(e){
                                    if($("#directoryPath").val() == ''){
                                        alert("You must select a directory or file.");
                                        e.preventDefault();
                                        setTimeout(function(){
                                            $("button",dialog).removeAttr("disabled", "disabled");
                                        },2);

                                    }
                                });
                            };

                            var jqxhr = $.get(addDirectoryUrl).done(function() {
                                loadContent(jqxhr, dialog);
                            });
                        },
                        position : "top",
                        autoOpen : false,
                        closeText : "",
                        width : "500px"

                    });

    $("#edit").click(function(e) {
        e.preventDefault();
        $("#edit-dialog").dialog("open");
        return false;

    });

    $("#edit-dialog").dialog(
            {
                modal : true,
                open : function() {
                    var dialog = this;

                    var loadContent = function(jqxhr, dialog) {
                        $(dialog).empty().append(
                                $(jqxhr.responseText).find(".section"));
                        addCancelButtonHandler(dialog);

                        // add next button handler
                        $("#next", dialog).click(function(e) {
                            var action = $("form", dialog).attr("action");
                            var data = $("form", dialog).serialize();
                            data += "&"+ $(e.target).attr("name");
                            var jqxhr = $.post(action, data).done(function() {
                                loadContent(jqxhr, dialog);
                            });

                            e.preventDefault();
                            // return false;
                        });
                    };

                    var jqxhr = $.get("duracloud-config").done(function() {
                        loadContent(jqxhr, dialog);
                    });

                },
                position : "top",
                autoOpen : false,
                closeText : "",
                width : "500px"

            });

    $("#options input").change(function(e){
        $(this).closest("form").submit();
    });
});