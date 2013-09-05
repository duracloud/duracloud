<?xml version="1.0" encoding="ISO-8859-1"?>
<%-- Copyright (c) 2009-2013 DuraSpace. All rights reserved.--%>
<%-- messages display
--%>
<%-- Author: Daniel Bernstein --%><%@include file="./libraries.jsp"%>


<fieldset>
  <legend>Deletion Policy</legend>
  <ul>
    <li><label
      title="Check this box if you wish that deletes performed on files within the directories below also be performed on those files in DuraCloud.">
        <form:checkbox path="syncDeletes" /> Sync
        deletes
    </label></li>
  </ul>
</fieldset>
<fieldset>
  <legend>Update Policy</legend>
  <ul>
    <li><label> <form:radiobutton
          path="updatePolicy"
          value="OVERWRITE" />Overwrite existing content
    </label></li>
    <li><label><form:radiobutton
          path="updatePolicy"
          value="NONE" />Do not sync updates</label></li>
    <li><label><form:radiobutton
          path="updatePolicy"
          value="PRESERVE" />Update but do not overwrite (preserve original in
        cloud)</label></li>
  </ul>
</fieldset>


