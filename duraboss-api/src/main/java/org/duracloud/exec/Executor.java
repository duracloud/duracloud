/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec;

import org.duracloud.exec.error.InvalidActionRequestException;

import java.util.Map;
import java.util.Set;

/**
 * The Executor is responsible for managing actions that are performed
 * on storage and services for the purposes of maintaining the DuraCloud
 * instance and fulfilling the expected and expressed needs of the user.
 *
 * @author: Bill Branan
 * Date: 3/1/12
 */
public interface Executor {

    /**
     * Stops the work of the Executor.
     */
    public void stop();

    /**
     * Lists the actions which the Executor can perform, which is based on
     * the actions that all registered Handlers can perform.
     *
     * @return supported actions listing
     */
    public Set<String> getSupportedActions();

    /**
     * Executes a specific action.
     *
     * @param actionName the action to execute
     * @param actionParameters the information needed to execute
     */
    public void performAction(String actionName, String actionParameters)
        throws InvalidActionRequestException;

    /**
     * Retrieves the status of the Executor, which is the collected status of
     * all Handlers.
     *
     * @return map of handler name to handler status
     */
    public Map<String, String> getStatus();

}
