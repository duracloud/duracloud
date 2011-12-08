/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor;

import org.duracloud.serviceconfig.ServiceSummary;

/**
 * @author: Bill Branan
 * Date: 12/2/11
 */
public interface ServiceMonitorEventHandler {

    /**
     * Performs work after learning that a service has been deployed.
     */
    public void handleDeployEvent();

    /**
     * Performs work after learning that a service has been undeployed.
     */
    public void handleUndeployEvent();

    /**
     * Performs work after learning that a service has been had a config change.
     */
    public void handleUpdateConfigEvent();

    /**
     * Performs work after learning that a service has completed.
     */
    public void handleCompletionEvent(ServiceSummary summary);

}
