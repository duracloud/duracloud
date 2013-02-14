/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.duracloudconfig;

import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.FlowExecutionOutcome;
import org.springframework.webflow.mvc.servlet.AbstractFlowHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author Daniel Bernstein 
 * 
 */
@Component(DuracloudConfigFlowHandler.FLOW_ID)
public class DuracloudConfigFlowHandler extends AbstractFlowHandler {
    public static final String FLOW_ID = "duracloud-config";
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
}
