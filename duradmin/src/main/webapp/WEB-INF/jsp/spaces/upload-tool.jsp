<%@include file="/WEB-INF/jsp/include.jsp"%>
    <spring:message var="storageProviderName"
      code="${fn:toLowerCase(contentStore.storageProviderType)}" />

    <c:set var="spaceId"
      value="${param['spaceId']}" />

<tiles:insertDefinition name="app-base">
  <tiles:putAttribute name="title">
      Duracloud :: <spring:message code="bulkUploadTool" />  ::  ${storageProviderName} :: ${spaceId} 
  </tiles:putAttribute>
  <tiles:putAttribute name="header-extensions">
    <script src="https://www.java.com/js/deployJava.js"></script>
  </tiles:putAttribute>
  <tiles:putAttribute name="body">
  <div id="upload-tool">
    <c:choose>
      <c:when test="${not empty space}">
        <h1>
          <spring:message code="bulkUploadTool" />
          :: ${storageProviderName}
          :: ${space.spaceId}
        </h1>

        <script>
        <%--Illustrates how to pass params into the applet's
        init() method.
        See UploadToolApplet.java--%>
            var attributes = {
                id : 'uploader',
                width : 900,
                height : 200
            };
            var parameters = {
                jnlp_href : '/duradmin/webstart/upload.jnlp',
                boxborder : 'false',
                host : '${pageContext.request.serverName}',
                port : '${pageContext.request.serverPort}',
                username : '${user.username}',
                password : '${user.password}',
                spaceId : '${space.spaceId}',
                storeId : '${space.storeId}'

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
        <h1>The Bulk Upload Tool could not be loaded</h1>
        <p>The designated space, "${spaceId}" does not exist in your primary storage provider (${storageProviderName}).</p>
        </div>
       
      </c:otherwise>
    </c:choose>
   </div>
    
  </tiles:putAttribute>
</tiles:insertDefinition>



