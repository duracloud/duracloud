<div class="float-r" id="logo-ds"></div>
Duracloud Administrator Release v${project.version} ${prefix.revision}${prefix.specialStatus} <span class="sep">|</span>
&copy;<script type="text/javascript">document.write(new Date().getFullYear());</script>

<jsp:useBean id="duradminConfig"
             class="org.duracloud.duradmin.config.DuradminConfig"
             type="org.duracloud.duradmin.config.DuradminConfig"
             scope="session"/>

<a target="_blank" href="http://www.duracloud.org">DuraCloud</a>  <span class="sep">|</span>
<a target="_blank" href="http://www.duraspace.org">DuraSpace</a>  <span class="sep">|</span>

<c:if test="${!empty duradminConfig.amaUrl && duradminConfig.amaUrl != 'null'}">
    <a target="_blank" href="${duradminConfig.amaUrl}">Management Console</a>  <span class="sep">|</span>
</c:if>
<a target="_blank" href="https://wiki.duraspace.org/display/DURACLOUD/DuraCloud+Help+Center">Help Center</a> <span class="sep">|</span>
<a target="_blank" href="mailto:info@duracloud.org">Contact Us</a>
