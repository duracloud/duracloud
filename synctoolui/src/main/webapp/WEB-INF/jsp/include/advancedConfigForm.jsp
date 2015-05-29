<?xml version="1.0" encoding="ISO-8859-1"?>
<%-- Copyright (c) 2009-2013 DuraSpace. All rights reserved.--%>
<%-- messages display
--%>
<%-- Author: Daniel Bernstein --%><%@include file="./libraries.jsp"%>


<fieldset>
  <legend>Deletion Policy</legend>
  <ul>
    <li><label
      title="Check this box if you wish that deletes performed on files within the directories below also be performed on those files in DuraCloud. Note: It cannot be used in conjunction with the 'Update, but do not overwrite' policy.">
        <form:checkbox id="syncDeletes" path="syncDeletes" disabled="${advancedForm.updatePolicy == 'PRESERVE'}"/> Sync deletes</label>
    </li>
  </ul>
</fieldset>
<fieldset>
  <legend>Update Policy</legend>
  <ul>
    <li><label> <form:radiobutton
          id="overwritePolicy"
          path="updatePolicy"
          value="OVERWRITE" />Overwrite existing content
    </label></li>
    <li><label><form:radiobutton
          id="noUpdatePolicy"
          path="updatePolicy"
          value="NONE"
          disabled="${advancedForm.jumpStart}" />Do not sync updates</label></li>
    <li><label><form:radiobutton
          id="preserveUpdatePolicy"
          path="updatePolicy"
          value="PRESERVE"  
          disabled="${advancedForm.syncDeletes || advancedForm.jumpStart}"
          />Update but do not overwrite (preserve original in
        cloud)</label></li>
  </ul>
</fieldset>
<fieldset>
  <legend>Jump Start</legend>
  <ul>
    <li><label
      title="This option will accelerate uploads by skipping the checks to see if the content already exists in DuraCloud. The 'jump start' feature is especially appropriate for the initial upload of collections with large numbers of very small files. Note:  It is only available when the policy is set to overwrite updates.">
        <form:checkbox id="jumpStart" path="jumpStart" disabled="${advancedForm.updatePolicy != 'OVERWRITE'}"/> Accelerate initial upload of files with jump start
    </label></li>
  </ul>
</fieldset>
<script>
	var syncDeletesCB  = $("#syncDeletes");
	syncDeletesCB.unbind().change(function(){
	  var checked = syncDeletesCB.is(":checked");
	    if(checked){
            if(!confirm("Warning: 'Sync Deletes' can be a very useful feature, but one that can cause unexpected side effects " +
                        "for those unfamiliar with its characteristics. Please understand that enabling this feature will "+
                        "ensure that deletes made to your local copy will be propagated to DuraCloud. In the event that "+
                        "a directory is renamed locally, all content under the original directory will be deleted from DuraCloud " +
                        "and then re-uploaded using the new directory path (assuming the new directory path is within any of " +
                        "the watched directories you've configured in the DuraCloud Sync Tool).\n\nSimilarly, if the Sync Tool is " +
                        "watching content on a locally mounted drive which is then unmounted (either intentionally or otherwise) " +
                        "the Sync Tool will see that the content no longer exists locally and will delete the copies of that " +
                        "content in DuraCloud. Click 'OK' if you are comfortable with the 'Sync Delete' behavior.\n\n")){
	         syncDeletesCB.prop("checked", false);
	         return false;
	       };
	    }
	      
		$("#preserveUpdatePolicy").prop("disabled",checked);
	});
	
    $("#jumpStart").change(function(){
      var jumpstartChecked = $("#jumpStart").is(":checked");
      $("#preserveUpdatePolicy").prop("disabled", jumpstartChecked);
      $("#noUpdatePolicy").prop("disabled",jumpstartChecked);
    });
	
	$("[name='updatePolicy']").change(function(){
		$("#syncDeletes").prop("disabled",($("#preserveUpdatePolicy").is(":checked")));
	    $("#jumpStart").prop("disabled",!$("#overwritePolicy").is(":checked"));
	});

	
	</script>


