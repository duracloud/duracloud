/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec.handler;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.exec.ServiceHandler;
import org.duracloud.serviceapi.ServicesManager;

import java.util.HashSet;
import java.util.Set;

/**
 * @author: Bill Branan
 * Date: 3/2/12
 */
public abstract class BaseServiceHandler implements ServiceHandler {

    protected ContentStoreManager storeMgr;
    protected ServicesManager servicesMgr;

    protected Set<String> supportedActions;

    public BaseServiceHandler() {
        supportedActions = new HashSet<String>();
    }

    @Override
    public void initialize(ContentStoreManager storeMgr,
                           ServicesManager servicesMgr) {
        this.storeMgr = storeMgr;
        this.servicesMgr = servicesMgr;
    }

    public abstract String getName();

    public abstract String getStatus();

    public abstract void start();

    public abstract void stop();

    @Override
    public Set<String> getSupportedActions() {
        return supportedActions;
    }

    public abstract void performAction(String actionName,
                                       String actionParameters);

}
