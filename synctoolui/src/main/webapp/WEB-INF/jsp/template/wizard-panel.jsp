<%@include file="../include/libraries.jsp"%>
<div class="wizard-panel" >
  <div class="section">
    <div class="header">
      <span> <tiles:insertAttribute name="panelTitle" />
      </span>

    </div>
    <div class="body">
      <p class="notice">
        <tiles:insertAttribute name="panelMessage" />
      </p>

      <tiles:insertAttribute name="panelContent" />
      
    </div>
  </div>
</div>
