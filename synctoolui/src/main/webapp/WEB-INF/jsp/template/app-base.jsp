<?xml version="1.0" encoding="ISO-8859-1"?>
<%@include file="../include/libraries.jsp"%>
<div>
  <div id="header">
    <div id="logo">SYNC TOOL</div>
    <tiles:insertAttribute name="subHeader" />
  </div>

  <div id="content">
    <tiles:insertAttribute name="content" />
  </div>

  <div id="footer">
    <ul>
      <li>DuraCloud Sync v${project.version} ${buildNumber}</li>
      <li><a href="http://www.duracloud.org">DuraCloud</a></li>
      <li><a href="http://www.duraspace.org">DuraSpace</a></li>
    </ul>
  </div>
</div>

