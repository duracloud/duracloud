<div class="float-r" id="logo-ds"></div>
Duracloud Administrator Release v${project.version} rev:${buildNumber} <span class="sep">|</span>
&copy;<script type="text/javascript">document.write(new Date().getFullYear());</script>

<jsp:useBean id="duradminConfig"
             class="org.duracloud.duradmin.config.DuradminConfig"
             type="org.duracloud.duradmin.config.DuradminConfig"
             scope="session"/>

<a target="_blank" href="http://duracloud.org">DuraCloud</a>  <span class="sep">|</span>
<a target="_blank" href="http://lyrasis.org">LYRASIS</a>  <span class="sep">|</span>

<c:if test="${!empty duradminConfig.amaUrl && duradminConfig.amaUrl != 'null'}">
    <a target="_blank" href="${duradminConfig.amaUrl}">Management Console</a>  <span class="sep">|</span>
</c:if>
<a target="_blank" href="https://wiki.lyrasis.org/display/DURACLOUD/DuraCloud+Help+Center">Help Center</a> <span class="sep">|</span>
<a target="_blank" href="https://lyrasis.zendesk.com/">Support</a>
