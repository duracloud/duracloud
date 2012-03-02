/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraboss.rest.exec;

import org.duracloud.common.util.SerializationUtil;
import org.duracloud.exec.Executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author: Bill Branan
 * Date: 3/1/12
 */
public class ExecResource {

    private Executor exec;

    public ExecResource(Executor exec) {
        this.exec = exec;
    }

    /**
     * Retrieves the status of the Executor
     *
     * @return Executor status information as XML
     */
    public String getExecutorStatus() {
        Map<String, String> status = exec.getStatus();
        return SerializationUtil.serializeMap(status);
    }

    /**
     * Retrieves the list of actions that the executor is able to perform
     *
     * @return Executor actions list as XML
     */
    public String getSupportedActions() {
        Set<String> supportedActions = exec.getSupportedActions();
        List<String> listActions = new ArrayList<String>(supportedActions);
        return SerializationUtil.serializeList(listActions);
    }

    /**
     * Requests that the Executor perform a specific action
     *
     * @param actionName the action to perform
     * @param actionParameters information needed to perform the action
     */
    public void performAction(String actionName, String actionParameters){
        exec.performAction(actionName, actionParameters);
    }

    /**
     * Requests that the Executor perform a graceful shutdown
     */
    public void shutdownExecutor() {
        exec.stop();
    }

}
