<?xml version="1.0" encoding="ISO-8859-1"?>
<%-- Copyright (c) 2009-2015 DuraSpace. All rights reserved.--%>
<%-- messages display
--%>
<%-- Author: Daniel Bernstein --%>
<%@include file="./libraries.jsp"%>



<fieldset>
  <legend>Run Modes</legend>
  <p>
     You may run your synchronization operations in one of two modes:  continuous or single pass. 
     In continuous mode, DuraCloudSync will continue indefinitely to watch for additions, updates, and deletions to the file system
     after adding all the files in your watched directories when the sync starts.  In the single pass mode, the application will 
     not continue to watch for changes after making the initial pass of your configured directories and/or files.
  </p>
  <ul>
    <li><label> <form:radiobutton
          id="continuousMode"
          path="mode"
          value="CONTINUOUS" />Run continuously
    </label></li>
      <li><label> <form:radiobutton
          id="singlePassMode"
          path="mode"
          value="SINGLE_PASS" />Single pass only
        </label>
      </li>
  </ul>
</fieldset>


