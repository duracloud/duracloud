<%@include file="/WEB-INF/jsp/include.jsp"%>
<spring:message var="storageProviderName"
  code="${fn:toLowerCase(contentStore.storageProviderType)}" />

<c:set var="spaceId" value="${param['spaceId']}" />

<tiles:insertDefinition name="app-base">
  <tiles:putAttribute name="title">
      Duracloud :: Upload Tool  ::  ${storageProviderName} :: ${spaceId} 
  </tiles:putAttribute>
  <tiles:putAttribute name="header-extensions">
    <script id="deployJava" src="https://www.java.com/js/deployJava.js"></script>

    <!-- FIXME remove when applet loading bug is sorted out with chrome and safari
               and possibly early versions of firefox on mac -->
    <script type="text/javascript">
	$(function(){
          if(navigator.userAgent.match(/macintosh/i)){
	    $("#applet-refresh-prompt").show();
          }
	});  	
    </script>
    <!-- fix me end -->
  </tiles:putAttribute>
  <tiles:putAttribute name="body">

    <div id="page-header" class="outer">
      <div id="left" class="float-l">
        <div id="dc-logo-panel">
          <a href="/duradmin/spaces" id="dc-logo"></a><span
            id="dc-app-title"></span>
        </div>
      </div>
      <div id="right" class="float-r">
        <img class="float-r" id="dc-partner-logo"
          src="/duradmin/partnerlogo" />
        <div id="dc-user" class="float-r">
          ${pageContext.request.userPrincipal.name}
        </div>
      </div>
    </div>
    <div id="page-content" class="pane-L1-body">
  <div class="upload-tool">
      <c:choose>
        <c:when test="${not empty space}">
          <h2 style="margin-bottom:10px">
            Upload to ${space.spaceId} in  ${storageProviderName} 
          </h2>
          
          <script>
                                        <%--Illustrates how to pass params into the applet's
        init() method.
        See UploadToolApplet.java--%>
                                            var attributes = {
                                                id : 'uploader',
                                                width : 790,
                                                height : 190,
                                                MAYSCRIPT : 'true',

                                            };
                                            var parameters = {
                                                jnlp_href : '/duradmin/webstart/upload.jnlp',
                                                boxborder : 'false',
                                                host : '${pageContext.request.serverName}',
                                                port : '${pageContext.request.serverPort}',
                                                username : '${user.username}',
                                                spaceId : '${space.spaceId}',
                                                storeId : '${space.storeId}',
                                                session : '${user.password}',
                                            };
                                            deployJava.runApplet(attributes,
                                                    parameters, '1.6');

                                        </script>

          <script>
                                            
                                        <%--Illustrates how to make calls on the applet
        created above.--%>
                                            //this feature is not yet implemented.
                                            //uploader.setSpaceId('differentspaceid');
                                        </script>
          <%--End temp uploader section--%>
        </c:when>
        <c:otherwise>
          <div class="error">
            <h1>The upload tool could not be loaded</h1>
            <p>The designated space, "${spaceId}" does not exist in
              the designated storage provider (${storageProviderName}).</p>
          </div>

        </c:otherwise>
      </c:choose>
    </div>
  
    </div>
    <div class="ui-layout-south footer">
    <!-- FIXME remove when above fix me has been addressed -->
    <p id="applet-refresh-prompt" style="display:none;" class="footer-prompt">If this window appears blank, click here: <a class="featured button" href="">Refresh</a></p>
    <!-- fix me end -->
    </div>
  </tiles:putAttribute>
</tiles:insertDefinition>



