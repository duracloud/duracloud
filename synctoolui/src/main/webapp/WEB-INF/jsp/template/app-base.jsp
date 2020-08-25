<?xml version="1.0" encoding="ISO-8859-1"?>
<%@include file="../include/libraries.jsp"%>
<div>
  <div class="message-holder">
    <c:if test="${not empty messageInclude}">
      <div class="message info">
        <jsp:include page="../include/message/${messageInclude}.jsp"/>
        <a id="close-link" href="#">close</a>
        <script>
          $("#close-link").click(function() {
              $(".message-holder").fadeOut();
          });
          
          setTimeout(function(){
              $(".message-holder").fadeOut();
          }, 7000);
        </script>
      </div>
    </c:if>
  </div>

  <div id="header">
    <div id="logo"><img src="${pageContext.request.contextPath }/static/image/logo_top_duracloud.png" alt="DURACLOUD"/>SYNC TOOL</div>
    <tiles:insertAttribute name="subHeader" />
  </div>

  <div id="content">
    <tiles:insertAttribute name="content" />
  </div>

  <div id="footer">
    <ul>
      <li>DuraCloud Sync v<spring:message code="version"/> rev <spring:message code="revision"/></li>
      <li><a href="http://www.duracloud.org">DuraCloud</a></li>
      <li><a href="http://www.duraspace.org">DuraSpace</a></li>
    </ul>
  </div>
</div>

