<%-- Copyright (c) 2009-2013 DuraSpace. All rights reserved.--%>
<%-- Template base for all pages --%>
<%-- Author: Daniel Bernstein --%>

<%@include file="../include/libraries.jsp"%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
<jsp:directive.page contentType="text/html; charset=utf-8" />
<head>
<title>DuraCloud Sync</title>
<link
  rel="stylesheet"
  type="text/css"
  href="${pageContext.request.contextPath}/static/css/global.css" />

<link
  rel="stylesheet"
  type="text/css"
  href="http://yui.yahooapis.com/3.5.1/build/cssgrids/grids-min.css"/>
<link rel="shortcut icon" href="${pageContext.request.contextPath}/static/favicon.ico" />
<script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jquery-ui.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/static/js/jqueryFileTree.js"></script>
 
<link
  rel="stylesheet"
  type="text/css"
  href="${pageContext.request.contextPath}/static/js/jqueryFileTree.css"/>

  <tiles:insertAttribute name="head-extension" ignore="true"/>

</head>

<body>
  <tiles:insertAttribute name="body" />

      <script>
        $(function(){
            $("button").live("click",function(evt){
                setTimeout(function(){
                    $("button").attr("disabled", "disabled");
                    //$(evt.target).html("<i class='working'></i>");
                },1);
            });
        });
        
        //I put this here because it is used both in the setup and duracloud-config wizards.
        $("#advanced").live("click",function(e){
            $(this).fadeOut();
            $("#portListItem").slideDown();
        });

      </script>

</body>
</html>
