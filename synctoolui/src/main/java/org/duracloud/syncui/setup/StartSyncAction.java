/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.setup;

import org.duracloud.syncui.service.SyncProcessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * 
 * @author Daniel Bernstein 
 * 
 */
@Component
public class StartSyncAction extends AbstractAction {

    private static Logger log = LoggerFactory.getLogger(StartSyncAction.class);
    private SyncProcessManager syncProcessManager;
    
    @Autowired
    public StartSyncAction(SyncProcessManager syncProcessManager){
        this.syncProcessManager = syncProcessManager;
    }
    public Event doExecute(RequestContext context) throws Exception {
        log.debug("executing...");
        this.syncProcessManager.start();
        return success();
    }
}
