/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.duracloudconfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.duracloud.syncui.domain.DuracloudConfiguration;
import org.duracloud.syncui.domain.DuracloudCredentialsForm;
import org.duracloud.syncui.domain.SpaceForm;
import org.duracloud.syncui.service.SyncConfigurationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.FlowExecutionOutcome;
import org.springframework.webflow.mvc.servlet.AbstractFlowHandler;

/**
 * 
 * @author Daniel Bernstein 
 * 
 */
@Component(DuracloudConfigFlowHandler.FLOW_ID)
public class DuracloudConfigFlowHandler extends AbstractFlowHandler {
    public static final String FLOW_ID = "duracloud-config";
    private SyncConfigurationManager syncConfigurationManager;
    
    @Autowired
    public DuracloudConfigFlowHandler(SyncConfigurationManager synConfigurationManager){
        this.syncConfigurationManager = synConfigurationManager;
    }
    
    @Override
    public String getFlowId() {
        return FLOW_ID;
    }
    
    @Override
    public String handleExecutionOutcome(FlowExecutionOutcome outcome,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        return "contextRelative:/";
    }
    
    @Override
    public MutableAttributeMap
        createExecutionInputMap(HttpServletRequest request) {
        MutableAttributeMap map =  super.createExecutionInputMap(request);
        if(map == null){
            map = new LocalAttributeMap();
        }
        DuracloudConfiguration config =
            this.syncConfigurationManager.retrieveDuracloudConfiguration();
        DuracloudCredentialsForm cf = new DuracloudCredentialsForm(config);
        
        map.put("duracloudCredentialsForm", cf);
        
        SpaceForm sf = new SpaceForm();
        sf.setSpaceId(config.getSpaceId());
        map.put("spaceForm", sf);
        
        return map;
    }
}
