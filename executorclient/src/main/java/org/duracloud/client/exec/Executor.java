/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.exec;

import org.duracloud.common.model.Securable;

import java.util.List;
import java.util.Map;

/**
 * Allows for communication with DuraBoss executor
 *
 * @author: Bill Branan
 * Date: 4/4/12
 */
public interface Executor extends Securable {

    /**
     * Lists the actions which the Executor can perform.
     *
     * @return supported actions listing
     */
    public List<String> getSupportedActions();

    /**
     * Executes a specific action.
     *
     * @param actionName the action to execute
     * @param actionParameters the information needed to execute
     */
    public void performAction(String actionName, String actionParameters);

    /**
     * Retrieves the status of the Executor, which is the collected status of
     * all Handlers. Handlers perform all of the underlying work of the
     * Executor.
     *
     * @return map of handler name to handler status
     */
    public Map<String, String> getStatus();

    /**
     * Stops the work of the Executor.
     */
    public void stop();

}
