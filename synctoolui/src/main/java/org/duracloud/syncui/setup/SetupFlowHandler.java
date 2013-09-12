/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.webflow.core.FlowException;
import org.springframework.webflow.execution.FlowExecutionOutcome;
import org.springframework.webflow.mvc.servlet.AbstractFlowHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author Daniel Bernstein 
 * 
 */
@Component(SetupFlowHandler.FLOW_ID)
public class SetupFlowHandler extends AbstractFlowHandler {

    private static Logger log = LoggerFactory.getLogger(SetupFlowHandler.class);

    public static final String FLOW_ID = "setup";

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
    public String handleException(FlowException e,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        log.error(e.getMessage(), e);
        return null;
    }
}
