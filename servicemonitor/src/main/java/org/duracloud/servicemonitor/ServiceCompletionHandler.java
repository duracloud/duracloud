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
public interface ServiceCompletionHandler {

    /**
     * Performs work that is waiting on the completion of a service
     *
     * @param summary information about the completed service
     */
    public void handleServiceComplete(ServiceSummary summary);

}
