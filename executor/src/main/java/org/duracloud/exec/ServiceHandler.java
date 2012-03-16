/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.exec.error.InvalidActionRequestException;
import org.duracloud.serviceapi.ServicesManager;

import java.util.Set;

/**
 * A Service Handler manages the running of a DuraCloud service, starting
 * the service at the appropriate times, providing the appropriate
 * configuration information, and stopping the service when needed.
 *
 * @author: Bill Branan
 * Date: 3/1/12
 */
public interface ServiceHandler {

    /**
     * Provides the handler with access to DuraCloud storage and services.
     *
     * @param storeMgr storage manager
     * @param servicesMgr services manager
     */
    public void initialize(ContentStoreManager storeMgr,
                           ServicesManager servicesMgr);

    /**
     * Retrieves the name of the handler.
     *
     * @return name of the handler
     */
    public String getName();

    /**
     * Retrieves the current status of the handler.
     *
     * @return handler status
     */
    public String getStatus();

    /**
     * Indicates that the handler should start work.
     */
    public void start();

    /**
     * Requests that the handler should shutdown gracefully.
     */
    public void stop();

    /**
     * Retrieves the list of actions that the handler can perform.
     *
     * @return list of all action names which this handler supports
     */
    public Set<String> getSupportedActions();

    /**
     * Requests that the handler perform a given action with the given
     * information.
     *
     * @param actionName name of the action to perform
     * @param actionParameters information needed to perform the action
     * @throws InvalidActionRequestException if either the action or parameters
     *                                       are not acceptable values
     */
    public void performAction(String actionName, String actionParameters)
        throws InvalidActionRequestException;

}
