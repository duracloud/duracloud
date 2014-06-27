<%-- Copyright (c) 2009-2013 DuraSpace. All rights reserved.--%>
<%-- Status Page: displays errors for the synchronization process. --%>
<%-- Author: Daniel Bernstein --%>

<%@include file="./include/libraries.jsp"%>
<tiles:insertDefinition
  name="app-base"
  flush="true">
  <tiles:putAttribute
    name="content"
    cascade="true">
    
    <div class="yui3-g">
      <div
        class="yui3-u-1 ">
        <div class="content">
          <div class="section">
            <div class="header">
              Oops: Something went wrong!
            </div>
            <div class="body">
              <spring:message var="version" code="version"/>
              <spring:message var="revision" code="revision"/>

              <img src="${pageContext.request.contextPath }/static/image/crash.jpg" height="200"/>
                  <p><strong>${message}</strong></p>
<c:url
var="mailto"
value=
"mailto:support@duracloud.org?subject=[SyncTool] ${message}&body=
[your comments here]%0D%0A
****************%0D%0A
version:${version}%0D%0A
revision:${revision}%0D%0A
url:${requestScope['javax.servlet.forward.request_uri']}%0D%0A
querystring:${requestScope['javax.servlet.forward.query_string']}%0D%0A
${stackTrace}"/>
 
                  <a
                    class="button"
                    href="${mailto}">Send a message to DuraCloud support</a>
                  
                  <c:if test="${not empty stackTrace}">
                  <p><a class="button" id="showDetails">Details</a></p>
                  <div id="details" style="border-radius: 5px; width:50%; height:200px; padding:20px; background-color: #999; color: white; margin-bottom:10px; overflow:scroll; display:none">
                    <pre>${stackTrace }</pre>
                    
                  </div>
                  <script>
                  	$(function(){
                  	    $("#showDetails").click(function(e){
                  	        $("#details").slideToggle();
                  	        e.preventDefault();
                  	    });
                  	});
                  </script>
                  
                  </c:if>
            </div>
          </div>
        </div>
        </div>
        </div>
        </tiles:putAttribute>
</tiles:insertDefinition>

