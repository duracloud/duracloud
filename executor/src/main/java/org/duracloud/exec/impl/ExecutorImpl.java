/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec.impl;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.exec.Executor;
import org.duracloud.exec.ServiceHandler;
import org.duracloud.exec.error.UnsupportedActionException;
import org.duracloud.serviceapi.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author: Bill Branan
 * Date: 3/2/12
 */
public class ExecutorImpl implements Executor {

    private final Logger log = LoggerFactory.getLogger(ExecutorImpl.class);

    private Set<ServiceHandler> serviceHandlers;
    private Set<String> supportedActions;

    boolean initialized;

    /**
     * Constructor which loads the set of handlers which the Executor will
     * use to perform work.
     *
     * @param handlers list of all available handlers
     */
    public ExecutorImpl(ServiceHandler... handlers) {
        initialized = false;
        serviceHandlers = new HashSet<ServiceHandler>(Arrays.asList(handlers));

        supportedActions = new HashSet<String>();
        for(ServiceHandler handler : serviceHandlers) {
            supportedActions.addAll(handler.getSupportedActions());
        }
    }

    @Override
    public void initialize(ContentStoreManager storeMgr,
                           ServicesManager servicesMgr) {
        log.debug("initialize() the Executor");
        for(ServiceHandler handler : serviceHandlers) {
            handler.initialize(storeMgr, servicesMgr);
        }
        initialized = true;
        start();
    }

    /**
     * Starts the work of all handlers.
     */
    private void start() {
        log.debug("start() the Executor");
        checkInitialized();

        for(ServiceHandler handler : serviceHandlers) {
            handler.start();
        }
    }

    @Override
    public void stop() {
        log.debug("stop() the Executor");
        checkInitialized();

        for(ServiceHandler handler : serviceHandlers) {
            handler.stop();
        }
    }

    @Override
    public Set<String> getSupportedActions() {
        log.debug("getSupportedActions()");
        checkInitialized();

        return supportedActions;
    }

    @Override
    public void performAction(String actionName, String actionParameters) {
        log.info("performAction(" + actionName + ", " + actionParameters + ")");
        checkInitialized();

        if(!supportedActions.contains(actionName)){
            throw new UnsupportedActionException(actionName);
        }

        for(ServiceHandler handler : serviceHandlers) {
            if(handler.getSupportedActions().contains(actionName)) {
                handler.performAction(actionName, actionParameters);
            }
        }
    }

    @Override
    public Map<String, String> getStatus() {
        log.debug("getStatus()");
        checkInitialized();

        Map<String, String> status = new HashMap<String, String>();
        for(ServiceHandler handler : serviceHandlers) {
            status.put(handler.getName(), handler.getStatus());
        }
        return status;
    }

    private void checkInitialized() {
        if(!initialized) {
            throw new DuraCloudRuntimeException("The Executor must be " +
                                                "initialized!");
        }
    }

}