<%-- Copyright (c) 2009-2013 DuraSpace. All rights reserved.--%>
<%-- Status Page: displays configuration information for the synchronization process. --%>
<%-- Author: Daniel Bernstein --%>

<%@include file="./include/libraries.jsp"%>
<tiles:insertDefinition
  name="app-base"
  flush="true">
  <tiles:putAttribute name="primaryTab" value="configuration" cascade="true"/>
  <tiles:putAttribute
    name="content"
    cascade="true">

    <div class="yui3-g">

      <div
        id="watched-directories"
        class="yui3-u-1 ">
        <div class="content">
          <div class="section">
            <div class="header">
              <span> <spring:message code="watchedDirectories" />
              </span>
              <ul class="button-bar">
                <li>
                  <a id="add" class="button">
                    <spring:message code="add"/>
                  </a>
                </li>
              </ul>
              
            </div>
            <div class="body">
              <table id="directories" role="presentation">
                <tbody>
                  <c:choose>
                    <c:when test="${not empty directoryConfigs}">
                      <c:forEach
                        items="${directoryConfigs}"
                        var="dc">
                        <tr>
                          <td>${dc.directoryPath}</td>
                          <td>
                            <form action="configuration/remove" method="post">
                              <input type="hidden" name="directoryPath" value = "${dc.directoryPath}"/>
                              <button id="${dc.file.name}-remove" class="trash" type="submit" title="remove">Remove</button>
                            </form>
                          </td>
                        </tr>
                      </c:forEach>
                    </c:when>
                    <c:otherwise>
                      <p class="error">There are no configured directories at this time. Please be aware that 
                      you must configure <em>at least one file or directory</em> in order sync content to DuraCloud.</p>
                    </c:otherwise>
                  </c:choose>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>      
    
      <div
        id="duracloud-configuration"
        class="yui3-u-1 ">
        <div class="content">
          <div class="section">
            <div class="header">
              <span> <spring:message code="duracloudConfiguration" /></span>
              <ul class="button-bar">
                <li>
                  <a id="edit" class="button">
                    <spring:message code="edit"/>
                  </a>
                </li>
              </ul>
              <div id="add-dialog" class="dialog" style="display:none"></div>
              <div id="edit-dialog" class="dialog" style="display:none"></div>
              <div id="optimize-dialog" class="dialog" style="display:none"></div>

            </div>
            <div class="body">
              <table role="presentation">
                <tr>
                  <td><spring:message code="host"/></td>
                  <td>${duracloudConfiguration.host}</td>
                </tr>
                <c:if test="${not duracloudConfiguration.defaultPort}">
                  <tr>
                    <td><spring:message code="port"/></td>
                    <td>${duracloudConfiguration.port}</td>
                  </tr>
                </c:if>
                <tr>
                  <td><spring:message code="username"/></td>
                  <td>${duracloudConfiguration.username}</td>
                </tr>
                <tr>
                  <td><spring:message code="spaceId"/></td>
                  <td>${duracloudConfiguration.spaceId}</td>
                </tr>
              </table>
            </div>
          </div>
        </div>
      </div>    
      <div
        id="policies"
        class="yui3-u-1 options">
        <div class="content">
          <div class="section ">
            <div class="header">
              <span> <spring:message code="options" text="Policies" /></span>
              <ul class="button-bar">
              </ul>
              
            </div>
            <div class="body">
              <form:form
                method="post"
                modelAttribute="advancedForm"
                action="${pageContext.request.contextPath}/configuration/advanced">
                <jsp:include page="./include/advancedConfigForm.jsp"/>
              </form:form>
            </div>
          </div>
        </div>
      </div>

      <div
        id="other"
        class="yui3-u-1 options">
        <div class="content">
          <div class="section">
            <div class="header">
              <span> <spring:message code="otherOptions" text="Other Options" /></span>
              <ul class="button-bar">
              </ul>
              
            </div>
            <div class="body">
              <form:form
                method="post"
                modelAttribute="modeForm"
                action="${pageContext.request.contextPath}/configuration/mode">
                <jsp:include page="./include/modeForm.jsp"/>
              </form:form>
            
              <fieldset>
                <legend>Destination Prefix</legend>
                <form:form
                  method="post"
                  modelAttribute="prefixForm"
                  action="${pageContext.request.contextPath}/configuration/prefix">

                    <label for="prefix">Optionally specify a prefix that is added to the beginning of
                                the ID of each content item that is stored
                                in DuraCloud. For example, a prefix value
                                of 'a/b/c/' with a content item whose path
                                is 'dir1/file.txt' would result in the
                                file stored in DuraCloud as
                                'a/b/c/dir1/file.txt'</label>
                    <form:input size="50%" placeholder="your/directory/path/here" path="prefix"/>
                </form:form>
              </fieldset>
              <fieldset>
                <legend>Max File Size</legend>
                <form:form
                  method="post"
                  modelAttribute="maxFileSizeForm"
                  action="${pageContext.request.contextPath}/configuration/max-file-size">

                    <label for="maxFileSizeInGB">The maximum size of a stored file in GB.</label>
                    <form:select path="maxFileSizeInGB">
                        <c:forEach items="${maxFileSizeForm.values}" var="val">
                            <form:option value="${val}">${val} GB</form:option>
                        </c:forEach>
                    </form:select>
                </form:form>
              </fieldset>

              <fieldset>
                <legend>Transfer Rate (Thread count)</legend>
              
                <form:form
                  method="post"
                  modelAttribute="threadCountForm"
                  action="${pageContext.request.contextPath}/configuration/thread-count">

                
                  <p  >Depending on your system's hardware and network,
                    you may be able to increase your transfer rate by increasing the number of uploader threads working in parallel. </p>


                  <c:set var="optimizing" value="${syncOptimizeManager.running}"/>
                  <c:set var="failed" value="${syncOptimizeManager.failed}"/>
                  <c:set var="status" value="${syncOptimizeManager.status}"/>
                  
                  <c:set var="syncRunning" value="${syncProcessState.toString() != 'STOPPED'}"/>
                  <label for="threadCount">Threads
                  <form:select
                    path="threadCount"
                    disabled="${optimizing}">
                    <c:forEach
                      begin="1"
                      end="50"
                      var="i">
                      <form:option value="${i}">${i}</form:option>
                    </c:forEach>
                  </form:select>
                  </label>
                  <div id="optimize-now">
                  <form:button id="optimize" disabled="${optimizing or syncRunning }">Optimize Automatically</form:button>
                  </div>
                    <div id="syncOptimizeStatus">
                    
                    <c:choose>
                      <c:when test="${syncRunning}">
                                            <div class="message warning">
                        <h2>Sync Running</h2>
                        <p>
                         To run the sync optimizer, you must stop the sync process in 
                         order to ensure accurate transfer speed measurements.
                        </p>
                      </div>
                      
                      
                      </c:when>
                      <c:otherwise>
                        <c:if test="${not empty status}">
                          <div class="message ${failed ? 'error' : 'info' }">
                              <p>
                              ${syncOptimizeManager.status}
                              </p>
                          </div>
                        </c:if>
                      </c:otherwise>
                    </c:choose>
                    </div>
                  
                  
                  

                </form:form>
                    
                    
              </fieldset>

            </div>
          </div>
        </div>
      </div>


    </div>
    
    <script src="${pageContext.request.contextPath}/static/js/configuration.js"></script>
    

  </tiles:putAttribute>
</tiles:insertDefinition>

